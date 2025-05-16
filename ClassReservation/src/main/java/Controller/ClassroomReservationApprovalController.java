package Controller;

import View.ClassroomReservationApproval;
import View.Executive;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class ClassroomReservationApprovalController {
    private ClassroomReservationApproval view;

    public ClassroomReservationApprovalController(ClassroomReservationApproval view) {
        this.view = view;
        
        loadReservationRequests();  // 추가: 새 예약 요청도 불러오기
        loadChangeRequests();       // 시작 시 변경 요청 불러오기
        setApproveButtonAction();   // 승인 버튼
        setRejectButtonAction();    // 거절 버튼
       
    }

    private void loadChangeRequests() {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
       

        File file = new File("data/ChangeRequest.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    model.addRow(parts); // 예약ID, 시간, 요일, 강의실, 이름
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "요청 파일 읽기 오류: " + e.getMessage());
        }
    }
    private void loadReservationRequests() {
    DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();

    File file = new File("data/ReservationRequest.txt");
    if (!file.exists()) return;

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");

            // ✅ 예약ID가 있는 경우
            if (parts.length >= 8) {
                String reservationId = parts[0];
                String name = parts[1];
                String room = parts[2];
                String day = parts[3];
                String time = parts[4];
                model.addRow(new Object[]{reservationId, time, day, room, name});
            }

            // ✅ 예약ID가 없는 예전 형식도 허용
            else if (parts.length >= 7) {
                String name = parts[0];
                String room = parts[1];
                String day = parts[2];
                String time = parts[3];
                String fakeId = name + "_" + room;
                model.addRow(new Object[]{fakeId, time, day, room, name});
            }

            // ✅ 오류 출력
            else {
                System.out.println("잘못된 형식: " + line);
            }
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(view, "예약 요청 파일 읽기 오류: " + e.getMessage());
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

        // ✅ trim() 처리로 안전하게 ID 추출
        String reservationId = model.getValueAt(selectedRow, 0).toString().trim(); // 예약 ID (로그인 아이디)
        String newTime = model.getValueAt(selectedRow, 1).toString().trim();
        String newDay = model.getValueAt(selectedRow, 2).toString().trim();
        String newRoom = model.getValueAt(selectedRow, 3).toString().trim();
        String userName = model.getValueAt(selectedRow, 4).toString().trim();

        // ✅ 강의실/실습실 판단 (908, 912, 913, 914 → 강의실, 그 외 → 실습실)
        String[] classRooms = {"908호", "912호", "913호", "914호"};
        boolean isClassRoom = java.util.Arrays.asList(classRooms).contains(newRoom);
        String targetFile = isClassRoom ? "data/ReserveClass.txt" : "data/ReserveLab.txt";

        // ✅ 승인된 예약을 해당 파일에 추가
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile, true))) {
            String approvedLine = String.join(",", reservationId, userName, newRoom, newDay, newTime, "학과 행사", "학생", "예약됨");
            writer.write(approvedLine);
            writer.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view, "예약 저장 중 오류 발생: " + ex.getMessage());
            return;
        }

        // ✅ 요청 파일 및 원본 예약 파일에서 정확히 삭제 (trim 비교)
        removeLineFromChangeRequest(reservationId, newTime, newDay, newRoom, userName);
        removeLineFromReservationRequest(reservationId); // 여기도 내부에 trim 비교 들어가야 함

        model.removeRow(selectedRow);
        JOptionPane.showMessageDialog(view, "예약이 승인되어 저장되었습니다.");
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
    private void removeLineFromReservationRequest(String reservationId) {
    File inputFile = new File("data/ReservationRequest.txt");
    File tempFile = new File("data/ReservationRequest_temp.txt");

    try (
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
    ) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 8) {
                if (parts[0].trim().equals(reservationId.trim())) {
                    continue; // 삭제 대상 줄
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
