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
                // 상태에 따라 강의실 상태 파일 갱신
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

// 현재 강의실 상태 갱신
roomStatusMap.put(roomNumber, status);

// 다시 파일에 쓰기
try (BufferedWriter writer = new BufferedWriter(new FileWriter(statusFile))) {
    for (Map.Entry<String, String> entry : roomStatusMap.entrySet()) {
        writer.write(entry.getKey() + "," + entry.getValue());
        writer.newLine();
    }
} catch (IOException ex) {
    ex.printStackTrace();
}



                // 결과 메시지 출력
                JOptionPane.showMessageDialog(view, "적용되었습니다.");
                // Executive 화면으로 전환
view.dispose(); // 현재 RoomAdmin 창 닫기
            }
        });
    }
}
