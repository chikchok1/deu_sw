package Controller;

import View.Executive;
import View.ReservedRoomView;
import View.LoginForm;
import View.RoomAdmin;  // ← 이 줄 추가
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import Model.UserDAO;
import View.ClientAdmin;
import Controller.RoomAdminController;


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
// [4] "고객 관리" 버튼 (jButton5)
this.executive.getJButton5().addActionListener(new ActionListener() {
    
    @Override
    
    public void actionPerformed(ActionEvent e) {
        openClientAdminView();
    }
});
// [5] "예약승인" 버튼 (jButton6)
this.executive.getJButton6().addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        openReservationApprovalView();
    }
});


    }

private void logout() {
    executive.setVisible(false);  // 보조적으로 창 숨기기
    executive.dispose();          // 창 닫기
    LoginForm loginForm = new LoginForm();
    UserDAO dao = new UserDAO();
    new LoginController(loginForm, dao);
    loginForm.setVisible(true);
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
    
    private void openClientAdminView() {
    ClientAdmin clientAdmin = new ClientAdmin(); // 뷰 인스턴스 생성
    new ClientAdminController(clientAdmin);      // 컨트롤러 연결
    clientAdmin.setVisible(true);                // 화면 표시
}
    private void openReservationApprovalView() {
    executive.dispose(); // 현재 Executive 창 닫기
    View.ClassroomReservationApproval approvalView = new View.ClassroomReservationApproval();
    new Controller.ClassroomReservationApprovalController(approvalView); // 컨트롤러 연결
    approvalView.setVisible(true); // 화면 보이기
}

}
