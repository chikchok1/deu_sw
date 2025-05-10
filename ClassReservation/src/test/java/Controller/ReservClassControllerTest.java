package Controller;

import View.ReservClassView;
import Model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservClassControllerTest {

    @Mock
    private ReservClassView mockView;

    @InjectMocks
    private ReservClassController controller;

    @BeforeEach
    void setup() throws IOException {
        Session.setLoggedInUserName("홍길동");

        Path file = Paths.get("ReserveClass.txt");
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        Files.write(file, "".getBytes()); // 초기화

        // 공통 Mock 반환값 설정
        when(mockView.getSelectedClassRoom()).thenReturn("101호");
        when(mockView.getSelectedDay()).thenReturn("월요일");
        when(mockView.getSelectedTime()).thenReturn("1교시");
    }

    @Test
    public void testReservationFailsWhenPurposeIsEmpty() {
        when(mockView.getPurpose()).thenReturn(""); // 목적 없음

        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.new ReservationListener().actionPerformed(mockEvent);

        verify(mockView).showMessage("사용 목적을 입력해주세요.");
        verify(mockView, never()).closeView();
    }

    @Test
    public void testReservationFailsWhenDuplicateExists() throws IOException {
        // 중복 예약 시뮬레이션 (파일에 직접 기록)
        Files.write(Paths.get("ReserveClass.txt"),
            "홍길동,101호,월요일,1교시,스터디\n".getBytes());

        when(mockView.getPurpose()).thenReturn("스터디");

        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.new ReservationListener().actionPerformed(mockEvent);

        verify(mockView).showMessage("이미 같은 강의실, 요일 및 시간에 예약이 존재합니다.");
        verify(mockView, never()).closeView();
    }

    @Test
    public void testReservationSuccess() {
        when(mockView.getPurpose()).thenReturn("회의");

        ActionEvent mockEvent = mock(ActionEvent.class);
        controller.new ReservationListener().actionPerformed(mockEvent);

        verify(mockView).showMessage("예약이 완료되었습니다!");
        verify(mockView).closeView();
    }
}
