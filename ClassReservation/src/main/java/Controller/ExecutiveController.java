/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

import View.Executive;
import View.ReservedRoomView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExecutiveController {

    private Executive executive;

    public ExecutiveController(Executive executive) {
        this.executive = executive;

        // 버튼에 이벤트 리스너 연결
        this.executive.getViewReservedButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReservedRoomView();
            }
        });
        
    }
    
    

    private void openReservedRoomView() {
    ReservedRoomView reservedView = new ReservedRoomView();
    new ReservedRoomController(reservedView); // 컨트롤러 연결 추가
    reservedView.setVisible(true);
}

}

