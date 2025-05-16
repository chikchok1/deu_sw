package Model;

public class Session {
    private static String loggedInUserId;
    private static String loggedInUserName; 
    private static String loggedInUserRole; // 사용자 권한 추가

    public static void setLoggedInUserId(String userId) {
        loggedInUserId = userId;
    }

    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserName(String userName) {
        loggedInUserName = userName;
    }

    public static String getLoggedInUserName() {
        return loggedInUserName;
    }

    public static void clear() {
        loggedInUserId = null;
        loggedInUserName = null;
         loggedInUserRole = null; // 역할도 초기화
    }
    public static void setLoggedInUserRole(String role) {
    loggedInUserRole = role;
    }

    public static String getLoggedInUserRole() {
    return loggedInUserRole;
    }

}
