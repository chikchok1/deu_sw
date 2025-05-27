package Controller;

import Model.Session;
import View.ChangePasswordView;
import org.junit.jupiter.api.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;
import javax.swing.JOptionPane;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.util.concurrent.TimeUnit;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class ChangePasswordControllerTest {

    private ChangePasswordView mockView;
    private ChangePasswordController controller;
    private static MockedStatic<Session> sessionMock;
    private static MockedStatic<JOptionPane> mockJOptionPane;

    @BeforeEach
    void setUp() {
        mockView = mock(ChangePasswordView.class);
        doNothing().when(mockView).dispose(); // 👉 dispose도 모킹
        controller = new ChangePasswordController(mockView);

        sessionMock = Mockito.mockStatic(Session.class);
        mockJOptionPane = Mockito.mockStatic(JOptionPane.class);
        mockJOptionPane.when(() -> JOptionPane.showMessageDialog(any(), any())).thenAnswer(invocation -> null);

        sessionMock.when(Session::getLoggedInUserId).thenReturn("S1234");
    }

    @AfterEach
    void tearDown() {
        if (sessionMock != null) sessionMock.close();
        if (mockJOptionPane != null) mockJOptionPane.close();
    }
@Test
void testChangePasswordSuccess() throws IOException, InterruptedException {
    when(mockView.getPresentPassword()).thenReturn("oldpass");
    when(mockView.getChangePassword()).thenReturn("newpass");

    PipedOutputStream serverInput = new PipedOutputStream();
    PrintWriter mockOut = new PrintWriter(serverInput, true);
    PipedInputStream clientOutput = new PipedInputStream(serverInput);

    PipedOutputStream clientInput = new PipedOutputStream();
    PipedInputStream pipedInput = new PipedInputStream(clientInput);
    BufferedReader mockIn = new BufferedReader(new InputStreamReader(pipedInput));

    sessionMock.when(Session::getOut).thenReturn(mockOut);
    sessionMock.when(Session::getIn).thenReturn(mockIn);

    CountDownLatch latch = new CountDownLatch(1);

    new Thread(() -> {
        try {
            Thread.sleep(200);
            clientInput.write("PASSWORD_CHANGED\n".getBytes());
            clientInput.flush();
            latch.countDown();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }).start();

    try (
    MockedStatic<GraphicsEnvironment> graphicsMock = mockStatic(GraphicsEnvironment.class);
    MockedConstruction<View.RoomSelect> roomSelectMock = mockConstruction(View.RoomSelect.class);
    MockedConstruction<View.Executive> executiveMock = mockConstruction(View.Executive.class)
) {
    graphicsMock.when(GraphicsEnvironment::isHeadless).thenReturn(false);
    GraphicsEnvironment fakeEnv = mock(GraphicsEnvironment.class);
    graphicsMock.when(GraphicsEnvironment::getLocalGraphicsEnvironment).thenReturn(fakeEnv);
    when(fakeEnv.getDefaultScreenDevice()).thenReturn(mock(java.awt.GraphicsDevice.class));

    controller.changePassword();
    latch.await(1, TimeUnit.SECONDS);

    verify(mockView, atLeastOnce()).dispose();
}

}


    @Test
    void testUserNotFound() throws IOException {
        when(mockView.getPresentPassword()).thenReturn("oldpass");
        when(mockView.getChangePassword()).thenReturn("newpass");

        PipedOutputStream serverInput = new PipedOutputStream();
        PrintWriter mockOut = new PrintWriter(serverInput, true);
        PipedInputStream clientOutput = new PipedInputStream(serverInput);

        PipedOutputStream clientInput = new PipedOutputStream();
        BufferedReader mockIn = new BufferedReader(new InputStreamReader(new PipedInputStream(clientInput)));

        sessionMock.when(Session::getOut).thenReturn(mockOut);
        sessionMock.when(Session::getIn).thenReturn(mockIn);

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
        verify(mockView, never()).dispose();
    }

    @Test
    void testEmptyFields() {
        when(mockView.getPresentPassword()).thenReturn("");
        when(mockView.getChangePassword()).thenReturn("");

        controller.changePassword();

        mockJOptionPane.verify(() ->
            JOptionPane.showMessageDialog(any(), eq("모든 필드를 입력해주세요.")),
            times(1)
        );
    }
}
