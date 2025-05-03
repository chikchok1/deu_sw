package Controller;

import Model.Session;
import View.ReservClassView;
import View.RoomSelect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.JOptionPane;

public class ReservClassController {

    private ReservClassView view;

    public ReservClassController(ReservClassView view) {
        this.view = view;
        this.view.resetReservationButtonListener();
        this.view.addReservationListener(new ReservationListener());

        // 🔹 예약 취소 버튼 리스너 등록
        this.view.addCancelListener(new CancelListener());

        // 이전 버튼 리스너 등록
        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();
            RoomSelect roomSelect = new RoomSelect();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
        });
    }

    // 🔸 예약 버튼 처리
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

                if (isDuplicateReservation(selectedClassRoom, selectedDay, selectedTime)) {
                    view.showMessage("이미 같은 강의실, 요일 및 시간에 예약이 존재합니다.");
                    return;
                }

                addReservationToFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose);

                view.showMessage("예약이 완료되었습니다!");
                view.closeView();

                RoomSelect newRoomSelect = new RoomSelect();
                new RoomSelectController(newRoomSelect);
                newRoomSelect.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                view.showMessage("예약 중 오류 발생: " + ex.getMessage());
            }
        }

        private boolean isDuplicateReservation(String classRoom, String day, String time) {
            try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveClass.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 5) {
                        String storedClassRoom = tokens[1].trim();
                        String storedDay = tokens[2].trim();
                        String storedTime = tokens[3].trim();
                        if (storedClassRoom.equals(classRoom) && storedDay.equals(day) && storedTime.equals(time)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void addReservationToFile(String userName, String room, String day, String time, String purpose) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveClass.txt", true))) {
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
                writer.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🔸 예약 취소 리스너 추가
    class CancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String reservationInfo = view.getSelectedReservationInfo();  // "강의실,요일,시간" 포맷

            try {
                BufferedReader reader = new BufferedReader(new FileReader("data/ReserveClass.txt"));
                StringBuilder updatedData = new StringBuilder();
                String line;
                boolean found = false;

                while ((line = reader.readLine()) != null) {
                    if (!line.contains(reservationInfo)) {
                        updatedData.append(line).append("\n");
                    } else {
                        found = true;
                    }
                }
                reader.close();

                if (!found) {
                    JOptionPane.showMessageDialog(null, "일치하는 예약을 찾을 수 없습니다.");
                    return;
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveClass.txt"));
                writer.write(updatedData.toString());
                writer.close();

                JOptionPane.showMessageDialog(null, "예약이 취소되었습니다.");

                // 예약 내역 갱신 필요 시 ReservedRoomView.refreshReservationList(); 호출

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "예약 취소 중 오류 발생");
            }
        }
    }
}
