package Controller;

import View.Reservationchangeview;
import View.RoomSelect;
import Model.Session;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.io.*;

public class ReservationchangeviewController {

    private Reservationchangeview view;

    public ReservationchangeviewController(Reservationchangeview view) {
        this.view = view;

        this.view.setChangeButtonActionListener(e -> handleReservationChange());
        this.view.setBackButtonActionListener(e -> handleBack());
        this.view.setCancelButtonActionListener(e -> handleReservationCancel());

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

        loadUserReservations();
    }

    private void handleBack() {
        view.dispose();
        RoomSelect roomSelect = RoomSelect.getInstance();
        new RoomSelectController(roomSelect);
        roomSelect.setVisible(true);
    }

    private void handleReservationChange() {
        int selectedRow = view.getReservationTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "변경할 예약을 선택하세요.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getReservationTable().getModel();
        String reservationId = (String) model.getValueAt(selectedRow, 0);
        String originalTime = (String) model.getValueAt(selectedRow, 1);
        String originalDay = (String) model.getValueAt(selectedRow, 2);
        String room = (String) model.getValueAt(selectedRow, 3);

        String newDay = view.getSelectedDay();
        String newTime = view.getSelectedTime();
        String selectedRoom = view.getSelectedRoom();
        String userId = Session.getLoggedInUserId();
        String userName = Session.getLoggedInUserName();

        if (newDay.equals("선택") || newTime.equals("선택") || selectedRoom == null) {
            JOptionPane.showMessageDialog(view, "모든 항목을 입력하세요.");
            return;
        }

        if (userId == null || userName == null || userId.isEmpty() || userName.isEmpty()) {
            JOptionPane.showMessageDialog(view, "로그인 정보가 없습니다. 먼저 로그인해주세요.");
            return;
        }

        File changeFile = new File("data/ChangeRequest.txt");
        if (changeFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(changeFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String existingTime = parts[1];
                        String existingDay = parts[2];
                        String existingRoom = parts[3];

                        if (existingTime.equals(newTime) && existingDay.equals(newDay) && existingRoom.equals(selectedRoom)) {
                            JOptionPane.showMessageDialog(view, "해당 시간, 요일, 강의실에 이미 변경 요청이 존재합니다.");
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "파일 읽기 오류: " + e.getMessage());
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data/ChangeRequest.txt", true))) {
            writer.write(userId + "," + newTime + "," + newDay + "," + selectedRoom + "," + userName);
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "변경 저장 중 오류: " + e.getMessage());
            return;
        }

        String filePath = isLectureRoom(room) ? "data/ReserveClass.txt" : "data/ReserveLab.txt";
        deleteReservationFromFile(filePath, userName, room, originalDay, originalTime);
        model.removeRow(selectedRow);

        JOptionPane.showMessageDialog(view, "변경 요청이 저장되고 기존 예약이 삭제되었습니다.");
    }

    private void handleReservationCancel() {
        int selectedRow = view.getReservationTable().getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(view, "취소할 예약을 선택하세요.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getReservationTable().getModel();
        String time = (String) model.getValueAt(selectedRow, 1);
        String day = (String) model.getValueAt(selectedRow, 2);
        String room = (String) model.getValueAt(selectedRow, 3);
        String userName = Session.getLoggedInUserName();
        String filePath = isLectureRoom(room) ? "data/ReserveClass.txt" : "data/ReserveLab.txt";

        deleteReservationFromFile(filePath, userName, room, day, time);
        model.removeRow(selectedRow);

        JOptionPane.showMessageDialog(view, "예약이 취소되었습니다.");
    }

    private void loadUserReservations() {
        String userName = Session.getLoggedInUserName();
        String userId = Session.getLoggedInUserId();
        if (userName == null || userId == null || userName.isEmpty() || userId.isEmpty()) {
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getReservationTable().getModel();
        model.setRowCount(0);

        java.util.List<String[]> reservationList = new java.util.ArrayList<>();

        File classFile = new File("data/ReserveClass.txt");
        if (classFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(classFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 7 && parts[0].equals(userName)) {
                        reservationList.add(new String[]{userId, parts[3], parts[2], parts[1], parts[0]});
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "ReserveClass.txt 읽기 오류: " + e.getMessage());
            }
        }

        File labFile = new File("data/ReserveLab.txt");
        if (labFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(labFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 7 && parts[0].equals(userName)) {
                        reservationList.add(new String[]{userId, parts[3], parts[2], parts[1], parts[0]});
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "ReserveLab.txt 읽기 오류: " + e.getMessage());
            }
        }

        for (String[] data : reservationList) {
            model.addRow(data);
        }
    }

    private void deleteReservationFromFile(String filePath, String userName, String room, String day, String time) {
        File inputFile = new File(filePath);
        File tempFile = new File("data/temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String name = parts[0].trim();
                    String r = parts[1].trim();
                    String d = parts[2].trim();
                    String t = parts[3].trim();

                    if (name.equals(userName) && r.equals(room) && d.equals(day) && t.equals(time)) {
                        System.out.println("삭제된 줄: " + line);
                        continue;
                    }
                }

                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "파일 처리 중 오류: " + e.getMessage());
        }

        try {
            java.nio.file.Files.delete(inputFile.toPath());
            java.nio.file.Files.move(tempFile.toPath(), inputFile.toPath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "파일 갱신 실패: " + e.getMessage());
        }
    }

    private boolean isLectureRoom(String room) {
        return room.equals("908호") || room.equals("912호") || room.equals("913호") || room.equals("914호");
    }
}
