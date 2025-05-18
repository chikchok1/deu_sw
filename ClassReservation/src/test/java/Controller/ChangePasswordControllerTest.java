package Controller;

import Model.Session;
import View.ChangePasswordView;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import static org.mockito.Mockito.*;
import java.io.*;
import javax.swing.JOptionPane;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


public class ChangePasswordControllerTest {

    private ChangePasswordView mockView;
    private ChangePasswordController controller;
    private static final String TEST_FILE = "test_users.txt";

    private static MockedStatic<Session> sessionMock;
    private static MockedStatic<JOptionPane> mockJOptionPane;

    @BeforeAll
    static void initMocks() {
        // Session mock
        sessionMock = Mockito.mockStatic(Session.class);

        // JOptionPane mock
        mockJOptionPane = Mockito.mockStatic(JOptionPane.class);
        mockJOptionPane.when(() -> JOptionPane.showMessageDialog(any(), any())).thenAnswer(invocation -> null);
    }

    @AfterAll
    static void closeMocks() {
        sessionMock.close();
        mockJOptionPane.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        mockView = mock(ChangePasswordView.class);
        controller = new ChangePasswordController(mockView) {
            @Override
            protected String getFileNameByUserId(String userId) {
                return TEST_FILE;
            }
        };

        Files.writeString(Paths.get(TEST_FILE), "학생,S1234,oldpass\n");
        sessionMock.when(Session::getLoggedInUserId).thenReturn("S1234");
    }

    @AfterEach
    void tearDown() throws IOException {
        // 파일 삭제 전 잠시 대기 (Windows에서 파일 잠김 방지용)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
         e.printStackTrace();
        }

     Files.deleteIfExists(Paths.get(TEST_FILE));
    }
    
    @Test
    void testChangePasswordSuccess() throws IOException {
        when(mockView.getPresentPassword()).thenReturn("oldpass");
        when(mockView.getChangePassword()).thenReturn("newpass");

        controller.changePassword();

        String content = Files.readString(Paths.get(TEST_FILE));
        Assertions.assertTrue(content.contains("S1234,newpass"));
    }

    @Test
    void testChangePasswordWrongOldPassword() throws IOException {
        when(mockView.getPresentPassword()).thenReturn("wrongpass");
        when(mockView.getChangePassword()).thenReturn("newpass");

        controller.changePassword();

        String content = Files.readString(Paths.get(TEST_FILE));
        Assertions.assertTrue(content.contains("S1234,oldpass")); // 변경되지 않아야 함
    }
    
    @Test
    void testEmptyFields() {
        when(mockView.getPresentPassword()).thenReturn("");
        when(mockView.getChangePassword()).thenReturn("");

        controller.changePassword();

        // 단순히 에러 없이 실행되었는지만 확인
        Assertions.assertTrue(new File(TEST_FILE).exists());
    }   

    @Test
    void testUserNotFound() throws IOException {
        // 파일 내용 변경 (다른 ID로)
        Files.writeString(Paths.get(TEST_FILE), "학생,S9999,oldpass\n");
        
        when(mockView.getPresentPassword()).thenReturn("oldpass");
        when(mockView.getChangePassword()).thenReturn("newpass");
        
        controller.changePassword();
        
        String content = Files.readString(Paths.get(TEST_FILE));
        Assertions.assertTrue(content.contains("S9999,oldpass")); // 변경되지 않아야 함
    }

}