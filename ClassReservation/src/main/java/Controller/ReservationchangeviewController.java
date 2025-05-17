package Controller;

import View.Reservationchangeview;
import View.RoomSelect;
import Model.Session;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReservationchangeviewController {

    private Reservationchangeview view;

    public ReservationchangeviewController(Reservationchangeview view) {
        this.view = view;

        // 변경 버튼 동작 설정
        this.view.setChangeButtonActionListener(e -> handleReservationChange());
        this.view.setBackButtonActionListener(e -> handleBack());

        // JTable 클릭 시 예약ID 자동 입력
        this.view.getReservationTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = view.getReservationTable().getSelectedRow();
                    if (selectedRow != -1) {
                        String selectedId = (String) view.getReservationTable().getValueAt(selectedRow, 0);
                        view.setReservationId(selectedId);
                    }
                }
            }
        });

        // 로그인한 사용자의 예약 정보 불러오기
        loadUserReservations();
    }

    private void handleBack() {
        view.dispose(); // 현재 창 닫기
        RoomSelect roomSelect = RoomSelect.getInstance(); // 기존 인스턴스 가져오기
        new RoomSelectController(roomSelect); // 컨트롤러 다시 연결
        roomSelect.setVisible(true); // 화면 다시 띄우기
    }

    private void handleReservationChange() {
        String newDay = view.getSelectedDay();
        String newTime = view.getSelectedTime();
        String selectedRoom = view.getSelectedRoom();
        String userId = Session.getLoggedInUserId(); // S123, P001 등
        String userName = Session.getLoggedInUserName();

        if (newDay.equals("선택") || newTime.equals("선택") || selectedRoom == null) {
            JOptionPane.showMessageDialog(view, "모든 항목을 입력하세요.");
            return;
        }

        if (userId == null || userName == null || userId.isEmpty() || userName.isEmpty()) {
            JOptionPane.showMessageDialog(view, "로그인 정보가 없습니다. 먼저 로그인해주세요.");
            return;
        }

        // 중복 예약 체크: 다른 사용자가 같은 시간, 요일, 강의실로 변경 요청한 경우
        File changeFile = new File("data/ChangeRequest.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(changeFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String existingUserId = parts[0];
                    String time = parts[1];
                    String day = parts[2];
                    String room = parts[3];

                    if (!existingUserId.equals(userId) && time.equals(newTime) && day.equals(newDay) && room.equals(selectedRoom)) {
                        JOptionPane.showMessageDialog(view, "이미 해당 시간에 강의실이 예약되어 있습니다.");
                        return;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "파일 읽기 오류: " + e.getMessage());
            return;
        }

        // 변경 요청 반영 (또는 새로 추가)
        File tempFile = new File("data/ChangeRequest_temp.txt");

        try (
                BufferedReader reader = new BufferedReader(new FileReader(changeFile)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            boolean updated = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(userId)) {
                    // 사용자 본인의 기존 요청 덮어쓰기
                    writer.write(userId + "," + newTime + "," + newDay + "," + selectedRoom + "," + userName);
                    writer.newLine();
                    updated = true;
                } else {
                    writer.write(line);
                    writer.newLine();
                }
            }

            // 변경 요청 반영 (또는 새로 추가)
            if (!updated) {
                writer.write(userId + "," + newTime + "," + newDay + "," + selectedRoom + "," + userName);
                writer.newLine();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "변경 저장 중 오류: " + e.getMessage());
            return;
        }

        if (!changeFile.delete() || !tempFile.renameTo(changeFile)) {
            JOptionPane.showMessageDialog(view, "파일 갱신 중 오류가 발생했습니다.");
            return;
        }

        JOptionPane.showMessageDialog(view, "변경 요청이 성공적으로 저장되었습니다.");
        loadUserReservations(); // 테이블 갱신
    }

    private void loadUserReservations() {
        String userName = Session.getLoggedInUserName();
        if (userName == null || userName.isEmpty()) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getReservationTable().getModel();
        model.setRowCount(0); // 테이블 초기화

        // 1. 원본 예약 읽기
        File originalFile = new File("data/ReserveClass.txt");
        Map<String, String[]> reservationMap = new LinkedHashMap<>();
        int idCounter = 1;

        String userId = Session.getLoggedInUserId(); // ← 사용자 ID를 직접 가져옴

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 6 && parts[0].equals(userName)) {
                    reservationMap.put(userId, new String[]{parts[3], parts[2], parts[1], parts[0]});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "예약 정보 불러오기 오류: " + e.getMessage());
            return;
        }

        // 2. 변경 요청 반영 (있다면 덮어쓰기)
        File changeFile = new File("data/ChangeRequest.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(changeFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(userId)) {
                    // parts = [userId, time, day, room]
                    reservationMap.put(userId, new String[]{parts[1], parts[2], parts[3], userName});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "변경 요청 정보 읽기 오류: " + e.getMessage());
        }

        // 3. 최종 데이터 테이블에 반영
        for (Map.Entry<String, String[]> entry : reservationMap.entrySet()) {
            String id = entry.getKey();
            String[] data = entry.getValue();
            model.addRow(new Object[]{id, data[0], data[1], data[2], data[3]});
        }
    }
}
