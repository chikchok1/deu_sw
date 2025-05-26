package Controller;

import Model.Session;
import View.ReservLabView;
import View.RoomSelect;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class ReservLabController {
    private ReservLabView view;
    private Map<String, Set<String>> reservedMap = new HashMap<>();

    public ReservLabController(ReservLabView view) {
        this.view = view;
        this.view.resetReservationButtonListener();
        this.view.addReservationListener(new ReservationListener());

        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();
            RoomSelect roomSelect = RoomSelect.getInstance();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
        });

        String initialRoom = view.getSelectedClassRoom();
        refreshReservationData(initialRoom);
        view.updateCalendarTable(buildCalendarTable(initialRoom));

        this.view.getLabComboBox().addActionListener(e -> {
            String selectedRoom = view.getSelectedClassRoom();
            refreshReservationData(selectedRoom);
            view.updateCalendarTable(buildCalendarTable(selectedRoom));
        });
    }

    private void refreshReservationData(String roomName) {
        reservedMap.clear();
        loadReservationDataFromServer(roomName);
    }

    private void checkRoomAvailability(String room, Consumer<Boolean> callback) {
        new Thread(() -> {
            boolean available = false;
            try {
                PrintWriter out = Session.getOut();
                BufferedReader in = Session.getIn();

                String cleanRoom = room.replace("호", "").trim();
                out.println("CHECK_ROOM_STATUS," + cleanRoom);
                out.flush();

                String response = in.readLine();
                available = "AVAILABLE".equals(response);
            } catch (IOException e) {
                System.err.println("[ReservLabController] 서버 통신 오류: " + e.getMessage());
            }

            boolean finalAvailable = available;
            SwingUtilities.invokeLater(() -> callback.accept(finalAvailable));
        }).start();
    }

    private void loadReservationDataFromServer(String roomName) {
        PrintWriter out = Session.getOut();
        BufferedReader in = Session.getIn();

        if (out == null || in == null) return;

        String normalizedRoom = roomName.endsWith("호") ? roomName : roomName + "호";
        reservedMap.put(normalizedRoom, new HashSet<>());

        try {
            out.println("VIEW_RESERVATION," + Session.getLoggedInUserId() + "," + roomName);
            out.flush();

            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("END_OF_RESERVATION")) break;

                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String status = parts[6].trim();
                    if (status.equals("예약됨") || status.equals("대기")) {
                        String room = parts[1].trim();
                        String day = parts[2].trim().replace("요일", "");
                        String time = parts[3].trim().substring(0, 3);
                        room = room.endsWith("호") ? room : room + "호";
                        reservedMap.computeIfAbsent(room, k -> new HashSet<>()).add(day + "_" + time);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ReservLabController] 예약 데이터 수신 오류: " + e.getMessage());
        }
    }

    private boolean isReserved(String room, String day, String time) {
        if (!room.endsWith("호")) room += "호";
        String key = day + "_" + (time.length() >= 3 ? time.substring(0, 3) : time);
        Set<String> reservedTimes = reservedMap.get(room);
        return reservedTimes != null && reservedTimes.contains(key);
    }

    private String sendReservationRequestToServer(String name, String room, String day, String time, String purpose, String role) {
        PrintWriter out = Session.getOut();
        BufferedReader in = Session.getIn();

        String request = String.join(",", "RESERVE_REQUEST", name, room, day, time, purpose, role);

        try {
            out.println(request);
            out.flush();
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "RESERVE_FAILED";
        }
    }

    class ReservationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = Session.getLoggedInUserName();
            String selectedRoom = view.getSelectedClassRoom();
            String selectedDay = view.getSelectedDay();
            String selectedTime = view.getSelectedTime();
            String purpose = view.getPurpose();

            if (purpose.isEmpty()) {
                view.showMessage("사용 목적을 입력해주세요.");
                return;
            }

            checkRoomAvailability(selectedRoom, isAvailable -> {
                if (!isAvailable) {
                    view.showMessage("해당 실습실은 현재 사용 불가능합니다.");
                    return;
                }

                String userRole = Session.getLoggedInUserRole();
                String response = sendReservationRequestToServer(userName, selectedRoom, selectedDay, selectedTime, purpose, userRole);

                switch (response) {
                    case "RESERVE_SUCCESS":
                        view.showMessage("예약이 완료되었습니다. 승인 후 확정됩니다.");
                        view.closeView();
                        RoomSelect rs = new RoomSelect();
                        new RoomSelectController(rs);
                        rs.setVisible(true);
                        break;
                    case "RESERVE_CONFLICT":
                        view.showMessage("이미 해당 시간에 예약이 존재합니다.");
                        break;
                    default:
                        view.showMessage("예약 처리 중 오류가 발생했습니다.");
                }
            });
        }
    }

    public JTable buildCalendarTable(String room) {
        String[] columnNames = {"교시", "월", "화", "수", "목", "금"};
        String[] times = {"1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시", "8교시", "9교시"};

        DefaultTableModel model = new DefaultTableModel(times.length, columnNames.length);
        model.setColumnIdentifiers(columnNames);

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(Color.GRAY);

        TableColumn firstColumn = table.getColumnModel().getColumn(0);
        firstColumn.setPreferredWidth(60);
        firstColumn.setMaxWidth(60);
        firstColumn.setMinWidth(60);

        for (int i = 0; i < times.length; i++) {
            model.setValueAt(times[i], i, 0);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    cell.setBackground(Color.LIGHT_GRAY);
                    cell.setHorizontalAlignment(JLabel.CENTER);
                    cell.setText(value != null ? value.toString() : "");
                } else {
                    String day = columnNames[column];
                    String time = times[row];
                    if (isReserved(room, day, time)) {
                        cell.setBackground(Color.RED);
                        cell.setText("");
                    } else {
                        cell.setBackground(Color.WHITE);
                        cell.setText("");
                    }
                }

                return cell;
            }
        });

        return table;
    }
}
