package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirDesk {
    static AirDesk instance = null;

    private User mainUser;
    private List<User> otherUsers;

    public static AirDesk getInstance(String email) {
        if(instance == null) {
            instance = new AirDesk(email);
        }
        return instance;
    }

    public String getMainUserNickname() {
        return mainUser.getNickname();
    }

    public User getMainUser() {
        return mainUser;
    }

    private AirDesk(String email) {
        //TODO: Implement
        /*
        0) Create main user
        1) Create all users with email and username
        2) Create all workspaces with all the info except files
        3) Create (references) and load all files
        4) Add workspaces to users
        5) Add files to the workspaces
         */
        // Hardcoded for test
        mainUser = new User(loadMainUserNickname(), email);
        loadWorkSpaces();
    }

    private String loadMainUserNickname() {
        // TODO: This method should load the returned expression from the database, or create a new one if non existent
        return "Banana";
    }

    private Map<String, WorkSpace> getSubscribedWorkSpaces() {
        // TODO: This method should load the returned expression from the database, or create a new one if non existent

        return null;
    }

    private void loadWorkSpaces() {
        // TODO: This method should load the workspaces from the database, and add them to the respective users

        // Hard coded for test
        mainUser.createWorkspace("Banana", 1024, null, true);
        mainUser.createWorkspace("Peach", 2048, null, true);
    }
}
