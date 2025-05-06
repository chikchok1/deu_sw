package Controller;

import View.Reservationchangeview;
import View.RoomSelect;
import Model.Session;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class ReservationchangeviewController {
    private Reservationchangeview view;

    public ReservationchangeviewController(Reservationchangeview view) {
        this.view = view;

        

        // 변경 버튼 동작 설정
        this.view.setChangeButtonActionListener(e -> handleReservationChange());

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

    private void handleReservationChange() {
        String reservationId = view.getReservationId();
        String newDay = view.getSelectedDay();
        String newTime = view.getSelectedTime();
        String selectedRoom = view.getSelectedRoom();
        String userName = Session.getLoggedInUserName();

        if (reservationId.isEmpty() || newDay.equals("선택") || newTime.equals("선택") || selectedRoom == null) {
            JOptionPane.showMessageDialog(view, "모든 항목을 입력하세요.");
            return;
        }

        if (userName == null || userName.isEmpty()) {
            JOptionPane.showMessageDialog(view, "로그인 정보가 없습니다. 먼저 로그인해주세요.");
            return;
        }

        File file = new File("data/ReserveClass.txt");
        boolean idExists = false;
        boolean ownedByUser = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int idCounter = 1;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[0].equals(userName)) {
                    String generatedId = String.format("R%03d", idCounter++);
                    if (generatedId.equals(reservationId)) {
                        idExists = true;
                        ownedByUser = true;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "파일 읽기 오류: " + e.getMessage());
            return;
        }

        if (!idExists) {
            JOptionPane.showMessageDialog(view, "입력한 예약 ID가 존재하지 않습니다.");
            return;
        }

        if (!ownedByUser) {
            JOptionPane.showMessageDialog(view, "본인의 예약만 변경할 수 있습니다.");
            return;
        }

        File changeFile = new File("data/ChangeRequest.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(changeFile, true))) {
            // 예약ID,시간,요일,강의실,이름 순서로 저장
            writer.write(reservationId + "," + newTime + "," + newDay + "," + selectedRoom + "," + userName);
            writer.newLine();

            JOptionPane.showMessageDialog(view, "변경 요청이 성공적으로 저장되었습니다.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "저장 중 오류: " + e.getMessage());
        }
    }

    private void loadUserReservations() {
        String userName = Session.getLoggedInUserName();
        if (userName == null || userName.isEmpty()) return;

        DefaultTableModel model = (DefaultTableModel) view.getReservationTable().getModel();
        model.setRowCount(0); // 테이블 초기화

        try (BufferedReader reader = new BufferedReader(new FileReader("data/ReserveClass.txt"))) {
            String line;
            int idCounter = 1;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 7 && parts[0].equals(userName)) {
                    // 예약 ID 생성 (R001, R002, ...)
                    String generatedId = String.format("R%03d", idCounter++);
                    String name = parts[0];
                    String room = parts[1];
                    String day = parts[2];
                    String time = parts[3];

                    // 테이블에 추가 (예약ID, 시간, 요일, 강의실, 이름)
                    model.addRow(new Object[]{generatedId, time, day, room, name});
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "예약 정보 불러오기 오류: " + e.getMessage());
        }
    }
}