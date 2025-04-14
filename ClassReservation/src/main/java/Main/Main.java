/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Main;

import View.RoomSelect;
import Controller.RoomSelectController;

public class Main {
    public static void main(String[] args) {
        RoomSelect view = new RoomSelect();
        RoomSelectController controller = new RoomSelectController(view);
        view.setVisible(true);
    }
}

