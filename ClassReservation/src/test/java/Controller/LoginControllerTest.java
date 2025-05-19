package Controller;

import Model.UserDAO;
import Model.Session;
import View.LoginForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * LoginControllerTest - 명확한 테스트 메서드명 (의도 중심) - 의미 있는 검증 (메시지 내용 확인 등) - 불필요한 모킹
 * 제거
 */
@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock(lenient = true)
    private LoginForm mockView;

    @Mock(lenient = true)
    private UserDAO mockDao;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setup() {
        when(mockView.getUserId()).thenReturn("S20230001");
        when(mockView.getPassword()).thenReturn("1234");
    }

    /**
     * [테스트 목적] 서버가 꺼져 있거나 연결할 수 없는 경우, 사용자에게 오류 메시지 "서버와 연결할 수 없습니다"를 출력해야 한다.
     */
    @Test
    void shouldShowServerErrorMessage_whenServerIsUnavailable() {
        loginController.handleLogin();

        verify(mockView).showMessage(startsWith("서버와 연결할 수 없습니다"));
    }

    /**
     * [테스트 목적] 아이디와 비밀번호 입력 칸이 비어 있을 때, 서버에 연결을 시도하지만 실패 메시지를 출력해야 한다.
     */
    @Test
    void shouldShowErrorMessage_whenInputFieldsAreEmpty() {
        when(mockView.getUserId()).thenReturn("");
        when(mockView.getPassword()).thenReturn("");

        loginController.handleLogin();

        verify(mockView).showMessage(contains("서버와 연결할 수 없습니다"));
    }

    /**
     * [테스트 목적] 서버가 꺼져 있거나 연결할 수 없는 상황에서는 "로그인 성공!" 메시지를 출력하지 않고, 대신 오류 메시지를
     * 보여줘야 한다.
     */
    @Test
    void shouldNotShowSuccessMessage_whenLoginFailsDueToServer() {
        loginController.handleLogin();

        verify(mockView, never()).showMessage("로그인 성공!");
        verify(mockView).showMessage(contains("서버와 연결할 수 없습니다"));
    }

    /**
     * [테스트 목적] 서버 연결 실패 상황에서는 세션(Session)에 사용자 ID와 이름이 저장되지 않아야 한다.
     */
    @Test
    void shouldNotSetSession_whenServerConnectionFails() {
        // 실행
        loginController.handleLogin();

        // 검증
        assertNull(Session.getLoggedInUserId(), "서버 연결 실패 시 세션에 사용자 ID가 저장되지 않아야 함");
        assertNull(Session.getLoggedInUserName(), "서버 연결 실패 시 세션에 사용자 이름이 저장되지 않아야 함");
    }

    /**
     * [테스트 목적] 로그인 실패 시 view.dispose()가 호출되지 않아야 한다 (즉, 화면 전환이 없어야 함).
     */
    @Test
    void shouldNotDisposeView_whenLoginFailsDueToServer() {
        // 실행
        loginController.handleLogin();

        // 검증: dispose()는 성공 시에만 호출되어야 하므로 호출되지 않아야 함
        verify(mockView, never()).dispose();
    }
}
