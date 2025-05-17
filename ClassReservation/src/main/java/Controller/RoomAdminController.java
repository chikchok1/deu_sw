package Controller;

import View.RoomAdmin;
import View.Executive;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class RoomAdminController {

    private RoomAdmin view;
    
   
    
    public RoomAdminController(RoomAdmin view) {
        this.view = view;
        initListeners();
        view.getJButton2().addActionListener(e -> goBackToExecutive()); // 이전
    }

    private void initListeners() {
         for (ActionListener al : view.getConfirmButton().getActionListeners()) {
        view.getConfirmButton().removeActionListener(al);
        }
        view.getConfirmButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomNumber = view.getRoomNumberField().getText().trim();
                String status = (String) view.getStatusComboBox().getSelectedItem();

                if (roomNumber.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "강의실 번호를 입력하세요.");
                    return;
                }

// 상태에 따라 강의실 상태 파일 갱신
File statusFile = new File("data/RoomStatus.txt");
Map<String, String> roomStatusMap = new LinkedHashMap<>();

// 기존 데이터 읽기
if (statusFile.exists()) {
    try (BufferedReader reader = new BufferedReader(new FileReader(statusFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length >= 2) {
                String room = tokens[0].trim();
                String stat = tokens[1].trim();
                // 덮어쓰기 위해 같은 강의실이 있을 경우 무시
                if (!room.equals(roomNumber)) {
                    roomStatusMap.put(room, stat);
                }
            }
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}
roomStatusMap.put(roomNumber, status);
                // 강의실 상태 업데이트
                if (status.equals("사용가능")) { // ← 주의: 띄어쓰기 제거
    roomStatusMap.remove(roomNumber);
} else if (status.equals("사용불가")) { // ← 주의: 띄어쓰기 제거
    roomStatusMap.put(roomNumber, "사용불가");
}


                // 파일에 다시 쓰기
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(statusFile))) {
                    for (Map.Entry<String, String> entry : roomStatusMap.entrySet()) {
                        writer.write(entry.getKey() + "," + entry.getValue());
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // 완료 메시지 및 화면 전환
                JOptionPane.showMessageDialog(view, "적용되었습니다.");
                view.dispose();
            }
        });
    }
 
        private void goBackToExecutive() {
    view.dispose(); // 현재 창 닫기
    View.Executive executiveView = new View.Executive(); // Executive 뷰 생성
    new Controller.ExecutiveController(executiveView);   // Executive 컨트롤러 연결
    executiveView.setVisible(true); // 창 띄우기
}
    
}
