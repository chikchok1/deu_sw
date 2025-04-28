package Model;

public class Session {
    private static String loggedInUserId;
    private static String loggedInUserName; 

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
    }
}
