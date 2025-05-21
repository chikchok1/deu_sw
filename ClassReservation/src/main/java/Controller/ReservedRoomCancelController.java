package Controller;

import Model.Session;
import View.Executive;
import View.ReservedRoomCancelView;
import Model.UserDAO; // 🔵 UserDAO import

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReservedRoomCancelController {

    private ReservedRoomCancelView view;
    private final String CLASS_FILE = "data/ReserveClass.txt";
    private final String LAB_FILE = "data/ReserveLab.txt";

    public ReservedRoomCancelController(ReservedRoomCancelView view) {
        this.view = view;

        loadUserReservations(); // 🔵 예약 불러오기
        setCancelAction();      // 🔵 취소 버튼 처리
        setBackAction();        // 🔵 이전 버튼 처리
    }

    // 🔵 예약 파일 로드 (ReserveClass + ReserveLab)
    private void loadUserReservations() {
        DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
        model.setRowCount(0); // 테이블 초기화

        String currentUserId = Session.getLoggedInUserId();
        String currentRole = Session.getLoggedInUserRole();

        // 🔄 조교면 전체 조회, 아니면 본인 것만
        if ("조교".equals(currentRole)) {
            loadFromFile(CLASS_FILE, null, model);
            loadFromFile(LAB_FILE, null, model);
        } else {
            loadFromFile(CLASS_FILE, currentUserId, model);
            loadFromFile(LAB_FILE, currentUserId, model);
        }
    }

    private void loadFromFile(String filePath, String filterUserId, DefaultTableModel model) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length >= 7) {
                    String name = parts[0].trim();
                    String userId = new UserDAO().getUserIdByName(name); // 이름 → ID 변환

                    if (filterUserId == null || filterUserId.equals(userId)) {
                        model.addRow(new Object[]{
                            userId,       // 예약 ID (로그인 ID 기준)
                            parts[3],     // 시간
                            parts[2],     // 요일
                            parts[1],     // 강의실
                            name          // 이름
                        });
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "파일 읽기 오류: " + e.getMessage());
        }
    }

    // 🔵 취소 버튼
    private void setCancelAction() {
        view.getCancelButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JTable table = view.getTable();
                int selectedRow = table.getSelectedRow();

                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "취소할 예약을 선택하세요.");
                    return;
                }

                String userId = table.getValueAt(selectedRow, 0).toString();
                String time = table.getValueAt(selectedRow, 1).toString();
                String day = table.getValueAt(selectedRow, 2).toString();
                String room = table.getValueAt(selectedRow, 3).toString();
                String userName = table.getValueAt(selectedRow, 4).toString();

                String targetFile = (room.equals("908호") || room.equals("912호") || room.equals("913호") || room.equals("914호"))
                        ? CLASS_FILE : LAB_FILE;

                if (deleteReservation(targetFile, userName, day, time, room)) {
                    ((DefaultTableModel) table.getModel()).removeRow(selectedRow);
                    JOptionPane.showMessageDialog(null, "예약이 취소되었습니다.");
                } else {
                    JOptionPane.showMessageDialog(null, "예약 취소 실패 또는 해당 정보 없음.");
                }
            }
        });
    }

    // 🔴 예약 삭제 처리 (userName 기준)
    private boolean deleteReservation(String filePath, String userName, String day, String time, String room) {
        File inputFile = new File(filePath);
        List<String> updatedLines = new ArrayList<>();
        boolean deleted = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length >= 7 &&
                    parts[0].trim().equals(userName) &&
                    parts[1].trim().equals(room) &&
                    parts[2].trim().equals(day) &&
                    parts[3].trim().equals(time)) {
                    deleted = true;
                    continue;
                }

                updatedLines.add(line);
            }
        } catch (IOException e) {
            System.out.println("삭제 중 오류: " + e.getMessage());
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFile))) {
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("파일 저장 오류: " + e.getMessage());
            return false;
        }

        return deleted;
    }

    // 🔙 이전 버튼
    private void setBackAction() {
        view.getBackButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.dispose();
                Executive execView = new Executive();
                new ExecutiveController(execView);
                execView.setVisible(true);
            }
        });
    }
}
