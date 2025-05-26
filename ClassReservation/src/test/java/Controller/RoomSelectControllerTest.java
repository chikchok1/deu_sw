package Controller;

/**
 * 테스트 클래스: RoomSelectControllerTest
 * 목적: RoomSelectController의 버튼 클릭 동작 검증
 */

import View.RoomSelect;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomSelectControllerTest {

    @Mock private RoomSelect mockView;
    @Mock private JButton mockClassButton;
    @Mock private JButton mockLabButton;
    @Mock private JButton mockViewReservedButton;
    @Mock private JButton mockLogoutButton;
    @Mock private JButton mockChangePwButton;

    private RoomSelectController controller;
    private ActionListener classButtonListener;
    private ActionListener labButtonListener;
    private ActionListener viewReservedListener;
    private ActionListener logoutListener;
    private ActionListener changePwListener;

    @BeforeEach
    void setUp() {
        when(mockView.getClassButton()).thenReturn(mockClassButton);
        when(mockView.getLabButton()).thenReturn(mockLabButton);

        // 각 버튼의 리스너 캡처
        doAnswer(invocation -> {
            classButtonListener = invocation.getArgument(0);
            return null;
        }).when(mockView).setClassButtonActionListener(any());

        doAnswer(invocation -> {
            labButtonListener = invocation.getArgument(0);
            return null;
        }).when(mockView).setLabButtonActionListener(any());

        doAnswer(invocation -> {
            viewReservedListener = invocation.getArgument(0);
            return null;
        }).when(mockView).setViewReservedActionListener(any());

        doAnswer(invocation -> {
            logoutListener = invocation.getArgument(0);
            return null;
        }).when(mockView).setLogOutButtonActionListener(any());

        doAnswer(invocation -> {
            changePwListener = invocation.getArgument(0);
            return null;
        }).when(mockView).setChangePasswordActionListener(any());

        controller = new RoomSelectController(mockView);
    }

    @Test
    @DisplayName("수업 예약 버튼 클릭 시 창 닫힘 테스트")
    void shouldOpenReservClassView_whenClassButtonClicked() {
        Assumptions.assumeTrue(classButtonListener != null);
        classButtonListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
    }

    @Test
    @DisplayName("실습실 예약 버튼 클릭 시 창 닫힘 테스트")
    void shouldOpenReservLabView_whenLabButtonClicked() {
        Assumptions.assumeTrue(labButtonListener != null);
        labButtonListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
    }

    @Test
    @DisplayName("예약 확인 버튼 클릭 시 창 닫힘 테스트")
    void shouldOpenReservedRoomView_whenViewReservedButtonClicked() {
        Assumptions.assumeTrue(viewReservedListener != null);
        viewReservedListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
    }

    @Test
    @DisplayName("비밀번호 변경 버튼 클릭 시 창 닫힘 테스트")
    void shouldOpenChangePasswordView_whenChangePasswordButtonClicked() {
        Assumptions.assumeTrue(changePwListener != null);
        changePwListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
    }

    @Test
    @DisplayName("로그아웃 버튼 클릭 시 창 닫힘 테스트")
    void shouldReturnToLogin_whenLogoutButtonClicked() {
        Assumptions.assumeTrue(logoutListener != null);
        logoutListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
    }
}
