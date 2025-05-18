package Controller;

import View.RoomSelect;
import View.ReservClassView;
import View.ReservLabView;
import View.ReservedRoomView;
import View.ChangePasswordView;
import View.LoginForm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.swing.*;
import java.awt.event.ActionListener;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // ✅ 불필요한 stubbing 경고 제거
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

        // 버튼 리스너 캡처용
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
    void shouldOpenReservClassView_whenClassButtonClicked() {
        classButtonListener.actionPerformed(null);
        verify(mockView).dispose();
    }

    @Test
    void shouldOpenReservLabView_whenLabButtonClicked() {
        labButtonListener.actionPerformed(null);
        verify(mockView).dispose();
    }

    @Test
    void shouldOpenReservedRoomView_whenViewReservedButtonClicked() {
        viewReservedListener.actionPerformed(null);
        verify(mockView).dispose();
    }

    @Test
    void shouldOpenChangePasswordView_whenChangePasswordButtonClicked() {
        changePwListener.actionPerformed(null);
        verify(mockView).dispose();
    }

    @Test
    void shouldReturnToLogin_whenLogoutButtonClicked() {
        logoutListener.actionPerformed(null);
        verify(mockView).dispose();
    }
}
