/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author YangJinWon
 */
import Model.Session;
import View.ReservClassView;
import View.RoomSelect;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class ReservClassController {

    private ReservClassView view;
    private Map<String, Set<String>> reservedMap = new HashMap<>();

    // 생성자: View와 컨트롤러를 연결하고 버튼 리스너를 초기화
    public ReservClassController(ReservClassView view) {
        this.view = view;
        this.view.resetReservationButtonListener();
        this.view.addReservationListener(new ReservationListener());

        // 이전 버튼 리스너 등록
        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();

            RoomSelect roomSelect = RoomSelect.getInstance();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
            view.dispose(); // 현재 ReservClassView 닫기

        });
        loadReservationData();
        JTable initialTable = buildCalendarTable(view.getSelectedClassRoom());
        view.updateCalendarTable(initialTable);

        view.getClassComboBox().addActionListener(e -> {
            String selectedRoom = view.getSelectedClassRoom();
            loadReservationData();
            JTable newTable = buildCalendarTable(selectedRoom);
            view.updateCalendarTable(newTable);
        });

    }
    // 강의실 상태 확인 메서드
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

    class ReservationListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // 세션에서 사용자 정보와 사용자가 선택한 예약 정보 가져오기
                String userName = Session.getLoggedInUserName(); // 사용자 이름 가져오기
                String selectedClassRoom = view.getSelectedClassRoom(); // 선택된 강의실
                String selectedDay = view.getSelectedDay(); // 선택된 날짜
                String selectedTime = view.getSelectedTime(); // 선택된 시간
                String purpose = view.getPurpose(); // 예약 목적

                // 예약 목적 유효성 확인
                if (purpose.isEmpty()) {
                    view.showMessage("사용 목적을 입력해주세요.");
                    return;
                }

                // 같은 요일, 시간, 강의실에 예약이 존재하는지 확인
                if (isDuplicateReservation(selectedClassRoom, selectedDay, selectedTime)) {
                    view.showMessage("이미 같은 강의실, 요일 및 시간에 예약이 존재합니다.");
                    return;
                }
                if (!isRoomAvailable(selectedClassRoom)) {
                    view.showMessage("이 강의실은 현재 사용 불가능합니다.");
                    return;
                }

                // 예약 요청을 ReservationRequest.txt에 '대기' 상태로 저장 (기존 즉시 예약 외에 추가 저장)
                String userRole = Session.getLoggedInUserRole(); // 사용자 권한 (학생, 교수 등)
                addReservationToRequestFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose, userRole);

                // 새로운 예약 정보를 파일에 추가 승인후에 처리해야 하므로 주석처리
                //addReservationToFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose);

                // 성공 메시지 출력 및 현재 View 닫기
                view.showMessage("예약이 완료되었습니다 승인 후 처리됩니다!");
                view.closeView();

                // 새로운 RoomSelect View로 전환
                RoomSelect newRoomSelect = new RoomSelect();
                new RoomSelectController(newRoomSelect);
                newRoomSelect.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace(); // 오류 로그 출력
                view.showMessage("예약 중 오류 발생: " + ex.getMessage());
            }
        }

        // 중복 예약 확인 메서드 (강의실, 요일, 시간 조건 포함)
        private boolean isDuplicateReservation(String classRoom, String day, String time) {
            try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveClass.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 5) {
                        String storedClassRoom = tokens[1].trim();
                        String storedDay = tokens[2].trim();
                        String storedTime = tokens[3].trim();

                        // 강의실, 요일, 시간이 모두 동일한 경우
                        if (storedClassRoom.equals(classRoom) && storedDay.equals(day) && storedTime.equals(time)) {
                            return true; // 중복 예약 있음
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // 파일 읽기 오류 처리
            }
            return false; // 중복 예약 없음
        }

        private void addReservationToFile(String userName, String room, String day, String time, String purpose) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveClass.txt", true))) {
                // 사용자 구분 가져오기 (예: S123 → 학생)
                String userType = "알 수 없음";
                String userId = Session.getLoggedInUserId();
                if (userId != null && !userId.isEmpty()) {
                    char typeChar = userId.charAt(0);
                    switch (typeChar) {
                        case 'S':
                            userType = "학생";
                            break;
                        case 'P':
                            userType = "교수";
                            break;
                        case 'A':
                            userType = "조교";
                            break;
                    }
                }

                writer.write(userName + "," + room + "," + day + "," + time + "," + purpose + "," + userType + ",예약됨");
                writer.newLine(); // 새 줄 추가
            } catch (Exception e) {
                e.printStackTrace(); // 파일 쓰기 오류 처리
            }
        }
    }

    private void loadReservationData() {
        reservedMap.clear();
        String filePath = "data/ReserveClass.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[6].trim().equals("예약됨")) {
                    String room = parts[1].trim();
                    String day = parts[2].trim().replace("요일", "");
                    String time = parts[3].trim().substring(0, 3);
                    reservedMap.computeIfAbsent(room, k -> new HashSet<>()).add(day + "_" + time);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 예약 여부 확인 메서드
    private boolean isReserved(String room, String day, String time) {
        String key = day + "_" + time;
        Set<String> reservedTimes = reservedMap.get(room);
        return reservedTimes != null && reservedTimes.contains(key);
    }

    // 🟩 [추가] 예약 요청 정보를 '대기' 상태로 따로 저장하는 메서드
    private void addReservationToRequestFile(String name, String room, String day, String time, String purpose, String role) {
    String userId = Session.getLoggedInUserId(); // 예약 ID로 로그인 아이디 사용
    String line = String.join(",", userId, name, room, day, time, purpose, role, "대기");

    File file = new File("data/ReservationRequest.txt");
    file.getParentFile().mkdirs();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
        writer.write(line);
        writer.newLine();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    public JTable buildCalendarTable(String room) {
        String[] columnNames = {"교시", "월", "화", "수", "목", "금"};
        String[] times = {"1교시", "2교시", "3교시", "4교시", "5교시", "6교시", "7교시", "8교시", "9교시"};

        boolean roomAvailable = isRoomAvailable(room); // 강의실 상태 확인

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
                    if (!roomAvailable) {
                        cell.setBackground(Color.DARK_GRAY); // 사용불가일 경우 회색 표시
                        cell.setText("X");
                        cell.setForeground(Color.WHITE);
                    } else {
                        String day = columnNames[column];
                        String time = times[row];
                        if (isReserved(room, day, time)) {
                            cell.setBackground(Color.RED);
                            cell.setText(""); // 예약된 경우
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
