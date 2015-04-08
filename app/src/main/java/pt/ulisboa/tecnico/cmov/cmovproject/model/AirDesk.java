package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;
import android.os.Environment;

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
    }

    public void init(String email, String nickName) {
        mainUser = new User(nickName, email);
        mainUser.sqlInsertMainUser();

        // The lines below should not be necessary if we did not have
        // the DB populated before using the app.
        List<User> users = User.sqlLoadUsers();
        otherUsers = users;
        mainUser.sqlLoadWorkspaces(users);
    }

    /**
     * Loads the AirDesk.
     *
     * @return true if successful, false otherwise. If the return is false,
     * the method {@link #init(String, String)} should be called
     */
    public boolean load() {
        mainUser = User.sqlLoadMainUser();

        if(mainUser != null) {
            List<User> users = User.sqlLoadUsers();
            otherUsers = users;
            mainUser.sqlLoadWorkspaces(users);
            return true;
        } else {
            return false;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
