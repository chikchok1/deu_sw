package Model;

import java.io.*;

public class UserDAO {

    private static final String DATA_FOLDER = "data";
    private static final String USER_FILE = DATA_FOLDER + "/users.txt";
    private static final String PROF_FILE = DATA_FOLDER + "/prof.txt";
    private static final String ASSISTANT_FILE = DATA_FOLDER + "/assistant.txt";
    private static final String RESERVE_CLASS_FILE = DATA_FOLDER + "/ReserveClass.txt";
    private static final String RESERVE_LAB_FILE = DATA_FOLDER + "/ReserveLab.txt";
    private static final String CHANGE_REQUEST = DATA_FOLDER + "/ChangeRequest.txt";
        private static final String ROOM_STATUS = DATA_FOLDER + "/RoomStatus.txt";


    public UserDAO() {
        new File(DATA_FOLDER).mkdirs();
        createFileIfNotExists(USER_FILE);
        createFileIfNotExists(PROF_FILE);
        createFileIfNotExists(ASSISTANT_FILE);
        createFileIfNotExists(RESERVE_CLASS_FILE);
        createFileIfNotExists(RESERVE_LAB_FILE);
        createFileIfNotExists(CHANGE_REQUEST);
                createFileIfNotExists(ROOM_STATUS);


    }

    private void createFileIfNotExists(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();  // 상위 폴더도 확인
                file.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("파일 생성 오류: " + e.getMessage());
        }
    }

    public synchronized boolean validateUser(String userId, String password) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3 && tokens[1].equals(userId) && tokens[2].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("읽기 오류: " + e.getMessage());
        }
        return false;
    }

    public synchronized boolean isUserIdExists(String userId) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2 && tokens[1].equals(userId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("읽기 오류: " + e.getMessage());
        }
        return false;
    }

    public synchronized void registerUser(User user) {
        String fileName = getFileNameByUserId(user.getUserId());
        if (fileName == null) {
            System.out.println("잘못된 형식의 ID입니다: " + user.getUserId());
            return;
        }

        if (isUserIdExists(user.getUserId())) {
            System.out.println("이미 존재하는 ID입니다: " + user.getUserId());
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(user.getName() + "," + user.getUserId() + "," + user.getPassword());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("쓰기 오류: " + e.getMessage());
        }
    }

    public synchronized String getUserNameById(String userId) {
        String fileName = getFileNameByUserId(userId);
        if (fileName == null) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 3 && tokens[1].equals(userId)) {
                    return tokens[0];
                }
            }
        } catch (IOException e) {
            System.out.println("읽기 오류: " + e.getMessage());
        }
        return null;
    }

    // [추가됨] 접근 권한 판단 함수
    public boolean authorizeAccess(String userId) {
        if (userId == null || userId.isEmpty()) {
            System.out.println("유효하지 않은 사용자 ID입니다.");
            return false;
        }

        char role = userId.charAt(0);
        return role == 'P' || role == 'A'; // 교수 또는 조교만 true 반환
    }

    private String getFileNameByUserId(String userId) {
        if (userId.startsWith("S")) {
            return USER_FILE;
        }
        if (userId.startsWith("P")) {
            return PROF_FILE;
        }
        if (userId.startsWith("A")) {
            return ASSISTANT_FILE;
        }
        return null;
    }
    public synchronized String getUserIdByName(String name) {
    String[] files = {USER_FILE, PROF_FILE, ASSISTANT_FILE};

    for (String fileName : files) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2 && tokens[0].trim().equals(name)) {
                    return tokens[1].trim(); // 이름이 일치하면 해당 ID 반환
                }
            }
        } catch (IOException e) {
            System.out.println("getUserIdByName 읽기 오류: " + e.getMessage());
        }
    }

    return name; // 이름이 없으면 그대로 반환 (임시 fallback)
}
    public static String findIdByName(String name) {
        File[] files = {
            new File("data/users.txt"),
            new File("data/professors.txt"),
            new File("data/assistants.txt")
        };

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2 && parts[1].trim().equals(name.trim())) {
                        return parts[0].trim(); // ID 반환
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "알 수 없음"; // 없을 경우
    }
}



/*학번 유효성 검사
    public boolean isValidId(String userId) {
        return userId.matches("[SPA][0-9]{3}"); // S123, P456, A789 등
    }

    // 비밀번호 유효성 검사
    public boolean isValidPassword(String password) {
        return password.length() >= 4 && password.length() <= 8;
    }
}
 */
