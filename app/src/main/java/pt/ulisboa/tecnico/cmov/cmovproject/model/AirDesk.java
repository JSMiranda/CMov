package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.util.List;

/**
 * Singleton AirDesk
 */
public class AirDesk {
    private static AirDesk instance = null;
    private static Context context;

    private User mainUser;
    private List<User> otherUsers;
    private boolean isLoaded;

    /**
     * Singleton getter
     * @param context context where it is called
     * @return AirDesk Singleton
     */
    public static AirDesk getInstance(Context context) {
        if (instance == null) {
            AirDesk.context = context;
            instance = new AirDesk();
        }
        return instance;
    }

    // FIXME: a bit hacky... see if we could/should change this for a "global var" (application context)
    /**
     * Retrieves context that initialized the singleton
     * @return
     */
    static Context getContext() {
        return context;
    }

    /**
     * private constructor
     */
    private AirDesk() {
        mainUser = User.sqlLoadMainUser();
        otherUsers = null;

        if(mainUser != null) {
            List<User> users = User.sqlLoadUsers();
            otherUsers = users;
            mainUser.sqlLoadWorkspaces(users);
            isLoaded = true;
        } else {
            isLoaded = false;
        }
    }

    /**
     * Initializes the main user (log in)
     * @param email email of the user
     * @param nickName nick name of the user
     */
    public void init(String email, String nickName) {
        mainUser = new User(nickName, email);
        mainUser.sqlInsertMainUser();

        // The lines below should not be necessary if we did not have
        // the DB populated before using the app. (populating facilitates testing)
        List<User> users = User.sqlLoadUsers();
        otherUsers = users;
        mainUser.sqlLoadWorkspaces(users);
    }

    /**
     * Checks if external storage is available for read and write
     * @return true if it is available for read and write; false otherwise
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks if external storage is available to at least read
     * @return true if it is available to at least read; false otherwise.
     */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public User getMainUser() {
        return mainUser;
    }

    public List<User> getOtherUsers() {
        return otherUsers;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}
