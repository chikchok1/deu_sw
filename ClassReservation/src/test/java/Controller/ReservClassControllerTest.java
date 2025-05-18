package Controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import Controller.ReservClassController;
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

    File file = new File("data/ReserveClass.txt");

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        if (!file.exists()) file.createNewFile();
        file.setWritable(true);
        new FileWriter(file).close();

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
        System.out.println("[정상 예약 테스트] 시작");

        // 예약 버튼 클릭 시뮬레이션
        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        assertTrue(file.exists(), "예약 파일이 생성되지 않았습니다!");

        System.out.println("[파일 내용 확인]");
        System.out.println(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            assertNotNull(line, "파일의 첫 줄이 null입니다. 예약이 기록되지 않았습니다.");
            assertTrue(line.contains("908호"));
            assertTrue(line.contains("월요일"));
            assertTrue(line.contains("1교시(09:00~10:00)"));
            assertTrue(line.contains("스터디"));
            assertTrue(line.contains("예약됨"));
        }

        System.out.println("[정상 예약 테스트] 통과");
    }

    @Test
    void testReserveRoom_DuplicateReservation() throws Exception {
        System.out.println("[중복 예약 테스트] 시작");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("김학생,908호,월요일,1교시(09:00~10:00),스터디,학생,예약됨\n");
        }

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.readLine() != null) {
                lineCount++;
            }
        }

        assertEquals(1, lineCount);
        verify(mockView).showMessage("이미 같은 강의실, 요일 및 시간에 예약이 존재합니다.");
        System.out.println("[중복 예약 테스트] 통과");
    }

    @Test
    void testReserveRoom_PurposeEmpty() throws Exception {
        System.out.println("[빈 목적 테스트] 시작");

        when(mockView.getPurpose()).thenReturn("");

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            assertNull(br.readLine());
        }

        verify(mockView).showMessage("사용 목적을 입력해주세요.");
        System.out.println("[빈 목적 테스트] 통과");
    }

    @Disabled("환경 의존성으로 인해 파일 잠금 테스트는 생략함")
    @Test
    void testReserveRoom_FileWriteFailure() throws Exception {
        FileWriter lock = new FileWriter(file);
        lock.write("");
        lock.flush();

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        verify(mockView).showMessage(startsWith("예약 중 오류 발생"));
        lock.close();
    }

    @Test
    void testReserveRoom_WhenFileMissing_ShouldSucceed() throws Exception {
        if (file.exists()) file.delete();

        // 예약 정보 재설정 (mockView가 초기화되어 있음)
        when(mockView.getSelectedClassRoom()).thenReturn("908호");
        when(mockView.getSelectedDay()).thenReturn("월요일");
        when(mockView.getSelectedTime()).thenReturn("1교시(09:00~10:00)");
        when(mockView.getPurpose()).thenReturn("스터디");

        for (ActionListener listener : mockReservationButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        }

        assertTrue(file.exists(), "파일이 생성되지 않았습니다.");
        verify(mockView).showMessage(contains("예약이 완료되었습니다"));
    }

    @AfterEach
    void tearDown() {
        Session.clear();
        file.setWritable(true);
        if (file.exists()) file.delete();
    }
}
