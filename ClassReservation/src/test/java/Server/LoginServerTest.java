/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;
// 미리 Server를 실행해둔 상태여야함. 
/**
 *
 * @author minju
 */

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginServerTest {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static final File USERS_FILE = new File("data/users.txt");

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    @BeforeAll
    void setupTestUserFile() throws IOException {
        // 테스트용 유저 등록
        USERS_FILE.getParentFile().mkdirs();  // 폴더 없을 수도 있음
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            writer.write("홍길동,S20230001,abc123");
            writer.newLine();
        }
    }

    @BeforeEach
    void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @AfterEach
    void close() throws IOException {
        socket.close();
    }

    @AfterAll
    void deleteUserFile() throws IOException {
        Files.deleteIfExists(USERS_FILE.toPath());
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
        String randomId = "S" + (int)(Math.random() * 90000 + 10000);  // ex. S12345
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
