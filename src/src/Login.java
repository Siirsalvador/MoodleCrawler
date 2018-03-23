import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Scanner;


public class Login {

    private static Exception e;

    /**
     * Authenticates user with the moodle platform
     * @return returns a session cookie
     */
    public static String getCookie() {

        Connection.Response res = null;

        try {
            res = Jsoup.connect(Const.MOODLE_BASE_ADDRESS + Const.LOGIN_ADDRESS)
                    .data("username", Const.USER_NAME)
                    .data("password", Const.PASSWORD)
                    .method(Connection.Method.POST)
                    .execute();
        } catch (Exception e) {
            Login.e = e;
            errorCheck(e);
            return null;
        }

        return res.cookie("MoodleSession");
    }

    /**
     * prints out an error message based of the exception passed to it
     * Gives the user the option to retry or cancel
     * @param e exception that occurred
     * @return null
     */
    private static void errorCheck(@NotNull Exception e) {

        System.out.println("[AN ERROR OCCURRED]  ++++  " + e.toString());
        System.out.println("[PLEASE CHECK INTERNET CONNECTION!]");
        System.out.println("[PRESS 'Y' TO RETRY AND 'N' TO CANCEL]");
        Scanner scanner = new Scanner(System.in);
        String retry = scanner.nextLine();

        switch (retry.toUpperCase()) {
            case "Y":
                getCookie();
                break;
            case "N":
                System.out.println("++++  [EXITING APPLICATION]  ++++");
                System.exit(0);
            default:
                System.out.println("[ENTER VALID RESPONSE!]");
                errorCheck(e);
                break;
        }
    }
}
