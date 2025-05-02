package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
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
}
