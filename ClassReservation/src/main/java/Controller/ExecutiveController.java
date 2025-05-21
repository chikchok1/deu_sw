package Controller;

import View.Executive;
import View.ReservedRoomView;
import View.LoginForm;
import View.RoomAdmin;
import View.ClientAdmin;
import Model.UserDAO;
import Model.Session;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;

import Model.UserDAO;
import View.ClientAdmin;
import Controller.RoomAdminController;
import javax.swing.JOptionPane;
import java.io.FileReader;
import java.io.IOException;


public class ExecutiveController {

    private Executive executive;
    private static boolean hasShownAlert = false;


    public ExecutiveController(Executive executive) {
        this.executive = executive;

        // 예약 요청 알림 확인
        if (!hasShownAlert) {
            int count = countPendingRequests("data/ReservationRequest.txt");
            if (count > 0) {
                JOptionPane.showMessageDialog(
                    executive,
                    "📌 현재 대기 중인 예약 요청이 총 " + count + "건 있습니다.",
                    "예약 요청 알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            hasShownAlert = true;
        }

        // [1] "예약 확인" 버튼
        this.executive.getViewReservedButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReservedRoomView();
            }
        });

        // [2] "강의실 및 실습실 관리" 버튼
        this.executive.getJButton2().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRoomAdminView();
            }
        });

        // [3] "로그아웃" 버튼
        this.executive.getJButton3().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });

        // [4] "고객 관리" 버튼
        this.executive.getJButton5().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openClientAdminView();
            }
        });

        // [5] "예약 승인" 버튼
        this.executive.getJButton6().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openReservationApprovalView();
            }
        });
    }

    private void logout() {
        try {
            // 서버에 EXIT 명령 전송
            PrintWriter out = Session.getOut();
            BufferedReader in = Session.getIn();

            if (out != null) {
                out.println("EXIT");
                out.flush();
            }

            // 서버 응답 확인 (선택)
            if (in != null) {
                String response = in.readLine();
                System.out.println("서버 응답: " + response);
            }
        } catch (Exception ex) {
            System.out.println("로그아웃 중 오류: " + ex.getMessage());
        } finally {
            // 세션 정리
            Session.clear();

            // Executive 창 닫고 로그인 창 다시 열기
            executive.setVisible(false);
            executive.dispose();

            LoginForm loginForm = new LoginForm();
            UserDAO dao = new UserDAO();
            new LoginController(loginForm, dao);
            loginForm.setVisible(true);
        }
    }

    private void openReservedRoomView() {
        ReservedRoomView reservedView = new ReservedRoomView();
        new ReservedRoomController(reservedView);
        reservedView.setVisible(true);
    }

    private void openRoomAdminView() {
        RoomAdmin roomAdmin = new RoomAdmin();
        new RoomAdminController(roomAdmin);
        roomAdmin.setVisible(true);
    }

    private void openClientAdminView() {
        ClientAdmin clientAdmin = new ClientAdmin();
        new ClientAdminController(clientAdmin);
        clientAdmin.setVisible(true);
    }

    private void openReservationApprovalView() {
        executive.dispose();
        View.ClassroomReservationApproval approvalView = new View.ClassroomReservationApproval();
        new Controller.ClassroomReservationApprovalController(approvalView);
        approvalView.setVisible(true);
    }
    private int countPendingRequests(String filePath) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}
