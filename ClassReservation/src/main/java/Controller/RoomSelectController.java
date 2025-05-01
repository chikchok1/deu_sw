package Controller;

import Model.UserDAO;
import View.RoomSelect;
import View.ReservClassView;
import View.ReservLabView;
import View.LoginForm;
import View.ReservedRoomView;

public class RoomSelectController {

    private RoomSelect view;

    public RoomSelectController(RoomSelect view) {
        this.view = view;

        // 버튼 클릭 시 동작 연결
        this.view.setClassButtonActionListener(e -> openReservClass());
        this.view.setLabButtonActionListener(e -> openReservLab());
        this.view.setViewReservedActionListener(e -> openReservedClassRoom());
        this.view.setLogOutButtonActionListener(e -> handleLogout());
    }

    private void openReservClass() {
        ReservClassView reservClassView = new ReservClassView();
        new ReservClassController(reservClassView); // 컨트롤러 생성 및 뷰 연결
        reservClassView.setVisible(true); // 화면 활성화
        view.dispose(); // 현재 창 닫기
    }

    private void openReservLab() {
        ReservLabView reservLabView = new ReservLabView();
        new ReservLabController(reservLabView); // 컨트롤러 연결
        reservLabView.setVisible(true);         // 화면 활성화
        view.dispose();
    }

    private void openReservedClassRoom() {
        new ReservedRoomView().setVisible(true);
        view.dispose();
    }

    private void handleLogout() {
        view.dispose(); // 현재 창 닫기

        // 다시 로그인 폼 띄우기
        LoginForm loginForm = new LoginForm();
        UserDAO dao = new UserDAO();
        new LoginController(loginForm, dao);

        loginForm.setVisible(true);
    }
}
