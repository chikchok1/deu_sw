/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import Model.MembershipModel;
import View.LoginForm;
import Model.UserDAO;
import ReservationForm.RoomSelect;
import View.MembershipView;

public class LoginController {
    private LoginForm view;
    private UserDAO dao;
    private MembershipView membershipView;  // MembershipView 인스턴스 추가

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
            new RoomSelect().setVisible(true);
        } else {
            view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    private void openMembership() {
    if (membershipView == null || !membershipView.isVisible()) {
        membershipView = new MembershipView();
        MembershipModel membershipModel = new MembershipModel();
        new MembershipController(membershipView, membershipModel, view, dao); // ← 여기!

        view.setVisible(false); // 로그인 창 숨기기
        membershipView.setVisible(true); // 회원가입 창 보여주기
    }
}

}
