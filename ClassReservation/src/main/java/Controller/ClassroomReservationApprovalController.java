package Controller;

import View.ClassroomReservationApproval;
import View.Executive;
import Model.Session; // ✅ 이 줄 추가 (필수)
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;


public class ClassroomReservationApprovalController {
    private ClassroomReservationApproval view;

    public ClassroomReservationApprovalController(ClassroomReservationApproval view) {
        this.view = view;

        loadAllRequests(); // ✅ 기존 loadChangeRequests() 대신 이걸로 변경
        setApproveButtonAction();   // 승인 버튼
        setRejectButtonAction();    // 거절 버튼
       
    }

  private void loadAllRequests() {
    DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
    model.setRowCount(0); // 테이블 초기화

    // ✅ 1. ChangeRequest.txt (변경 요청)
    File changeFile = new File("data/ChangeRequest.txt");
    if (changeFile.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(changeFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String id = parts[0].trim();   // 예약ID (로그인 아이디)
                    String time = parts[1].trim();
                    String day  = parts[2].trim();
                    String room = parts[3].trim();
                    String name = parts[4].trim();

                    model.addRow(new String[]{id, time, day, room, name});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "ChangeRequest.txt 읽기 실패: " + e.getMessage());
        }
    }

    // ✅ 2. ReservationRequest.txt (신규 예약 요청)
    File requestFile = new File("data/ReservationRequest.txt");
    if (requestFile.exists()) {
        try (BufferedReader reader = new BufferedReader(new FileReader(requestFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    // 구조: 이름, 강의실, 요일, 시간, 목적, 권한, 상태
                    String id   = Session.getLoggedInUserId(); // 시각적으로 예약ID처럼 사용
                    String name = parts[0].trim();
                    String room = parts[1].trim();
                    String day  = parts[2].trim();
                    String time = parts[3].trim();

                    model.addRow(new String[]{id, time, day, room, name});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "ReservationRequest.txt 읽기 실패: " + e.getMessage());
        }
    }

}



    public void setApproveButtonAction() {
    view.getApproveButton().addActionListener(e -> {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "승인할 행을 선택하세요.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        String id = ((String) model.getValueAt(selectedRow, 0)).trim();     // 예약ID (로그인 아이디)
        String time = ((String) model.getValueAt(selectedRow, 1)).trim();
        String day = ((String) model.getValueAt(selectedRow, 2)).trim();
        String room = ((String) model.getValueAt(selectedRow, 3)).trim();
        String name = ((String) model.getValueAt(selectedRow, 4)).trim();   // 예약자 이름

        String 목적 = "";
        String 권한 = "";
        boolean deleted = false;

        // ✅ 1. ReservationRequest.txt 에서 삭제 시도
        File reservationFile = new File("data/ReservationRequest.txt");
        File tempReservation = new File("data/temp1.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(reservationFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempReservation))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 &&
                    parts[0].trim().equals(name) &&
                    parts[1].trim().equals(room) &&
                    parts[2].trim().equals(day) &&
                    parts[3].trim().equals(time)) {

                    목적 = parts[4].trim();
                    권한 = parts[5].trim();
                    deleted = true;
                    continue;
                }
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view, "ReservationRequest 삭제 오류: " + ex.getMessage());
            return;
        }

        if (deleted) {
            reservationFile.delete();
            tempReservation.renameTo(reservationFile);
        } else {
            // ✅ 2. ChangeRequest.txt 에서 삭제 시도
            File changeFile = new File("data/ChangeRequest.txt");
            File tempChange = new File("data/temp2.txt");

            try (BufferedReader reader = new BufferedReader(new FileReader(changeFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempChange))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 5 &&
                        parts[0].trim().equals(id) &&
                        parts[1].trim().equals(time) &&
                        parts[2].trim().equals(day) &&
                        parts[3].trim().equals(room) &&
                        parts[4].trim().equals(name)) {
                        deleted = true;
                        continue;
                    }
                    writer.write(line);
                    writer.newLine();
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "ChangeRequest 삭제 오류: " + ex.getMessage());
                return;
            }

            if (deleted) {
                changeFile.delete();
                tempChange.renameTo(changeFile);
            }
        }

        if (!deleted) {
            JOptionPane.showMessageDialog(view, "원본 파일에서 삭제 실패.");
            return;
        }

        // ✅ 3. 강의실/실습실 분기
        String numberOnly = room.replaceAll("[^0-9]", "");
        int roomNumber = Integer.parseInt(numberOnly);
        String targetFile = "data/ReserveClass.txt";
        if (!(roomNumber == 908 || roomNumber == 912 || roomNumber == 913 || roomNumber == 914)) {
            targetFile = "data/ReserveLab.txt";
        }

        // ✅ 4. 최종 예약 저장
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true))) {
            String saveLine = String.join(",", name, room, day, time, 목적, 권한, "예약됨");
            writer.write(saveLine);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view, "승인된 예약 저장 중 오류: " + ex.getMessage());
            return;
        }

        // ✅ 5. 승인 백업
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ApprovedBackup.txt", true))) {
            String backupLine = String.join(",", name, room, day, time, 목적, 권한, "승인");
            writer.write(backupLine);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view, "승인 백업 저장 중 오류: " + ex.getMessage());
        }

        model.removeRow(selectedRow);
        JOptionPane.showMessageDialog(view, "예약 승인 완료되었습니다.");
    });
}









    public void setRejectButtonAction() {
    view.getRejectButton().addActionListener(e -> {
        int selectedRow = view.getTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "거절할 행을 선택하세요.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        String reservationId = (String) model.getValueAt(selectedRow, 0);
        String newTime = (String) model.getValueAt(selectedRow, 1);
        String newDay = (String) model.getValueAt(selectedRow, 2);
        String newRoom = (String) model.getValueAt(selectedRow, 3);
        String userName = (String) model.getValueAt(selectedRow, 4);

        removeLineFromChangeRequest(reservationId, newTime, newDay, newRoom, userName);
        model.removeRow(selectedRow);

        JOptionPane.showMessageDialog(view, "거절되었습니다. 요청이 삭제되었습니다.");
    });
}


  

    private void removeLineFromChangeRequest(String reservationId, String time, String day, String room, String name) {
    File inputFile = new File("data/ChangeRequest.txt");
    File tempFile = new File("data/temp.txt");

    try (
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
    ) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length == 5) {
                String id = parts[0];
                String t = parts[1];
                String d = parts[2];
                String r = parts[3];
                String n = parts[4];

                if (id.equals(reservationId) && t.equals(time) && d.equals(day) && r.equals(room) && n.equals(name)) {
                    continue; // 삭제
                }
            }
            writer.write(line);
            writer.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    inputFile.delete();
    tempFile.renameTo(inputFile);
}

}
