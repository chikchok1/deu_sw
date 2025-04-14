/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

import java.io.*;

public class UserDAO {
    private static final String FILE_PATH = "users.txt"; // 사용자 정보 텍스트 파일

    public UserDAO() {
        createFileIfNotExists();
    }

    // users.txt 파일이 없으면 생성
    private void createFileIfNotExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // 기본 관리자 계정 추가 (선택사항)
                writer.write("admin,1234");
                writer.newLine();
                System.out.println("users.txt 파일이 생성되었습니다 (기본 계정 포함)");
            } catch (IOException e) {
                System.out.println("파일 생성 오류: " + e.getMessage());
            }
        }
    }

    // 로그인 검증
    public boolean validateUser(String userId, String password) {
    try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
        String line;

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(",");
            if (tokens.length >= 3) {
                String storedId = tokens[1].trim();         // 학번 (아이디)
                String storedPassword = tokens[2].trim();   // 비밀번호

                if (storedId.equals(userId) && storedPassword.equals(password)) {
                    return true;
                }
            }
        }
    } catch (IOException e) {
        System.out.println("파일 읽기 오류: " + e.getMessage());
    }

    return false;
}

    // 사용자 정보 파일에 저장 (회원가입)
   // 예: 이름, 학번, 비밀번호 순으로 저장
public void registerUser(User user, String name) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
        writer.write(name + "," + user.getUserId() + "," + user.getPassword());
        writer.newLine();
        System.out.println("새로운 사용자 정보가 저장되었습니다: " + user.getUserId());
    } catch (IOException e) {
        System.out.println("파일 쓰기 오류: " + e.getMessage());
    }
}
}
