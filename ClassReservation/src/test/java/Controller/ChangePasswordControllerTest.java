package Controller;

import Model.Session;
import View.ChangePasswordView;
import org.junit.jupiter.api.*;
import java.io.*;
import javax.swing.JOptionPane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ChangePasswordControllerTest {

    private ChangePasswordView mockView;
    private ChangePasswordController controller;
    private static MockedStatic<Session> sessionMock;
    private static MockedStatic<JOptionPane> mockJOptionPane;

    @BeforeAll
    static void initMocks() {
        // Session과 JOptionPane 정적 메서드 모킹
        sessionMock = Mockito.mockStatic(Session.class);
        mockJOptionPane = Mockito.mockStatic(JOptionPane.class);
        mockJOptionPane.when(() -> JOptionPane.showMessageDialog(any(), any())).thenAnswer(invocation -> null);
    }

    @AfterAll
    static void closeMocks() {
        sessionMock.close();
        mockJOptionPane.close();
    }

    @BeforeEach
    void setUp() {
        mockView = mock(ChangePasswordView.class);
        controller = new ChangePasswordController(mockView);
        sessionMock.when(Session::getLoggedInUserId).thenReturn("S1234");
    }

    @Test
    void testChangePasswordSuccess() throws IOException {
        when(mockView.getPresentPassword()).thenReturn("oldpass");
        when(mockView.getChangePassword()).thenReturn("newpass");

        // Session 입출력 스트림 설정
        PipedOutputStream serverInput = new PipedOutputStream();
        PrintWriter mockOut = new PrintWriter(serverInput, true);
        PipedInputStream clientOutput = new PipedInputStream(serverInput);

        PipedOutputStream clientInput = new PipedOutputStream();
        BufferedReader mockIn = new BufferedReader(new InputStreamReader(new PipedInputStream(clientInput)));

        sessionMock.when(Session::getOut).thenReturn(mockOut);
        sessionMock.when(Session::getIn).thenReturn(mockIn);

        // 서버 응답 시뮬레이션
        new Thread(() -> {
            try {
                Thread.sleep(100);
                clientInput.write("PASSWORD_CHANGED\n".getBytes());
                clientInput.flush();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        controller.changePassword();

        // 성공 시 dispose() 호출되었는지 확인
        verify(mockView, atLeastOnce()).dispose();
    }

    @Test
    void testUserNotFound() throws IOException {
        when(mockView.getPresentPassword()).thenReturn("oldpass");
        when(mockView.getChangePassword()).thenReturn("newpass");

        // Session 입출력 스트림 설정
        PipedOutputStream serverInput = new PipedOutputStream();
        PrintWriter mockOut = new PrintWriter(serverInput, true);
        PipedInputStream clientOutput = new PipedInputStream(serverInput);

        PipedOutputStream clientInput = new PipedOutputStream();
        BufferedReader mockIn = new BufferedReader(new InputStreamReader(new PipedInputStream(clientInput)));

        sessionMock.when(Session::getOut).thenReturn(mockOut);
        sessionMock.when(Session::getIn).thenReturn(mockIn);

        // 서버 응답 시뮬레이션
        new Thread(() -> {
            try {
                Thread.sleep(100);
                clientInput.write("USER_NOT_FOUND\n".getBytes());
                clientInput.flush();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        controller.changePassword();

        // 실패 시 dispose() 호출되지 않아야 함
        verify(mockView, never()).dispose();
    }

   @Test
void testEmptyFields() {
    when(mockView.getPresentPassword()).thenReturn("");
    when(mockView.getChangePassword()).thenReturn("");

    controller.changePassword();

    // 메시지 출력만 되었는지 확인하고, 다른 검증은 생략 (Session 호출 여부는 무시)
    mockJOptionPane.verify(() ->
        JOptionPane.showMessageDialog(any(), eq("모든 필드를 입력해주세요.")),
        times(1)
    );
}
}
