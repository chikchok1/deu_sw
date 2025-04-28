package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AssistantMainView extends JFrame {
     private JButton logoutButton;
     
    public AssistantMainView() {
        setTitle("조교 메인 화면");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        logoutButton = new JButton("로그아웃");
        add(logoutButton, BorderLayout.SOUTH);
    }
      public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }
}
