package Controller;

import Model.MembershipModel;
import Model.User;
import Model.UserDAO;
import View.LoginForm;
import View.MembershipView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MembershipController {
    private MembershipView view;
    private MembershipModel model;
    private LoginForm loginForm;
    private UserDAO userDAO;

    public MembershipController(MembershipView view, MembershipModel model, LoginForm loginForm, UserDAO userDAO) {
        this.view = view;
        this.model = model;
        this.loginForm = loginForm;
        this.userDAO = userDAO;

        // 회원가입 버튼 리스너 등록
        this.view.setCustomActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = view.getName();
                String studentId = view.getStudentId();
                String password = view.getPassword();

                if (name.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                    view.showMessage("모든 필드를 입력해주세요.");
                    return;
                }

                if (!isValidId(studentId)) {
                    view.showMessage("아이디는 대문자 S/P/A + 숫자 3개로 구성되어야 합니다.\n예: S123");
                    return;
                }

                if (!isValidPassword(password)) {
                    view.showMessage("비밀번호는 최소 4자리에서 최대 8자리여야 합니다.");
                    return;
                }

                //  중복 아이디 검사
                if (userDAO.isUserIdExists(studentId)) {
                    view.showMessage("이미 존재하는 학번입니다. 다른 학번을 사용해주세요.");
                    return;
                }

                // 모델에 정보 저장
                model.setName(name);
                model.setStudentId(studentId);
                model.setPassword(password);

                // DAO를 이용해 파일에 저장
                User user = new User(studentId, password);
                userDAO.registerUser(user, name);

                view.showMessage("회원가입이 완료되었습니다.");
                view.disposeView();
                loginForm.setVisible(true);
            }
        });
    }

    // 아이디(학번) 유효성 검사
    private boolean isValidId(String userId) {
        return userId.matches("[SPA][0-9]{3}");
    }

    // 비밀번호 유효성 검사 (최소 4자리, 최대 8자리, 문자 포함 가능)
private boolean isValidPassword(String password) {
    return password.length() >= 4 && password.length() <= 8;
}

}
