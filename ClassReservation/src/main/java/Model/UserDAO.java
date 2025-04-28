package Model;

import java.io.*;
import javax.swing.JOptionPane;

public class UserDAO {
    private static final String USER_FILE = "users.txt";
    private static final String PROF_FILE = "prof.txt";
    private static final String ASSISTANT_FILE = "assistant.txt";

    public UserDAO() {
        createFileIfNotExists(USER_FILE);
        createFileIfNotExists(PROF_FILE);
        createFileIfNotExists(ASSISTANT_FILE);
    }

    // 파일이 없으면 생성
    private void createFileIfNotExists(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                if (fileName.equals(USER_FILE)) {
                    writer.write("admin,S001,1234567"); // 포맷 변경: S+3자리, 비번 7자리
                    writer.newLine();
                }
                System.out.println(fileName + " 파일이 생성되었습니다.");
            } catch (IOException e) {
                System.out.println("파일 생성 오류: " + e.getMessage());
            }
        }
    }

    // 로그인 검증
    public boolean validateUser(String userId, String password) {
        if (!isValidId(userId)) {
            System.out.println("잘못된 학번 형식입니다: " + userId);
            return false;
        }

        if (password.length() != 7) {
            System.out.println("비밀번호는 7자리여야 합니다: " + password);
            return false;
        }

        String fileName = getFileNameByUserId(userId);
        if (fileName == null) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3) {
                    String storedId = tokens[1].trim();
                    String storedPassword = tokens[2].trim();

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

    // 🔥 userId로 이름 가져오기 추가
    public String getUserNameById(String userId) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3) {
                    String storedId = tokens[1].trim();
                    if (storedId.equals(userId)) {
                        return tokens[0].trim(); // 이름 반환
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("파일 읽기 오류: " + e.getMessage());
        }

        return null;
    }

    // 회원가입
    public void registerUser(User user, String name) {
        String userId = user.getUserId();

        if (!isValidId(userId)) {
            JOptionPane.showMessageDialog(null, "잘못된 학번 형식입니다: " + userId);
            return;
        }

        if (user.getPassword().length() != 7) {
            JOptionPane.showMessageDialog(null, "비밀번호는 7자리여야 합니다: " + user.getPassword());
            return;
        }

        String fileName = getFileNameByUserId(userId);
        if (fileName == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(name + "," + userId + "," + user.getPassword());
            writer.newLine();
            System.out.println("새로운 사용자 정보가 저장되었습니다: " + userId + " (" + fileName + ")");
        } catch (IOException e) {
            System.out.println("파일 쓰기 오류: " + e.getMessage());
        }
    }

    // (파일명 구하는 메서드)
    private String getFileNameByUserId(String userId) {
        String firstLetter = userId.substring(0, 1);

        switch (firstLetter) {
            case "S":
                return USER_FILE;
            case "P":
                return PROF_FILE;
            case "A":
                return ASSISTANT_FILE;
            default:
                System.out.println("학번이 S, P, A로 시작하지 않습니다: " + userId);
                return null;
        }
    }

    // 아이디(학번) 유효성 검사
    private boolean isValidId(String userId) {
        return userId.matches("[SPA][0-9]{3}"); // 문자 1개 + 숫자 3자리
    }
}
