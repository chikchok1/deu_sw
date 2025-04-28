package View;

/**
 *
 * @author YangJinWon
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProfessorMainView extends JFrame {
    private JButton logoutButton;

    public ProfessorMainView() {
        setTitle("교수 메인 화면");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logoutButton = new JButton("로그아웃");
        add(logoutButton, BorderLayout.SOUTH);
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }
}
