package Server;

import Model.User;
import Model.UserDAO;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginServerTest {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final File USERS_FILE = new File("data/users.txt");

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private Thread serverThread;

   @BeforeAll
void startServerAndPrepareFile() throws Exception {
    try (Socket testSocket = new Socket(HOST, PORT)) {
        // 이미 서버가 실행 중이면 서버 실행 생략
        System.out.println("서버가 이미 실행 중입니다.");
    } catch (IOException e) {
        // 서버가 실행 중이 아니므로 새로 시작
        serverThread = new Thread(() -> {
            try {
                LoginServer.main(null);  // 서버 시작
            } catch (Exception ignored) {}
        });
        serverThread.start();

        Thread.sleep(1000);  // 서버가 완전히 뜰 때까지 잠깐 대기
    }

    // 테스트용 유저 파일 초기화
    USERS_FILE.getParentFile().mkdirs();
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
        writer.write("홍길동,S20230001,abc123");
        writer.newLine();
    }
}

    @AfterAll
    void stopServerAndCleanUp() throws IOException, InterruptedException {
        // 서버 종료 요청
        try (Socket shutdownSocket = new Socket(HOST, PORT); PrintWriter shutdownOut = new PrintWriter(shutdownSocket.getOutputStream(), true)) {
            shutdownOut.println("SHUTDOWN");
        }

        // 서버가 종료될 시간 대기
        Thread.sleep(500);

        Files.deleteIfExists(USERS_FILE.toPath());
    }

    @BeforeEach
    void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @AfterEach
    void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Test
    void testLoginSuccess() throws IOException {
        out.println("LOGIN,S20230001,abc123");
        String response = in.readLine();
        assertTrue(response.startsWith("SUCCESS"), "Expected SUCCESS, got: " + response);
    }

    @Test
    void testLoginFail_WrongPassword() throws IOException {
        out.println("LOGIN,S20230001,wrongpass");
        String response = in.readLine();
        assertEquals("FAIL", response);
    }

    @Test
    void testRegisterSuccess() throws IOException {
        String randomId = "S" + (int) (Math.random() * 90000 + 10000);
        out.println("REGISTER,테스트유저," + randomId + ",pw1234");
        String response = in.readLine();
        assertEquals("SUCCESS", response);
    }

    @Test
    void testRegisterDuplicate() throws IOException {
        out.println("REGISTER,홍길동,S20230001,abc123");
        String response = in.readLine();
        assertEquals("DUPLICATE", response);
    }
}
