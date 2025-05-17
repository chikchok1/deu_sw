package Controller;

import Model.MembershipModel;
import Model.UserDAO;
import Model.Session; // 추가: Session 가져오기
import View.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import utils.ConfigLoader;

public class LoginController {

    private LoginForm view;
    private UserDAO dao;
    private MembershipView membershipView;

    public LoginController(LoginForm view, UserDAO dao) {
        this.view = view;
        this.dao = dao;

        this.view.addLoginListener(e -> handleLogin());
        this.view.addJoinListener(e -> openMembership());
    }
 // GitHub Actions 동작 확인용 커밋

/**
 * [테스트 용도로만 public 처리함]
 */
public void handleLogin() {
    String id = view.getUserId();
    String password = view.getPassword();

    String serverIp = ConfigLoader.getProperty("server.ip");
    int serverPort = Integer.parseInt(ConfigLoader.getProperty("server.port"));

    try (
        Socket socket = new Socket(serverIp, serverPort);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    ) {
        out.println("LOGIN," + id + "," + password);

        // 서버 응답 처리
        String response = in.readLine();
        if (response != null && response.startsWith("SUCCESS")) {
            String[] parts = response.split(",", 2);
            String userName = parts.length > 1 ? parts[1] : "이름없음";

            // 세션 저장
            Session.setLoggedInUserId(id);
            Session.setLoggedInUserName(userName);

            view.showMessage("로그인 성공!");
            view.dispose();
            openUserMainView(id.charAt(0));
        } else {
            view.showMessage("로그인 실패: 아이디 또는 비밀번호가 틀렸습니다.");
        }
    } catch (IOException e) {
        view.showMessage("서버와 연결할 수 없습니다: " + e.getMessage());
    }
}


    private void openUserMainView(char userType) {
        switch (userType) {
            case 'S': // 학생
            case 'P': // 교수도 같은 화면으로 이동
                
                 // 이미 열려 있는 RoomSelect 닫기
            for (java.awt.Window window : java.awt.Window.getWindows()) {
                if (window instanceof RoomSelect) {
                    window.dispose();
                }
                }
                RoomSelect roomSelect = new RoomSelect();
                new RoomSelectController(roomSelect);
                roomSelect.setVisible(true);
                break;
            case 'A': // 조교
                Executive executive = new Executive();
                new ExecutiveController(executive); // 버튼 기능 연결
                executive.setVisible(true);
                break;

            default:
                System.out.println("알 수 없는 사용자 타입: " + userType);
                break;
        }
    }

    private void openMembership() {
        if (membershipView == null || !membershipView.isVisible()) {
            membershipView = new MembershipView();
            MembershipModel membershipModel = new MembershipModel();

            new MembershipController(membershipView, membershipModel, view);

            view.setVisible(false);
            membershipView.setVisible(true);
        }
    }
}
