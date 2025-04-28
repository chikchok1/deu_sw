package Controller;

import Model.MembershipModel;
import Model.UserDAO;
import Model.Session; // 추가: Session 가져오기
import View.*;

public class LoginController {
    private LoginForm view;
    private UserDAO dao;
    private MembershipView membershipView;

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

        // 로그인 성공하면 세션에 ID, 이름 저장
        Session.setLoggedInUserId(id);
        String userName = dao.getUserNameById(id);
        Session.setLoggedInUserName(userName);

        view.dispose();
        openUserMainView(id.charAt(0));  // ID 첫 글자만 넘겨서 분기
    } else {
        view.showMessage("아이디 또는 비밀번호가 틀렸습니다.");
    }
}


    private void openUserMainView(char userType) {
        switch (userType) {
            case 'S':
                RoomSelect roomSelect = new RoomSelect();
                new RoomSelectController(roomSelect);
                roomSelect.setVisible(true);
                break;
            case 'P':
                ProfessorMainView professorMainView = new ProfessorMainView();
                new ProfessorMainController(professorMainView);
                professorMainView.setVisible(true);
                break;
            case 'A':
                AssistantMainView assistantMainView = new AssistantMainView();
                new AssistantMainController(assistantMainView);
                assistantMainView.setVisible(true);
                break;
            default:
                System.out.println("알 수 없는 사용자 타입: " + userType);
                break;
        }
    }

    private void openMembership() {
        if (membershipView == null || !membershipView.isVisible()) {
            membershipView = new MembershipView();
            MembershipModel membershipModel = new MembershipModel();

            new MembershipController(membershipView, membershipModel, view, dao);

            view.setVisible(false);
            membershipView.setVisible(true);
        }
    }
}
