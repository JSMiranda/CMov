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

    public List<User> getOtherUsers() {
        return otherUsers;
    }

    private AirDesk() {
        mainUser = null;
        otherUsers = null;
        init("sarah_w@tecnico.ulisboa.pt"); // FIXME: hardcoded
    }

    public void init(String email) {
        // FIXME: hardcoded, change for "sqlInsertMainUser", and move sqlLoadWorkspaces to WorkSpace class
        mainUser = new User("Sarah", "sarah_w@tecnico.ulisboa.pt");
        List<User> users = User.sqlLoadUsers();
        otherUsers = users;
        mainUser.sqlLoadWorkspaces(users);
    }

    /**
     * Loads the AirDesk.
     *
     * @return true if successful, false otherwise. If the return is false,
     * the method {@link #init(String)} should be called
     */
    public boolean load() {
        // TODO: Implement (in user class, and call here) load main user
        return true;
    }
}
