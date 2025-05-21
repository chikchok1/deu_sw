package Server;

import Model.User;
import Model.UserDAO;
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginServer {

    private static boolean running = true;
    private static ServerSocket serverSocket;
    private static final int MAX_CLIENTS = 3;
    private static final AtomicInteger currentClients = new AtomicInteger(0);

    // 중복 로그인 방지를 위한 사용자 목록 (UserID -> Socket)
    private static final ConcurrentHashMap<String, Socket> loggedInUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        try {
            serverSocket = new ServerSocket(5000, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("서버 시작: 포트 5000");

            while (running) {
                Socket socket = serverSocket.accept();

                new Thread(() -> {
                    String userId = null;

                    try {
                        userId = handleClient(socket, userDAO);
                    } finally {
                        if (userId != null) {
                            synchronized (LoginServer.class) {
                                loggedInUsers.remove(userId);
                                currentClients.decrementAndGet();
                                System.out.println(userId + " 로그아웃 처리됨");
                                System.out.println("현재 로그인 중인 사용자: " + loggedInUsers.keySet());
                                System.out.println("현재 접속자 수: " + currentClients.get());
                            }
                        }

                        try {
                            socket.close();
                        } catch (IOException ignored) {}
                    }
                }).start();
            }
        } catch (IOException e) {
            System.err.println("서버 오류: " + e.getMessage());
        }
    }

    private static String handleClient(Socket socket, UserDAO userDAO) {
        String loggedInUserId = null;

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String request = in.readLine();
            System.out.println("수신된 요청: " + request);

            if (request == null) return null;

            if (request.equalsIgnoreCase("SHUTDOWN")) {
                out.println("SERVER_SHUTTING_DOWN");
                running = false;
                serverSocket.close();
                return null;
            }

            if (request.startsWith("LOGIN")) {
                String[] parts = request.split(",");
                if (parts.length == 3) {
                    String userId = parts[1];
                    String password = parts[2];

                    synchronized (LoginServer.class) {
                        if (currentClients.get() >= MAX_CLIENTS) {
                            out.println("SERVER_BUSY");
                            return null;
                        }

                        if (loggedInUsers.containsKey(userId)) {
                            out.println("ALREADY_LOGGED_IN");
                            return null;
                        }

                        boolean valid = userDAO.validateUser(userId, password);
                        if (valid) {
                            loggedInUserId = userId;
                            loggedInUsers.put(userId, socket);
                            currentClients.incrementAndGet();

                            String name = userDAO.getUserNameById(userId);
                            out.println("SUCCESS," + name);
                            System.out.println(userId + " 로그인 성공");
                            System.out.println("현재 로그인 중인 사용자: " + loggedInUsers.keySet());
                            System.out.println("현재 접속자 수: " + currentClients.get());
                        } else {
                            out.println("FAIL");
                            return null;
                        }
                    }
                } else {
                    out.println("INVALID_FORMAT");
                    return null;
                }

            } else if (request.startsWith("REGISTER")) {
                String[] parts = request.split(",", 4);
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

            } else {
                out.println("UNKNOWN_COMMAND");
            }

            // 로그인 이후 클라이언트와 통신 유지
            while (true) {
                String msg = in.readLine();
                if (msg == null || msg.equalsIgnoreCase("EXIT")) {
                    out.println("LOGOUT_SUCCESS");
                    break;
                }

                System.out.println("[" + loggedInUserId + "] 메시지 수신: " + msg);
            }

        } catch (IOException e) {
            System.out.println("클라이언트 처리 오류: " + e.getMessage());
        }

        return loggedInUserId;
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
        if (userId == null || userId.isEmpty()) {
            return "알 수 없음";
        }
        return switch (userId.charAt(0)) {
            case 'S' -> "학생";
            case 'P' -> "교수";
            case 'A' -> "조교";
            default -> "알 수 없음";
        };
    }
}
