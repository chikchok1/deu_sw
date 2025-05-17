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

        loadChangeRequests();       // 시작 시 변경 요청 불러오기
        setApproveButtonAction();   // 승인 버튼
        setRejectButtonAction();    // 거절 버튼
       
    }

    private void loadChangeRequests() {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        model.setRowCount(0); // 테이블 초기화

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

    public void setApproveButtonAction() {
        view.getApproveButton().addActionListener(e -> {
            int selectedRow = view.getTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(view, "승인할 행을 선택하세요.");
                return;
            }

            DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
            String reservationId = (String) model.getValueAt(selectedRow, 0);
            String newTime = (String) model.getValueAt(selectedRow, 1);
            String newDay = (String) model.getValueAt(selectedRow, 2);
            String newRoom = (String) model.getValueAt(selectedRow, 3);
            String userName = (String) model.getValueAt(selectedRow, 4);

            File reserveFile = new File("data/ReserveClass.txt");
            File tempFile = new File("data/ReserveClass_temp.txt");

            try (
                BufferedReader reader = new BufferedReader(new FileReader(reserveFile));
                BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
            ) {
                String line;
                boolean updated = false;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 7 &&
                        parts[0].equals(userName)) {

                        String oldRoom = parts[1].trim();
                        String oldDay = parts[2].trim();
                        String oldTime = parts[3].trim();

                        if (oldRoom.equals(newRoom) && oldDay.equals(newDay) && oldTime.equals(newTime)) {
                            writer.write(line); // 이미 변경된 경우
                        } else {
                            String updatedLine = String.join(",", userName, newRoom, newDay, newTime, parts[4], parts[5], parts[6]);
                            writer.write(updatedLine);
                            updated = true;
                        }

                    } else {
                        writer.write(line);
                    }
                    writer.newLine();
                }

                if (!updated) {
                    String updatedLine = String.join(",", userName, newRoom, newDay, newTime, "학과 행사", "학생", "예약됨");
                    writer.write(updatedLine);
                    writer.newLine();
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(view, "예약 수정 중 오류: " + ex.getMessage());
                return;
            }

            if (reserveFile.delete()) {
                tempFile.renameTo(reserveFile);
            }

            removeLineFromChangeRequest(reservationId, newTime, newDay, newRoom, userName);

            model.removeRow(selectedRow);
            JOptionPane.showMessageDialog(view, "승인 완료되었습니다.");
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
