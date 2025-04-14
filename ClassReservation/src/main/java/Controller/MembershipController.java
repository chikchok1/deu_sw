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

        // view의 setCustomActionListener 메서드를 사용하여 ActionListener 등록
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

        //  model에 값 설정
        model.setName(name);
        model.setStudentId(studentId);
        model.setPassword(password);

        // 회원 정보 저장
        boolean success = model.register();
        
        if (success) {
            view.showMessage("회원가입이 완료되었습니다.");
            view.disposeView();
            loginForm.setVisible(true);
        } else {
            view.showMessage("회원가입에 실패했습니다. 다시 시도해주세요.");
        }
    }
});

    }
}
