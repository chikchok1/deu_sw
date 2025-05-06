package Controller;

import Model.ReservedRoomModel;
import Model.Session;
import View.ReservedRoomView;
import View.RoomSelect;
import View.Executive;
import javax.swing.JTable;

public class ReservedRoomController {
    private ReservedRoomView view;
    private ReservedRoomModel model;

    public ReservedRoomController(ReservedRoomView view) {
        this.view = view;
        this.model = new ReservedRoomModel();
        addListeners();
    }

    private void addListeners() {
        // [1] "확인" 버튼 클릭 시
        view.getCheckButton().addActionListener(e -> {
            String selectedRoom = view.getSelectedRoom();
            if (!"선택".equals(selectedRoom)) {
                loadReservedRooms(selectedRoom);
            }
        });

        // [2] 강의실 선택 콤보박스
        view.getClassComboBox().addActionListener(e -> {
            if (view.isUpdating()) return;
            view.setUpdating(true);
            view.resetLabSelection(); // 실습실 초기화
            String selectedRoom = view.getSelectedRoom();
            if (!"선택".equals(selectedRoom)) {
                loadReservedRooms(selectedRoom);
            }
            view.setUpdating(false);
        });

        // [3] 실습실 선택 콤보박스
        view.getLabComboBox().addActionListener(e -> {
            if (view.isUpdating()) return;
            view.setUpdating(true);
            view.resetClassSelection(); // 강의실 초기화
            String selectedRoom = view.getSelectedRoom();
            if (!"선택".equals(selectedRoom)) {
                loadReservedRooms(selectedRoom);
            }
            view.setUpdating(false);
        });

        // [4] 이전 버튼 클릭 시
        view.getBeforeButton().addActionListener(e -> {
            view.dispose();
            char userType = Session.getLoggedInUserId().charAt(0);
            if (userType == 'E') {
                Executive executive = new Executive();
                new ExecutiveController(executive);
                executive.setVisible(true);
            } else {
                RoomSelect roomSelect = new RoomSelect();
                new RoomSelectController(roomSelect);
                roomSelect.setVisible(true);
            }
        });
    }

    private void loadReservedRooms(String selectedRoom) {
        JTable table = view.getTable();
        // 테이블 초기화
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 1; col < table.getColumnCount(); col++) {
                table.setValueAt("", row, col);
            }
        }

        char userType = Session.getLoggedInUserId().charAt(0);
        String userName = Session.getLoggedInUserName();

        // 두 파일 모두 읽어옴
        loadFileToTable("data/ReserveClass.txt", selectedRoom, userType, userName);
        loadFileToTable("data/ReserveLab.txt", selectedRoom, userType, userName);
    }

    private void loadFileToTable(String filePath, String selectedRoom, char userType, String userName) {
        JTable table = view.getTable();
        for (var r : model.getReservations(filePath, selectedRoom, userType, userName)) {
            int col = getDayColumn(r.day);
            int row = getPeriodRow(r.period);

            if (col != -1 && row != -1) {
                String display = (userType == 'S') ? "예약됨" : r.name;
                String current = (String) table.getValueAt(row, col);
                if (current == null || current.isEmpty()) {
                    table.setValueAt(display, row, col);
                } else if (!current.contains(display)) {
                    table.setValueAt(current + ", " + display, row, col);
                }
            }
        }
    }

    private int getDayColumn(String day) {
        return switch (day) {
            case "월요일" -> 1;
            case "화요일" -> 2;
            case "수요일" -> 3;
            case "목요일" -> 4;
            case "금요일" -> 5;
            default -> -1;
        };
    }

    private int getPeriodRow(String period) {
        return switch (period) {
            case "1교시(09:00~10:00)" -> 0;
            case "2교시(10:00~11:00)" -> 1;
            case "3교시(11:00~12:00)" -> 2;
            case "4교시(12:00~13:00)" -> 3;
            case "5교시(13:00~14:00)" -> 4;
            case "6교시(14:00~15:00)" -> 5;
            case "7교시(15:00~16:00)" -> 6;
            case "8교시(16:00~17:00)" -> 7;
            case "9교시(17:00~18:00)" -> 8;
            default -> -1;
        };
    }
}
