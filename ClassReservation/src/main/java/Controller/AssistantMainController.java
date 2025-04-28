package Controller;

import Model.UserDAO;
import View.AssistantMainView;
import View.LoginForm;

public class AssistantMainController {
    private AssistantMainView view;

    public AssistantMainController(AssistantMainView view) {
        this.view = view;

        this.view.addLogoutListener(e -> handleLogout());
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
