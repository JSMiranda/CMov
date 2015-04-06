package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirDesk {
    private static AirDesk instance = null;
    private static Context context;

    private User mainUser;
    private List<User> otherUsers;

    public static AirDesk getInstance(String email, Context context) {
        if(instance == null) {
            AirDesk.context = context;
            instance = new AirDesk(email);
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

    private AirDesk(String email) {
        List<User> users = User.sqlLoadUsers();
        for(User u : users) {
            if(u.getEmail().equals(email)) {
                mainUser = u;
                users.remove(u);
                break;
            }
        }
        otherUsers = users;
    }
}
