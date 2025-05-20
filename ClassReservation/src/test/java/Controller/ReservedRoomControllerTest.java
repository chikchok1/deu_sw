/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author minju
 */

import Model.ReservedRoomModel;
import Model.User;
import View.ReservedRoomView;
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
class ReservedRoomControllerTest {

    @Mock private ReservedRoomView mockView;
    @Mock private ReservedRoomModel mockModel;
    @Mock private JButton mockCheckButton;
    @Mock private JComboBox<String> mockClassComboBox;
    @Mock private JComboBox<String> mockLabComboBox;
    @Mock private JButton mockBeforeButton;
    @Mock private JTable mockTable;

    private ReservedRoomController controller;

    @BeforeEach
    void setUp() {
        when(mockView.getCheckButton()).thenReturn(mockCheckButton);
        when(mockView.getClassComboBox()).thenReturn(mockClassComboBox);
        when(mockView.getLabComboBox()).thenReturn(mockLabComboBox);
        when(mockView.getBeforeButton()).thenReturn(mockBeforeButton);
        when(mockView.getTable()).thenReturn(mockTable);
        when(mockView.getSelectedRoom()).thenReturn("101호");
        when(mockView.isUpdating()).thenReturn(false);

        controller = new ReservedRoomController(mockView);
    }

    @Test
    void testActionListenersAttached() {
        verify(mockCheckButton).addActionListener(any(ActionListener.class));
        verify(mockClassComboBox).addActionListener(any(ActionListener.class));
        verify(mockLabComboBox).addActionListener(any(ActionListener.class));
        verify(mockBeforeButton).addActionListener(any(ActionListener.class));
    }
}
