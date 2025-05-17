package Server;

import Model.User;
import Model.UserDAO;
import java.io.*;
import java.net.*;

public class LoginServer {
    private static boolean running = true;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        try {
            // 모든 IP로부터 수신 가능하게 함
            serverSocket = new ServerSocket(5000, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("서버 시작: 포트 5000");

            while (running) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket, userDAO)).start();
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket, UserDAO userDAO) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String request = in.readLine();
                    System.out.println("수신된 요청: " + request); // 디버그 로그

            if (request == null) return;

            if (request.equalsIgnoreCase("SHUTDOWN")) {
                out.println("SERVER_SHUTTING_DOWN");
                running = false;
                serverSocket.close();
                return;
            }

            if (request.startsWith("LOGIN")) {
                            System.out.println("로그인 요청 처리");

                String[] parts = request.split(",");
                if (parts.length == 3) {
                    boolean valid = userDAO.validateUser(parts[1], parts[2]);
                    if (valid) {
                        String name = userDAO.getUserNameById(parts[1]);
                        out.println("SUCCESS," + name);
                    } else {
                        out.println("FAIL");
                    }
                } else {
                    out.println("INVALID_FORMAT");
                                    System.out.println("REGISTER 요청 포맷 오류");

                }
            } else if (request.startsWith("REGISTER")) {
                            System.out.println("회원가입 요청 처리 시작");

                String[] parts = request.split(",", 4);
                            System.out.println("REGISTER 요청 파싱 결과: " + parts.length + "개");

                if (parts.length == 4) {
                    if (userDAO.isUserIdExists(parts[2])) {
                        out.println("DUPLICATE");
                    } else {
                        userDAO.registerUser(new User(parts[2], parts[3], parts[1]));
                        out.println("SUCCESS");
                    }
                } else {
                    out.println("INVALID_FORMAT");
                }
            } else if (request.startsWith("RESERVE_CLASS") || request.startsWith("RESERVE_LAB")) {
                String[] parts = request.split(",", 8);
                if (parts.length != 8) {
                    out.println("INVALID_FORMAT");
                    return;
                }

                String type = parts[0];
                String fileName = type.equals("RESERVE_CLASS") ? "data/ReserveClass.txt" : "data/ReserveLab.txt";

                String userName = parts[1];
                String room = parts[2];
                String day = parts[3];
                String time = parts[4];
                String purpose = parts[5];
                String userId = parts[6];
                String userType = getUserType(userId);

                if (isDuplicate(fileName, room, day, time)) {
                    out.println("DUPLICATE");
                    return;
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                    writer.write(userName + "," + room + "," + day + "," + time + "," + purpose + "," + userType + ",예약됨");
                    writer.newLine();
                }

                out.println("SUCCESS");
            } else {
                out.println("UNKNOWN_COMMAND");
                            System.out.println("알 수 없는 명령어 수신: " + request);

            }
        } catch (IOException e) {
            System.out.println("클라이언트 처리 오류: " + e.getMessage());
        }
    }

    private static boolean isDuplicate(String fileName, String room, String day, String time) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 4 && tokens[1].equals(room) && tokens[2].equals(day) && tokens[3].equals(time)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String getUserType(String userId) {
        if (userId == null || userId.isEmpty()) return "알 수 없음";
        switch (userId.charAt(0)) {
            case 'S': return "학생";
            case 'P': return "교수";
            case 'A': return "조교";
            default: return "알 수 없음";
        }
    }
}
