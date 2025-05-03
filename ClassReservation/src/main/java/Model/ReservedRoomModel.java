package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ReservedRoomModel {

    public static class Reservation {
        public String name, room, day, period, purpose;

        public Reservation(String name, String room, String day, String period, String purpose) {
            this.name = name;
            this.room = room;
            this.day = day;
            this.period = period;
            this.purpose = purpose;
        }
    }

    public List<Reservation> getReservations(String filePath, String selectedRoom, char userType, String userName) {
        List<Reservation> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    System.err.println("잘못된 형식의 데이터: " + line);
                    continue;
                }

                String name = parts[0].trim();
                String room = parts[1].trim();
                String day = parts[2].trim();
                String period = parts[3].trim();
                String purpose = parts[4].trim();

                if (!selectedRoom.equals("선택") && !room.equals(selectedRoom)) continue;
                if (userType == 'S' && !name.equals(userName)) continue;

                result.add(new Reservation(name, room, day, period, purpose));
            }
        } catch (Exception e) {
            System.err.println("파일 읽기 오류: " + filePath);
            e.printStackTrace();
        }
        return result;
    }

    // 예약 ID를 기준으로 예약을 취소하는 메서드 추가
    public boolean cancelReservation(String name, String room, String day, String period, String purpose, char userType, String userName) {
        Iterator<Reservation> iterator = getReservations().iterator();
        while (iterator.hasNext()) {
            Reservation reservation = iterator.next();
            // 예약 정보와 사용자 유형을 확인합니다.
            if (reservation.name.equals(name) &&
                reservation.room.equals(room) &&
                reservation.day.equals(day) &&
                reservation.period.equals(period) &&
                reservation.purpose.equals(purpose) &&
                (userType == 'P' || reservation.name.equals(userName))) {
                iterator.remove(); // 예약 제거
                return true; // 취소 성공
            }
        }
        return false; // 취소 실패
    }

    // 예약 목록을 반환하는 메서드 (더미 데이터 포함)
    private List<Reservation> getReservations() {
        // 더미 데이터로 예약 목록 초기화
        List<Reservation> reservations = new ArrayList<>();
        reservations.add(new Reservation("홍길동", "101", "월요일", "1교시", "강의"));
        reservations.add(new Reservation("김철수", "102", "화요일", "2교시", "회의"));
        return reservations;
    }
}
