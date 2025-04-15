package Controller;

import Model.MembershipModel;
import View.LoginForm;
import Model.UserDAO;
import View.RoomSelect;
import View.MembershipView;

public class LoginController {
    private LoginForm view;
    private UserDAO dao;
    private MembershipView membershipView;  // 회원가입 뷰

    public LoginController(LoginForm view, UserDAO dao) {
        this.view = view;
        this.dao = dao;

        this.view.addLoginListener(e -> handleLogin());
        this.view.addJoinListener(e -> openMembership());
    }

    private void handleLogin() {
        String id = view.getUserId();
        String password = view.getPassword();

        if (dao.validateUser(id, password)) {
            view.showMessage("로그인 성공!");
            view.dispose();

            // RoomSelect 뷰 + 컨트롤러 연결
            RoomSelect roomSelect = new RoomSelect();
            new RoomSelectController(roomSelect);  // 컨트롤러 연결
            roomSelect.setVisible(true);
        } else {
            view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    private void openMembership() {
        if (membershipView == null || !membershipView.isVisible()) {
            membershipView = new MembershipView();
            MembershipModel membershipModel = new MembershipModel();

            // 회원가입 컨트롤러 연결
            new MembershipController(membershipView, membershipModel, view, dao);

            view.setVisible(false);
            membershipView.setVisible(true);
        }
    }
}
