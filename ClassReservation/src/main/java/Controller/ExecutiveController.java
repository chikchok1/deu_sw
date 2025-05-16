package Controller;

import View.Executive;
import View.ReservedRoomView;
import View.LoginForm;
import View.RoomAdmin;  // ← 이 줄 추가
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Model.UserDAO;
import View.ClassroomReservationApproval;
import Controller.ClassroomReservationApprovalController;

public class ExecutiveController {

    private Executive executive;

    public ExecutiveController(Executive executive) {
        this.executive = executive;
        
        // [1] "예약 확인" 버튼
        this.executive.getViewReservedButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReservedRoomView();
            }
        });

        // [2] "강의실 및 실습실 관리" 버튼 (jButton2)
        this.executive.getJButton2().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRoomAdminView();
            }   
        });
        // [3] "로그아웃" 버튼
this.executive.getJButton3().addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        logout();
    }
});
 this.executive.getUserManageButton().addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            openReservationApprovalView(); // 승인 화면으로 이동
        }
    });
    }

private void logout() {
    executive.dispose();
    LoginForm loginForm = new LoginForm();
    UserDAO dao = new UserDAO(); // DB 접근 객체 (이미 너가 LoginController에 넘겨주고 있음)
    new LoginController(loginForm, dao); // ✅ 컨트롤러 연결
    loginForm.setVisible(true);
}

private void openReservationApprovalView() {
    ClassroomReservationApproval approvalView = new ClassroomReservationApproval();
    new ClassroomReservationApprovalController(approvalView); // 컨트롤러 연결
    approvalView.setVisible(true);  // 승인 화면 보여주기
    executive.dispose();            // 기존 Executive 창 닫기 
}

    
    private void openReservedRoomView() {
        ReservedRoomView reservedView = new ReservedRoomView();
        new ReservedRoomController(reservedView); // 컨트롤러 연결
        reservedView.setVisible(true);
    }

    private void openRoomAdminView() {
        RoomAdmin roomAdmin = new RoomAdmin();
        new RoomAdminController(roomAdmin); // 컨트롤러 연결 (있다면)
        roomAdmin.setVisible(true);
    }
}
