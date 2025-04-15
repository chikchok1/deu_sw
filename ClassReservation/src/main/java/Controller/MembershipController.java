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

                // 모델에 정보 저장 (화면상 데이터 유지 목적)
                model.setName(name);
                model.setStudentId(studentId);
                model.setPassword(password);

                // DAO를 이용해 파일에 저장
                User user = new User(studentId, password);
                userDAO.registerUser(user, name);

                // 완료 메시지 및 화면 전환
                view.showMessage("회원가입이 완료되었습니다.");
                view.disposeView();
                loginForm.setVisible(true);
            }
        });
    }
}
