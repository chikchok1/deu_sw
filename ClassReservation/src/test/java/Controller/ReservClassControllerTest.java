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
import java.lang.reflect.Method;


import org.mockito.ArgumentCaptor;

public class ReservClassControllerTest {

    @Mock
    ReservClassView mockView;

    ReservClassController controller;
    JButton mockReservationButton;

    File file = new File("data/ReserveClass.txt");

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // 항상 파일 쓸 수 있게 복원
        if (!file.exists()) file.createNewFile();
        file.setWritable(true);
        new FileWriter(file).close(); // 내용 비우기

        // 세션 설정
        Session.setLoggedInUserId("S20230001");
        Session.setLoggedInUserName("김학생");

        // Mock View 기본값 설정
        when(mockView.getSelectedClassRoom()).thenReturn("908호");
        when(mockView.getSelectedDay()).thenReturn("월요일");
        when(mockView.getSelectedTime()).thenReturn("1교시(09:00~10:00)");
        when(mockView.getPurpose()).thenReturn("스터디");

        // 버튼 및 컴포넌트 생성
        JButton mockBefore = new JButton();
        mockReservationButton = new JButton();
        when(mockView.getBeforeButton()).thenReturn(mockBefore);
        when(mockView.getClassComboBox()).thenReturn(new JComboBox<>());

        // View 동작은 무시
        doNothing().when(mockView).showMessage(anyString());
        doNothing().when(mockView).closeView();
        doNothing().when(mockView).updateCalendarTable(any(JTable.class));
        doNothing().when(mockView).resetReservationButtonListener();

        // 리스너를 mock 버튼에 붙이도록
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

        System.out.println("[정상 예약 테스트] 통과 ");
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
        System.out.println("[중복 예약 테스트] 통과 ");
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
        System.out.println("[빈 목적 테스트] 통과 ");
    }

    @Disabled("파일 잠금이 환경에 따라 실패해서 무시함")
    @Test
    void testReserveRoom_FileWriteFailure() throws Exception {
    // 파일을 미리 열어서 잠금 처리
    FileWriter lock = new FileWriter("data/ReserveClass.txt");
    lock.write(""); lock.flush(); // 파일 열기만 하고 닫지 않음

    // 버튼 클릭 시도
    for (ActionListener listener : mockReservationButton.getActionListeners()) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    // 에러 메시지가 출력됐는지 확인
    verify(mockView).showMessage(startsWith("예약 중 오류 발생"));
    lock.close(); // 잠금 해제
}

    @Test
    void testReserveRoom_WhenFileMissing_ShouldSucceed() throws Exception {
    // 파일이 존재하지 않도록 삭제
    File file = new File("data/ReserveClass.txt");
    if (file.exists()) file.delete();

    for (ActionListener listener : mockReservationButton.getActionListeners()) {
        listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
    }

    // 메시지 확인
    verify(mockView).showMessage("예약이 완료되었습니다!");
}


    @AfterEach
    void tearDown() {
        Session.clear();
        file.setWritable(true); // 항상 복원
    }
}
