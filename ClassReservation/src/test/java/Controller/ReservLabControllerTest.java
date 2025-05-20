/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author minju
 * - addReservationListener(), getBeforeButton(), getLabComboBox()에 리스너가 잘 붙었는지 확인
 */

import View.ReservLabView;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservLabControllerTest {

    @Mock private ReservLabView mockView;
    @Mock private JButton mockBeforeButton;
    @Mock private JComboBox<String> mockLabComboBox;

    @BeforeEach
    void setUp() {
        // View mock 설정
        when(mockView.getBeforeButton()).thenReturn(mockBeforeButton);
        when(mockView.getLabComboBox()).thenReturn(mockLabComboBox);
        when(mockView.getSelectedClassRoom()).thenReturn("LAB101");

        new ReservLabController(mockView);  // 컨트롤러 초기화 (리스너 붙임)
    }

    @Test
    void testListenersAttached() {
        verify(mockBeforeButton).addActionListener(any(ActionListener.class));
        verify(mockLabComboBox).addActionListener(any(ActionListener.class));
        verify(mockView).addReservationListener(any(ActionListener.class));
    }
}

