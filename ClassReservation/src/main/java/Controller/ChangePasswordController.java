/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author YangJinWon
 */
import Model.Session;
import Model.UserDAO;
import View.ChangePasswordView;
import View.RoomSelect;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.GraphicsEnvironment;

public class ChangePasswordController {

    private final ChangePasswordView view;
    private final UserDAO userDAO;

    public ChangePasswordController(ChangePasswordView view) {
        this.view = view;
        this.userDAO = new UserDAO();

        // View에서 버튼 클릭 이벤트를 등록
        view.setSaveButtonListener(e -> changePassword());
    }

    public void changePassword() {
        String currentPassword = view.getPresentPassword().trim();
        String newPassword = view.getChangePassword().trim();
        String userId = Session.getLoggedInUserId();

        if (currentPassword.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(view, "모든 필드를 입력해주세요.");
            return;
        }

        String filePath = getFileNameByUserId(userId);
        if (filePath == null) {
            JOptionPane.showMessageDialog(view, "유효하지 않은 사용자입니다.");
            return;
        }

        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> lines = new ArrayList<>();
            boolean updated = false;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[1].equals(userId)) {
                    if (!parts[2].equals(currentPassword)) {
                        JOptionPane.showMessageDialog(view, "현재 비밀번호가 일치하지 않습니다.");
                        reader.close();
                        return;
                    }
                    line = parts[0] + "," + parts[1] + "," + newPassword;
                    updated = true;
                }
                lines.add(line);
            }
            reader.close();

            if (!updated) {
                JOptionPane.showMessageDialog(view, "사용자 정보를 찾을 수 없습니다.");
                return;
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String l : lines) {
                writer.write(l);
                writer.newLine();
            }
            writer.close();

            JOptionPane.showMessageDialog(view, "비밀번호가 성공적으로 변경되었습니다.");

            RoomSelect roomSelect = new RoomSelect();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
            view.dispose();

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "오류 발생: " + e.getMessage());
        }
        
       // [minju] GitHub Actions에서 창 띄우면 에러 발생해서, 화면 있을 때만 실행되게 분기 추가
        if (!GraphicsEnvironment.isHeadless()) {
            // 비밀번호 변경 완료 후 강의실 선택 화면으로 이동
            RoomSelect roomSelect = new RoomSelect();
            new RoomSelectController(roomSelect);
            roomSelect.setVisible(true);
            
            // 현재 비밀번호 변경 창 닫기
            view.dispose();
        }
    }

    protected String getFileNameByUserId(String userId) {
        if (userId == null || userId.length() == 0) return null;

        switch (userId.charAt(0)) {
            case 'S':
                return "data/users.txt";
            case 'P':
                return "data/prof.txt";
            case 'A':
                return "data/assistant.txt";
            default:
                return null;
        }
    }
}

