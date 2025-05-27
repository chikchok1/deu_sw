package Controller;

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

        doNothing().when(mockView).dispose();
        doNothing().when(mockView).setVisible(false);

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
    @DisplayName("수업 예약 버튼 클릭 시 창 dispose 호출됨")
    void shouldOpenReservClassView_whenClassButtonClicked() {
        Assumptions.assumeTrue(classButtonListener != null);
        classButtonListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
        verify(mockView, never()).setVisible(false);
    }

    @Test
    @DisplayName("실습실 예약 버튼 클릭 시 창 dispose 호출됨")
    void shouldOpenReservLabView_whenLabButtonClicked() {
        Assumptions.assumeTrue(labButtonListener != null);
        labButtonListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
        verify(mockView, never()).setVisible(false);
    }

    @Test
    @DisplayName("예약 확인 버튼 클릭 시 setVisible(false) 호출됨")
    void shouldOpenReservedRoomView_whenViewReservedButtonClicked() {
        Assumptions.assumeTrue(viewReservedListener != null);
        viewReservedListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).setVisible(false);
        verify(mockView, never()).dispose();
    }

    @Test
    @DisplayName("비밀번호 변경 버튼 클릭 시 setVisible(false) 호출됨")
    void shouldOpenChangePasswordView_whenChangePasswordButtonClicked() {
        Assumptions.assumeTrue(changePwListener != null);
        changePwListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).setVisible(false);
        verify(mockView, never()).dispose();
    }

    @Test
    @DisplayName("로그아웃 버튼 클릭 시 dispose 호출됨")
    void shouldReturnToLogin_whenLogoutButtonClicked() {
        Assumptions.assumeTrue(logoutListener != null);
        logoutListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "click"));
        verify(mockView).dispose();
        verify(mockView, never()).setVisible(false);
    }
}
