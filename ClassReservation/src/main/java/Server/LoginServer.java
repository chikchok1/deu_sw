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
            serverSocket = new ServerSocket(5000);
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
            if (request == null) return;

            if (request.equalsIgnoreCase("SHUTDOWN")) {
                out.println("SERVER_SHUTTING_DOWN");
                running = false;
                serverSocket.close();
                return;
            }

            if (request.startsWith("LOGIN")) {
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
        } catch (IOException e) {
            System.out.println("클라이언트 처리 오류: " + e.getMessage());
        }
    }
}

