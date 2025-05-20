/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controller;

/**
 *
 * @author minju
 */

import View.Reservationchangeview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservationchangeviewControllerTest {

    @Mock private Reservationchangeview mockView;
    @Mock private JButton mockChangeButton;
    @Mock private JButton mockBackButton;
    @Mock private JButton mockCancelButton;
    @Mock private JTable mockTable;
    @Mock private ListSelectionModel mockSelectionModel;
    @Mock private DefaultTableModel mockTableModel;

    @BeforeEach
    void setUp() {
        when(mockView.getReservationTable()).thenReturn(mockTable);
        when(mockTable.getSelectionModel()).thenReturn(mockSelectionModel);
        when(mockTable.getModel()).thenReturn(mockTableModel);

        new ReservationchangeviewController(mockView);
    }

    @Test
    void testListenersAreAttached() {
        verify(mockView).setChangeButtonActionListener(any());
        verify(mockView).setBackButtonActionListener(any());
        verify(mockView).setCancelButtonActionListener(any());
        verify(mockSelectionModel).addListSelectionListener(any(ListSelectionListener.class));
    }
}
