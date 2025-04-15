package Controller;

import View.RoomSelect;
import View.ReservClass;
import View.ReservLab;
import View.ReservedClassRoom;
import deu.CRS.Login.Login;

public class RoomSelectController {
    private RoomSelect view;

    public RoomSelectController(RoomSelect view) {
        this.view = view;

        // 버튼 클릭 시 동작 연결
        this.view.setClassButtonActionListener(e -> openReservClass());
        this.view.setLabButtonActionListener(e -> openReservLab());
        this.view.setViewReservedActionListener(e -> openReservedClassRoom());
        this.view.setLogOutButtonActionListener(e -> logOut());
    }

    private void openReservClass() {
        new ReservClass().setVisible(true);
        view.dispose(); // 현재 창 닫기
    }

    private void openReservLab() {
        new ReservLab().setVisible(true);
        view.dispose();
    }

    private void openReservedClassRoom() {
        new ReservedClassRoom().setVisible(true);
        view.dispose();
    }

    private void logOut() {
        new Login().setVisible(true);
        view.dispose();
    }
}
