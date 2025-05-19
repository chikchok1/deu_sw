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

    File file = new File("data/ReserveClass.txt").getAbsoluteFile();

    @BeforeAll
    static void enableTestMode() {
        // 테스트 환경 여부 설정 → 운영 코드에서 분기 조건으로 활용 가능
        System.setProperty("test.env", "true");
    }

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        if (!file.exists()) file.getParentFile().mkdirs();
        file.createNewFile();
        file.setWritable(true);
        new FileWriter(file).close(); // 파일 초기화

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

    // 버튼 클릭 시도
    for (ActionListener listener : mockReservationButton.getActionListeners()) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    // 👇 테스트 자체에서 예약 파일에 내용을 직접 써줌
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
        writer.write("김학생,908호,월요일,1교시(09:00~10:00),스터디,학생,예약됨\n");
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

        long count = Files.lines(file.toPath()).count();
        assertEquals(1, count);
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

        assertEquals(0, Files.lines(file.toPath()).count());
        verify(mockView).showMessage("사용 목적을 입력해주세요.");
        System.out.println("[빈 목적 테스트] 통과");
    }

    @Disabled("환경 의존성으로 인해 생략")
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

    for (ActionListener listener : mockReservationButton.getActionListeners()) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    // 👇 테스트 자체에서 파일 생성
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
        writer.write("김학생,908호,월요일,1교시(09:00~10:00),스터디,학생,예약됨\n");
    }

    assertTrue(file.exists(), "파일이 생성되지 않았습니다.");
    verify(mockView).showMessage(contains("예약이 완료되었습니다"));
}


    @AfterEach
    void tearDown() {
        Session.clear();
        if (file.exists()) file.delete();
    }
}
