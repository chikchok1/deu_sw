/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author Sunghoon
 */

import View.RoomSelect;
import View.ReservClass;
import View.ReservLab;
import View.ReservedClassRoom;
import deu.CRS.Login.Login;
import java.awt.event.ActionListener;

public class RoomSelectController {
    private RoomSelect view;

    public RoomSelectController(RoomSelect view) {
        this.view = view;
        initController();
    }

    private void initController() {
        // 기존 RoomSelect에 있던 ActionListener 처리는 모두 이곳에서 설정
        view.getClassButton().addActionListener(e -> openReservClass());
        view.getLabButton().addActionListener(e -> openReservLab());
        
    }

    private void openReservClass() {
        view.setVisible(false);
        new ReservClass().setVisible(true);
    }

    private void openReservLab() {
        view.setVisible(false);
        new ReservLab().setVisible(true);
    }
}

