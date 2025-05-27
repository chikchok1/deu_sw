package Controller;

import Model.Session;
import View.ReservClassView;
import View.RoomSelect;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.*;

public class ReservClassController {

    private ReservClassView view;
    private Map<String, Set<String>> reservedMap = new HashMap<>();

    public ReservClassController(ReservClassView view) {
        this.view = view;
        this.view.resetReservationButtonListener();
        this.view.addReservationListener(new ReservationListener());

        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();
            RoomSelect roomSelect = RoomSelect.getInstance();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
        });

        String selectedRoom = view.getSelectedClassRoom();
        refreshReservationAndAvailability(selectedRoom);

        this.view.getClassComboBox().addActionListener(e -> {
            String newSelectedRoom = view.getSelectedClassRoom();
            refreshReservationAndAvailability(newSelectedRoom);
        });
    }

    protected void refreshReservationAndAvailability(String roomName) {
        checkRoomAvailability(roomName, isAvailable -> {
            loadReservationDataFromServer(roomName);
            JTable updatedTable = buildCalendarTable(roomName, isAvailable);
            view.updateCalendarTable(updatedTable);
        });
    }

    protected void checkRoomAvailability(String classRoom, Consumer<Boolean> callback) {
        new Thread(() -> {
            boolean available = false;
            try {
                BufferedReader in = Session.getIn();
                PrintWriter out = Session.getOut();
                Socket socket = Session.getSocket();

                if (in == null || out == null || socket == null || socket.isClosed()) {
                    System.err.println("[ReservClassController] 서버 연결이 유효하지 않음");
                    callback.accept(false);
                    return;
                }

                String cleanClassRoom = classRoom.replace("호", "").trim();
                out.println("CHECK_ROOM_STATUS," + cleanClassRoom);
                out.flush();
                String response = in.readLine();
                available = "AVAILABLE".equals(response);
            } catch (IOException e) {
                System.err.println("[ReservClassController] 서버 통신 오류: " + e.getMessage());
            }

            boolean finalAvailable = available;
            SwingUtilities.invokeLater(() -> callback.accept(finalAvailable));
        }).start();
    }

    class ReservationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userName = Session.getLoggedInUserName();
            String selectedClassRoom = view.getSelectedClassRoom();
            String selectedDay = view.getSelectedDay();
            String selectedTime = view.getSelectedTime();
            String purpose = view.getPurpose();

            if (purpose.isEmpty()) {
                view.showMessage("사용 목적을 입력해주세요.");
                return;
            }

            checkRoomAvailability(selectedClassRoom, isAvailable -> {
                if (!isAvailable) {
                    view.showMessage("선택하신 강의실은 현재 사용 불가능합니다. 관리자에게 문의하세요.");
                    return;
                }

                String userRole = Session.getLoggedInUserRole();
                String response = sendReservationRequestToServer(userName, selectedClassRoom, selectedDay, selectedTime, purpose, userRole);

                switch (response) {
                    case "RESERVE_SUCCESS":
                        view.showMessage("예약이 완료되었습니다. 관리자 승인 후 처리됩니다.");
                        view.closeView();
                        RoomSelect roomSelect = new RoomSelect();
                        new RoomSelectController(roomSelect);
                        roomSelect.setVisible(true);
                        break;
                    case "RESERVE_CONFLICT":
                        view.showMessage("해당 시간에 이미 예약이 존재합니다.");
                        break;
                    case "RESERVE_FAILED":
                    default:
                        view.showMessage("예약 중 오류가 발생했습니다.");
                        break;
                }
            });
        }
    }

    protected String sendReservationRequestToServer(String name, String room, String day, String time, String purpose, String role) {
        String requestLine = String.join(",", "RESERVE_REQUEST", name, room, day, time, purpose, role);
        PrintWriter out = Session.getOut();
        BufferedReader in = Session.getIn();

        try {
            if (out != null && in != null) {
                out.println(requestLine);
                out.flush();
                String response = in.readLine();
                System.out.println("[ReservClassController] 서버 응답: " + response);
                return response;
            } else {
                System.err.println("[ReservClassController] 서버 연결이 없습니다.");
                return "RESERVE_FAILED";
            }
        } catch (IOException e) {
            System.err.println("[ReservClassController] 예약 요청 전송 중 오류: " + e.getMessage());
            return "RESERVE_FAILED";
        }
    }

    protected void loadReservationDataFromServer(String roomName) {
        PrintWriter out = Session.getOut();
        BufferedReader in = Session.getIn();

        if (out == null || in == null) {
            System.err.println("[loadReservationDataFromServer] 세션의 입출력 스트림이 null입니다.");
            return;
        }

        try {
            out.println("VIEW_RESERVATION," + Session.getLoggedInUserId() + "," + roomName);
            out.flush();

            String normalizedRoom = roomName.endsWith("호") ? roomName : roomName + "호";
            reservedMap.put(normalizedRoom, new HashSet<>());

            String line;
            int readCount = 0;
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
                        readCount++;
                    }
                }
            }
            System.out.println("[loadReservationDataFromServer] " + readCount + "개의 예약 정보 수신 완료");
        } catch (IOException e) {
            System.err.println("[loadReservationDataFromServer] 서버 응답 수신 중 오류: " + e.getMessage());
        }
    }

    private boolean isReserved(String room, String day, String time) {
        if (!room.endsWith("호")) {
            room = room + "호";
        }

        time = time.length() >= 3 ? time.substring(0, 3) : time;
        String key = day + "_" + time;
        Set<String> reservedTimes = reservedMap.get(room);
        return reservedTimes != null && reservedTimes.contains(key);
    }

    private JTable buildCalendarTable(String room, boolean roomAvailable) {
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
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 0) {
                    cell.setBackground(Color.LIGHT_GRAY);
                    cell.setHorizontalAlignment(JLabel.CENTER);
                    cell.setText(value != null ? value.toString() : "");
                } else {
                    if (!roomAvailable) {
                        cell.setBackground(Color.DARK_GRAY);
                        cell.setText("X");
                        cell.setForeground(Color.WHITE);
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
                }
                return cell;
            }
        });

        return table;
    }
}
