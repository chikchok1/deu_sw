/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author YangJinWon
 */
import Model.Session;
import View.ReservLabView;
import View.RoomSelect;
import java.awt.Color;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class ReservLabController {
private ReservLabView view;
    private Map<String, Set<String>> reservedMap = new HashMap<>();
    
    public ReservLabController(ReservLabView view) {
        this.view = view;
        this.view.resetReservationButtonListener(); 
        this.view.addReservationListener(new ReservationListener()); 

        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();  // 🔄 중복 제거
            RoomSelect roomSelect = RoomSelect.getInstance();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
        });

        loadReservationData();
        JTable initialTable = buildCalendarTable(view.getSelectedClassRoom());
        view.updateCalendarTable(initialTable); 

        this.view.getLabComboBox().addActionListener(e -> { 
            String selectedRoom = view.getSelectedClassRoom();
            JTable newTable = buildCalendarTable(selectedRoom); 
            view.updateCalendarTable(newTable);
        });
    }

    class ReservationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String userName = Session.getLoggedInUserName();
                String selectedClassRoom = view.getSelectedClassRoom();
                String selectedDay = view.getSelectedDay();
                String selectedTime = view.getSelectedTime();
                String purpose = view.getPurpose();

                if (purpose.isEmpty()) {
                    view.showMessage("사용 목적을 입력해주세요.");
                    return;
                }

                // ✅ 사용 가능 여부 체크
                if (!isRoomAvailable(selectedClassRoom)) {
                    view.showMessage("해당 강의실은 현재 사용이 불가능합니다.");
                    return;
                }

                if (isDuplicateReservation(selectedClassRoom, selectedDay, selectedTime)) {
                    view.showMessage("이미 같은 실습실, 요일 및 시간에 예약이 존재합니다.");
                    return;
                }

                // ✅ 예약 요청을 ReservationRequest.txt에 대기 상태로 저장
                String userRole = Session.getLoggedInUserRole();
                addReservationToRequestFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose, userRole);

                // ✅ 예약 추가 부분을 승인 없이 예약을 확정하면 안 되므로 아래 줄은 주석 처리함
                //addReservationToFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose);

                view.showMessage("예약이 완료되었습니다. 승인 후 확정됩니다!");
                view.closeView();

                RoomSelect newRoomSelect = new RoomSelect();
                new RoomSelectController(newRoomSelect);
                newRoomSelect.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                view.showMessage("예약 중 오류 발생: " + ex.getMessage());
            }
        }

        // 강의실 사용 가능 여부 확인 메서드
// 강의실 사용 가능 여부 확인 메서드
private boolean isRoomAvailable(String classRoom) {
    classRoom = classRoom.replace("호", ""); // '호' 제거
    String filePath = "data/RoomStatus.txt";
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length >= 2) {
                String room = tokens[0].trim();
                String status = tokens[1].trim();
                if (room.equals(classRoom)) {
                    return status.equals("사용가능");
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return true; // 기본적으로 사용 불가로 처리
}





        private boolean isDuplicateReservation(String classRoom, String day, String time) {
            String key = classRoom + "_" + day + "_" + time;
            try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveLab.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 5) {
                        String storedKey = tokens[1].trim() + "_" + tokens[2].trim() + "_" + tokens[3].trim();
                        if (storedKey.equals(key)) {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        private void addReservationToFile(String userName, String room, String day, String time, String purpose) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveLab.txt", true))) {
                String userType = "알 수 없음";
                String userId = Session.getLoggedInUserId();
                if (userId != null && !userId.isEmpty()) {
                    switch (userId.charAt(0)) {
                        case 'S': userType = "학생"; break;
                        case 'P': userType = "교수"; break;
                        case 'A': userType = "조교"; break;
                    }
                }

                writer.write(userName + "," + room + "," + day + "," + time + "," + purpose + "," + userType + ",예약됨");
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ✅ 요청을 ReservationRequest.txt 에 저장하는 메서드 추가
        private void addReservationToRequestFile(String name, String room, String day, String time, String purpose, String role) {
            String line = String.join(",", name, room, day, time, purpose, role, "대기");
            File file = new File("data/ReservationRequest.txt");
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(line);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReservationData() {
        reservedMap.clear();
        loadFromFile("data/ReserveLab.txt");
        loadFromFile("data/ReservationRequest.txt");
    }

    private void loadFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    String status = parts[6].trim();
                    if (status.equals("예약됨") || status.equals("대기")) {
                        String room = parts[1].trim();
                        String day = parts[2].trim().replace("요일", "");
                        String time = parts[3].trim().substring(0, 3);
                        reservedMap.computeIfAbsent(room, k -> new HashSet<>())
                                .add(day + "_" + time);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isReserved(String room, String day, String time) {
        Set<String> reservedTimes = reservedMap.get(room);
        return reservedTimes != null && reservedTimes.contains(day + "_" + time);
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
    // 첫 번째 열에 "교시" 데이터 채우기
        for (int i = 0; i < times.length; i++) {
            model.setValueAt(times[i], i, 0);  // 첫 번째 열에 교시값 넣기
        }

    // 셀 렌더러 설정
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel cell = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 첫 번째 열(교시 열)은 색상을 유지하고 텍스트만 출력
                if (column == 0) {
                    cell.setBackground(Color.LIGHT_GRAY); // 교시 열 배경색
                    cell.setHorizontalAlignment(JLabel.CENTER);
                } else {
                    String day = columnNames[column];
                    String time = times[row];

                    if (isReserved(room, day, time)) {
                        cell.setBackground(Color.RED);
                    } else {
                        cell.setBackground(Color.WHITE);
                    }

                    cell.setText("");
                }

                return cell;
            }
        });

        return table;
    }
}