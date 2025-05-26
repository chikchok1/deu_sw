package Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import Model.Session;
import View.ReservClassView;

import org.junit.jupiter.api.*;
import org.mockito.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ReservClassControllerTest {

    @Mock
    ReservClassView mockView;
    ReservClassController controller;
    JButton mockReservationButton;

    File file = new File("data/ReserveClass.txt").getAbsoluteFile();

    @BeforeAll
    static void enableTestMode() {
        System.setProperty("test.env", "true");
    }

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        Files.write(file.toPath(), new byte[0]); // 파일 초기화

        Session.setLoggedInUserId("S20230001");
        Session.setLoggedInUserName("김학생");

        when(mockView.getSelectedClassRoom()).thenReturn("908호");
        when(mockView.getSelectedDay()).thenReturn("월요일");
        when(mockView.getSelectedTime()).thenReturn("1교시(09:00~10:00)");
        when(mockView.getPurpose()).thenReturn("스터디");

        JButton mockBefore = new JButton();
        mockReservationButton = new JButton();
        when(mockView.getBeforeButton()).thenReturn(mockBefore);
        when(mockView.getClassComboBox()).thenReturn(new JComboBox<>());

        doNothing().when(mockView).showMessage(anyString());
        doNothing().when(mockView).closeView();
        doNothing().when(mockView).updateCalendarTable(any(JTable.class));
        doNothing().when(mockView).resetReservationButtonListener();

        doAnswer(invocation -> {
            ActionListener listener = invocation.getArgument(0);
            mockReservationButton.addActionListener(listener);
            return null;
        }).when(mockView).addReservationListener(any(ActionListener.class));

        controller = new ReservClassController(mockView);
    }

    @Test
    void testReserveRoom_Success() throws Exception {
        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            assertNotNull(line);
            assertTrue(line.contains("908호"));
            assertTrue(line.contains("월요일"));
            assertTrue(line.contains("1교시"));
            assertTrue(line.contains("스터디"));
            assertTrue(line.contains("예약됨"));
        }

        verify(mockView).showMessage(contains("예약이 완료되었습니다"));
    }

    @Test
    void testReserveRoom_DuplicateReservation() throws Exception {
        Files.writeString(file.toPath(), "김학생,908호,월요일,1교시(09:00~10:00),스터디,학생,예약됨\n");

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        long count = Files.lines(file.toPath()).count();
        assertEquals(1, count);
        verify(mockView).showMessage("이미 같은 강의실, 요일 및 시간에 예약이 존재합니다.");
    }

    @Test
    void testReserveRoom_PurposeEmpty() throws Exception {
        when(mockView.getPurpose()).thenReturn("");

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        assertEquals(0, Files.lines(file.toPath()).count());
        verify(mockView).showMessage("사용 목적을 입력해주세요.");
    }

    @Test
    void testReserveRoom_WhenFileMissing_ShouldSucceed() throws Exception {
        if (file.exists()) {
            file.delete();
        }

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        assertTrue(file.exists());
        verify(mockView).showMessage(contains("예약이 완료되었습니다"));
    }

    @AfterEach
    void tearDown() {
        Session.clear();
        if (file.exists()) {
            file.delete();
        }
    }
}
