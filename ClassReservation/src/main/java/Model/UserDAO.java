package Model;

import java.io.*;
import javax.swing.JOptionPane;

public class UserDAO {

    private static final String DATA_FOLDER = "data"; // 전용 폴더
    private static final String USER_FILE = DATA_FOLDER + "/users.txt";
    private static final String PROF_FILE = DATA_FOLDER + "/prof.txt";
    private static final String ASSISTANT_FILE = DATA_FOLDER + "/assistant.txt";
    private static final String RESERVE_FILE = DATA_FOLDER + "/ReserveClass.txt"; 

    public UserDAO() {
        createDataFolderIfNotExists();
        createFileIfNotExists(USER_FILE);
        createFileIfNotExists(PROF_FILE);
        createFileIfNotExists(ASSISTANT_FILE);
        createFileIfNotExists(RESERVE_FILE); 
    }

    //  data 폴더 없으면 생성
    private void createDataFolderIfNotExists() {
        File folder = new File(DATA_FOLDER);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("data 폴더가 생성되었습니다.");
            }
        }
    }

    // 파일이 없으면 생성 (내용 없이 빈 파일로)
    private void createFileIfNotExists(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println(fileName + " 파일이 생성되었습니다.");
                }
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

        if (password.length() < 4 || password.length() > 8) {
            System.out.println("비밀번호는 4자리 이상 8자리 이하이어야 합니다: " + password);
            return false;
        }

        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return false;
        }

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

    // userId가 이미 존재하는지 확인
    public boolean isUserIdExists(String userId) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2) {
                    String storedId = tokens[1].trim();
                    if (storedId.equals(userId)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("파일 읽기 오류: " + e.getMessage());
        }

        return false;
    }

    // 이름 가져오기
    public String getUserNameById(String userId) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3) {
                    String storedId = tokens[1].trim();
                    if (storedId.equals(userId)) {
                        return tokens[0].trim();
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

        if (user.getPassword().length() < 4 || user.getPassword().length() > 8) {
            JOptionPane.showMessageDialog(null, "비밀번호는 4자리 이상 8자리 이하로 입력해주세요.");
            return;
        }

        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(name + "," + userId + "," + user.getPassword());
            writer.newLine();
            System.out.println("새로운 사용자 정보가 저장되었습니다: " + userId + " (" + fileName + ")");
        } catch (IOException e) {
            System.out.println("파일 쓰기 오류: " + e.getMessage());
        }
    }

    // 파일명 반환
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

    // 학번 유효성 검사
    private boolean isValidId(String userId) {
        return userId.matches("[SPA][0-9]{3}"); // 문자 + 숫자 3자리
    }
}
