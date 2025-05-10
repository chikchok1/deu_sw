package Controller;

import View.Executive;
import View.ReservedRoomView;
import View.RoomAdmin;  // ← 이 줄 추가
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
