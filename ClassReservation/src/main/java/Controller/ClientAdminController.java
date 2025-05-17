package Controller;

import View.ClientAdmin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientAdminController {
    private ClientAdmin view;
    private final String profFile = "data/prof.txt";
    private final String userFile = "data/users.txt";

    public ClientAdminController(ClientAdmin view) {
        this.view = view;
        
        loadFileToTable(profFile);
        loadFileToTable(userFile);
        
view.getJButton2().addActionListener(e -> deleteSelectedRow()); // 삭제
    view.getJButton1().addActionListener(e -> updateSelectedRow());  // 수정
    view.getJButton3().addActionListener(e -> goBackToExecutive()); // 이전

    }

    
    
private void loadFileToTable(String filePath) {
    DefaultTableModel model = (DefaultTableModel) view.getTable().getModel();
    File file = new File(filePath);
    
    if (!file.exists()) {
        return; // 파일이 없으면 무시
    }

    List<String[]> professorRows = new ArrayList<>();
    List<String[]> userRows = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length == 3) {
                if (tokens[1].startsWith("P")) {
                    professorRows.add(tokens);
                } else {
                    userRows.add(tokens);
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    // 교수 데이터를 먼저 추가
    for (String[] row : professorRows) {
        model.addRow(row);
    }

    // 사용자 데이터를 다음에 추가
    for (String[] row : userRows) {
        model.addRow(row);
    }
}

    private void deleteSelectedRow() {
        JTable table = view.getTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "삭제할 행을 선택하세요.");
            return;
        }

        String name = (String) model.getValueAt(selectedRow, 0);
        String id = (String) model.getValueAt(selectedRow, 1);
        String pw = (String) model.getValueAt(selectedRow, 2);

        model.removeRow(selectedRow); // 테이블에서 제거

        String filePath = id.startsWith("P") ? profFile : userFile;

        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals(name + "," + id + "," + pw)) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateSelectedRow() {
    JTable table = view.getTable();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    int selectedRow = table.getSelectedRow();

    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(null, "수정할 행을 선택하세요.");
        return;
    }

    String oldName = (String) model.getValueAt(selectedRow, 0);
    String id = (String) model.getValueAt(selectedRow, 1);
    String oldPw = (String) model.getValueAt(selectedRow, 2);

    // 새 이름, 새 비번 입력 받기
    String newName = JOptionPane.showInputDialog(null, "새 이름을 입력하세요:", oldName);
    if (newName == null || newName.trim().isEmpty()) return;

    String newPw = JOptionPane.showInputDialog(null, "새 비밀번호를 입력하세요:", oldPw);
    if (newPw == null || newPw.trim().isEmpty()) return;

    // 파일 결정
    String filePath = id.startsWith("P") ? profFile : userFile;

    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.equals(oldName + "," + id + "," + oldPw)) {
                lines.add(newName + "," + id + "," + newPw); // 수정된 내용
            } else {
                lines.add(line);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        return;
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
        for (String line : lines) {
            bw.write(line);
            bw.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
        return;
    }

    // 테이블 갱신
    model.setValueAt(newName, selectedRow, 0);
    model.setValueAt(newPw, selectedRow, 2);

    JOptionPane.showMessageDialog(null, "수정이 완료되었습니다.");
}

    private void goBackToExecutive() {
    view.dispose(); // 현재 창 닫기
    View.Executive executiveView = new View.Executive(); // Executive 뷰 생성
    new Controller.ExecutiveController(executiveView);   // Executive 컨트롤러 연결
    executiveView.setVisible(true); // 창 띄우기
}


}
