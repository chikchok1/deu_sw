package Controller;


import Controller.LoginController;
import Model.UserDAO;
import View.LoginForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginForm mockView;

    @Mock
    private UserDAO mockDao;

    @InjectMocks
    private LoginController loginController;

    @Test
    void testHandleLogin_success() {
        when(mockView.getUserId()).thenReturn("S20230001");
        when(mockView.getPassword()).thenReturn("1234");
        when(mockDao.validateUser("S20230001", "1234")).thenReturn(true);
        when(mockDao.getUserNameById("S20230001")).thenReturn("홍길동");

        loginController.handleLogin();

        verify(mockView).showMessage("로그인 성공!");
        verify(mockView).dispose();
    }

    @Test
    void testHandleLogin_fail() {
        when(mockView.getUserId()).thenReturn("S20230001");
        when(mockView.getPassword()).thenReturn("wrong");
        when(mockDao.validateUser("S20230001", "wrong")).thenReturn(false);

        loginController.handleLogin();

        verify(mockView).showMessage("아이디 또는 비밀번호가 틀렸습니다.");
        verify(mockView, never()).dispose();
    }
}
