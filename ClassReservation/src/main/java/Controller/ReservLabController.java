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
import View.ReservLabView;
import View.RoomSelect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ReservLabController {

    private ReservLabView view;

    // 생성자: View와 컨트롤러를 연결하고 버튼 리스너를 초기화
    public ReservLabController(ReservLabView view) {
        this.view = view;
        this.view.resetReservationButtonListener(); // 기존 리스너 초기화
        this.view.addReservationListener(new ReservationListener()); // 새로운 리스너 등록
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

                // 같은 요일, 시간, 실습실에 예약이 존재하는지 확인
                if (isDuplicateReservation(selectedClassRoom, selectedDay, selectedTime)) {
                    view.showMessage("이미 같은 실습실, 요일 및 시간에 예약이 존재합니다.");
                    return;
                }

                // 새로운 예약 정보를 파일에 추가
                addReservationToFile(userName, selectedClassRoom, selectedDay, selectedTime, purpose);

                // 성공 메시지 출력 및 현재 View 닫기
                view.showMessage("예약이 완료되었습니다!");
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
            try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveLab.txt"))) {
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ReserveLab.txt", true))) {
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
}
