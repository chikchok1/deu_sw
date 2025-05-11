package Controller;

import View.RoomAdmin;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class RoomAdminController {

    private RoomAdmin view;

    public RoomAdminController(RoomAdmin view) {
        this.view = view;
        initListeners();  // 이벤트 리스너 초기화
    }

    private void initListeners() {
        // 확인 버튼 클릭 이벤트 리스너 등록
        view.getConfirmButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 사용자 입력 가져오기
                String roomNumber = view.getRoomNumberField().getText().trim();
                String status = (String) view.getStatusComboBox().getSelectedItem();
                String otherDescription = view.getOtherDescriptionField().getText().trim();

                // 불가능한 객실 정보 저장 파일
                File file = new File("data/UnavailableRooms.txt");
                List<String> rooms = new ArrayList<>();

                // 파일에 이미 존재하는 데이터 읽기
                if (file.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            rooms.add(line.trim());
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                // 상태에 따라 객실 목록 갱신
                if (status.equals("사용불가")) {
                    if (!rooms.contains(roomNumber)) {
                        rooms.add(roomNumber);
                    }
                } else if (status.equals("사용가능")) {
                    rooms.remove(roomNumber);
                }

                // 갱신된 데이터를 파일에 저장
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String room : rooms) {
                        writer.write(room);
                        writer.newLine();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // 결과 메시지 출력
                JOptionPane.showMessageDialog(view, "적용되었습니다.");
            }
        });
    }
}
