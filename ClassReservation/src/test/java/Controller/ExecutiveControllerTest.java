/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author minju
 */

import View.*;
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
class ExecutiveControllerTest {

    @Mock private Executive mockExecutive;
    @Mock private JButton mockViewReservedButton;
    @Mock private JButton mockJButton2;
    @Mock private JButton mockJButton3;
    @Mock private JButton mockJButton5;
    @Mock private JButton mockJButton6;

    @BeforeEach
    void setUp() {
        // 각 버튼에 대한 mock 반환 설정
        when(mockExecutive.getViewReservedButton()).thenReturn(mockViewReservedButton);
        when(mockExecutive.getJButton2()).thenReturn(mockJButton2);
        when(mockExecutive.getJButton3()).thenReturn(mockJButton3);
        when(mockExecutive.getJButton5()).thenReturn(mockJButton5);
        when(mockExecutive.getJButton6()).thenReturn(mockJButton6);

        // 컨트롤러 초기화 (리스너 등록됨)
        new ExecutiveController(mockExecutive);
    }

    @Test
    void testButtonListenersAttached() {
        // 각 버튼에 ActionListener가 정상적으로 등록됐는지 확인
        verify(mockViewReservedButton).addActionListener(any(ActionListener.class));
        verify(mockJButton2).addActionListener(any(ActionListener.class));
        verify(mockJButton3).addActionListener(any(ActionListener.class));
        verify(mockJButton5).addActionListener(any(ActionListener.class));
        verify(mockJButton6).addActionListener(any(ActionListener.class));
    }
}

