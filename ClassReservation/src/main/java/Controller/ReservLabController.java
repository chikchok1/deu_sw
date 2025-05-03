package Controller;

import Model.Session;
import View.ReservLabView;
import View.RoomSelect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.JOptionPane;

public class ReservLabController {

    private ReservLabView view;

    // 생성자: View와 컨트롤러를 연결하고 버튼 리스너를 초기화
    public ReservLabController(ReservLabView view) {
        this.view = view;
        this.view.resetReservationButtonListener(); // 기존 리스너 초기화
        this.view.addReservationListener(new ReservationListener()); // 예약 등록 리스너
        this.view.addCancelListener(new CancelListener());         // 예약 취소 리스너 추가

        // 이전 버튼 리스너 등록
        this.view.getBeforeButton().addActionListener(e -> {
            view.dispose();
            RoomSelect roomSelect = new RoomSelect();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
        });
    }

    // ── 예약 등록 처리 ──
    class ReservationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String userName = Session.getLoggedInUserName();
                String lab       = view.getSelectedLabRoom();
                String day       = view.getSelectedDay();
                String time      = view.getSelectedTime();
                String purpose   = view.getPurpose();

                if (purpose.isEmpty()) {
                    view.showMessage("사용 목적을 입력해주세요.");
                    return;
                }
                if (isDuplicateReservation(lab, day, time)) {
                    view.showMessage("이미 같은 실습실, 요일 및 시간에 예약이 존재합니다.");
                    return;
                }
                addReservationToFile(userName, lab, day, time, purpose);

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

        private boolean isDuplicateReservation(String lab, String day, String time) {
            try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveLab.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tok = line.split(",");
                    if (tok.length >= 5
                        && tok[1].trim().equals(lab)
                        && tok[2].trim().equals(day)
                        && tok[3].trim().equals(time)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void addReservationToFile(String userName, String lab, String day, String time, String purpose) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveLab.txt", true))) {
                String userType = "알 수 없음";
                String userId   = Session.getLoggedInUserId();
                if (userId != null && !userId.isEmpty()) {
                    switch (userId.charAt(0)) {
                        case 'S': userType = "학생"; break;
                        case 'P': userType = "교수"; break;
                        case 'A': userType = "조교"; break;
                    }
                }
                writer.write(userName + "," + lab + "," + day + "," + time + "," + purpose + "," + userType + ",예약됨");
                writer.newLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ── 예약 취소 처리 ──
    class CancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String target = view.getSelectedLabRoom() + "," + view.getSelectedDay() + "," + view.getSelectedTime();
            try {
                BufferedReader reader = new BufferedReader(new FileReader("data/ReserveLab.txt"));
                StringBuilder updated = new StringBuilder();
                String line;
                boolean found = false;

                while ((line = reader.readLine()) != null) {
                    if (!line.contains(target)) {
                        updated.append(line).append("\n");
                    } else {
                        found = true;
                    }
                }
                reader.close();

                if (!found) {
                    JOptionPane.showMessageDialog(null, "일치하는 예약을 찾을 수 없습니다.");
                    return;
                }

                BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveLab.txt"));
                writer.write(updated.toString());
                writer.close();

                JOptionPane.showMessageDialog(null, "예약이 취소되었습니다.");
                // 예약 내역 조회 화면 갱신이 필요하면 호출

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "예약 취소 중 오류 발생");
            }
        }
    }
}
