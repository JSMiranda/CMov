package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;

import java.util.List;

public class AirDesk {
    private static AirDesk instance = null;
    private static Context context;

    private User mainUser;
    private List<User> otherUsers;

    public static AirDesk getInstance(Context context) {
        if (instance == null) {
            AirDesk.context = context;
            instance = new AirDesk();
        }
        return instance;
    }

    // FIXME: Singleton or static methods ? Choose one...
    static Context getContext() {
        return context;
    }

    public User getMainUser() {
        return mainUser;
    }

    private AirDesk() {
        mainUser = null;
        otherUsers = null;
        init("sarah_w@tecnico.ulisboa.pt"); // FIXME: hardcoded
    }

    public void init(String email) {
        List<User> users = User.sqlLoadUsers();
        for (User u : users) {
            if (u.getEmail().equals(email)) {
                mainUser = u;
                users.remove(u);
                break;
            }
        }
        otherUsers = users;
    }
}
