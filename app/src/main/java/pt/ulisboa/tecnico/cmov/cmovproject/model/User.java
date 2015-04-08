package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A User has a set of workspaces (owned)
 * and is represented by their nickname and email.
 * A user is responsible for managing their own workspaces.
 */
public class User {
    private Map<String, WorkSpace> ownedWorkSpaces;
    private Map<String, WorkSpace> subscribedWorkSpaces;
    private String nickname;
    private String email;

    /**
     * Initializes this {@code User}. Can be used to create a new user or load an existing one
     *
     * @param nickname User's nickname
     * @param email    User's email
     */
    public User(String nickname, String email) {
        this.ownedWorkSpaces = new HashMap<String, WorkSpace>();
        this.subscribedWorkSpaces = new HashMap<String, WorkSpace>();
        this.nickname = nickname;
        this.email = email;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    /**
     * Loads from the database all users.
     * @return The list of users
     */
    static synchronized List<User> sqlLoadUsers() {
        List<User> users = new ArrayList<User>();

        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM USERS";
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()) {
            String nickname = c.getString(0);
            String email = c.getString(1);
            users.add(new User(nickname, email));
        }

        return users;
    }

    /**
     * Loads from the database the logged in user.
     * @return The logged in user. null if no login was done
     */
    static synchronized User sqlLoadMainUser() {
        User user = null;

        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM MAIN_USER";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToNext()) {
            String nickname = c.getString(0);
            String email = c.getString(1);
            user = new User(nickname, email);
        }

        return user;
    }

    /**
     * Inserts this user as a main user (logged in).
     */
    synchronized void sqlInsertMainUser() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO MAIN_USER VALUES(?, ?)";
        String[] args = new String[]{nickname, email};
        db.execSQL(query, args);
    }


    /**
     * Loads from database all workspaces owned by this user
     * @param users The list of all users (needed to fill the permitted users list)
     */
    synchronized void sqlLoadWorkspaces(List<User> users) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM WORKSPACES";
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()) {
            String name = c.getString(0);
            int quota = c.getInt(1);
            boolean isPublic = c.getInt(2) == 0 ? false : true;

            // note that all workspaces in db are owned by the "main user"
            ownedWorkSpaces.put(name, new WorkSpace(name, quota, isPublic, this));
            WorkSpace ws = getOwnedWorkspaceByName(name);

            // for each workspace, create the file list, tag list, and the permitted users list
            ws.sqlLoadFiles();
            query = "SELECT tag FROM TAGS WHERE workSpace = ?";
            String[] args = new String[] {name};
            Cursor c2 = db.rawQuery(query, args);
            while(c2.moveToNext()) {
                ws.getTags().add(c2.getString(0)); // FIXME: violating encapsulation
            }
            query = "SELECT user FROM SUBSCRIPTIONS WHERE workSpace = ?";
            args = new String[] {name};
            Cursor c3 = db.rawQuery(query, args);
            while(c2.moveToNext()) {
                // find by email. When found, add to permitted users.
                for(User u : users) {
                    if(u.getEmail().equals(c2.getString(0))) {
                        ws.addPermittedUser(u);
                        subscribedWorkSpaces.put(ws.getName(), ws);
                        ws.addPermittedUser(this);
                        break;
                    }
                }
            }
        }
    }

    // TODO:
    // This method should be used only after connection is implemented.
    // At that point, think more carefully about visibility
    private synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO USERS VALUES(?, ?)";
        String[] args = new String[]{nickname, email};
        db.execSQL(query, args);
    }

    // TODO:
    // This method should be used only after connection is implemented.
    // At that point, think more carefully about visibility
    private synchronized void sqlUpdate(String oldNick, String oldEmail) {
        // TODO: Check if one can change his email. If not, correct this
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "UPDATE USERS SET name = ?, email = ? WHERE name = ? AND email = ?";
        String[] args = new String[]{nickname, email, oldNick, oldEmail};
        db.execSQL(query, args);
    }

    // TODO:
    // This method should be used only after connection is implemented.
    // At that point, think more carefully about visibility
    private synchronized void sqlDelete() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM USERS WHERE name = ? AND email = ?";
        String[] args = new String[]{nickname, email};
        db.execSQL(query, args);
    }

    private synchronized void sqlInsertSubscription(WorkSpace ws) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO SUBSCRIPTIONS VALUES(?, ?)";
        String[] args = new String[]{this.getEmail(), ws.getName()};
        db.execSQL(query, args);
    }

    private synchronized void sqlDeleteSubscription(WorkSpace ws) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM SUBSCRIPTIONS WHERE user = ?, workSpace = ?";
        String[] args = new String[]{this.getEmail(), ws.getName()};
        db.execSQL(query, args);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public void createWorkspace(String name, int quota, boolean isPublic) {
        WorkSpace ws = new WorkSpace(name, quota, isPublic, this);
        ownedWorkSpaces.put(name, ws);
        ws.sqlInsert();
    }

    /**
     * Delete a given workspace. To get user's workspaces use {@link User#getOwnedWorkSpaces}
     *
     * @param ws instance of WorkSpace to delete.
     */
    public void deleteWorkspace(WorkSpace ws) {
        ownedWorkSpaces.remove(ws.getName());
        // TODO: Remove all files and subscriptions
        ws.sqlDelete();
    }

    public void subscribeWorkspace(WorkSpace ws) {
        subscribedWorkSpaces.put(ws.getName(), ws);
        ws.addPermittedUser(this);
        sqlInsertSubscription(ws);
    }

    public void unsubscribeWorkspace(WorkSpace ws) {
        subscribedWorkSpaces.remove(ws.getName());
        ws.removePermittedUser(this);
        sqlDeleteSubscription(ws);
    }

    public void setWorkSpaceQuota(String workSpaceName, int quota) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setQuota(quota);
    }

    public void addTagToWorkSpace(String workSpaceName, String tag) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.addTag(tag);
    }

    public void removeTagFromWorkSpace(String workSpaceName, String tag) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeTag(tag);
    }

    public void removeAllTagsFromWorkSpace(String workSpaceName) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeAllTags();
    }

    public void addFileToWorkSpace(String workSpaceName, File f) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.addFile(f);
    }

    public void removeFileFromWorkSpace(String workSpaceName, File f) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeFile(f);
    }

    public void setWorkSpaceToPublic(String workSpaceName) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setPublic(true);
    }

    public void setWorkSpaceToPrivate(String workSpaceName) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setPublic(false);
    }

    public void setWorkSpaceName(String oldName, String newName) {
        WorkSpace ws = getOwnedWorkspaceByName(oldName);
        ws.setName(newName);
        ownedWorkSpaces.remove(oldName);
        ownedWorkSpaces.put(newName, ws);
    }

    public void addUserToWorkSpace(String workSpaceName, User u) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        u.subscribeWorkspace(ws);
    }

    public void removeUserFromWorkSpace(String workSpaceName, User u) {
        WorkSpace ws = getOwnedWorkspaceByName(workSpaceName);
        u.unsubscribeWorkspace(ws);
    }

    public ArrayList<String> getOwnedWorkspaceNames() {
        ArrayList<String> workspaces = new ArrayList<String>();
        for(Map.Entry<String, WorkSpace> entry: ownedWorkSpaces.entrySet()) {
            workspaces.add(entry.getValue().getName());
        }

        return workspaces;
    }

    /*
     * Getters
     */

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public Map<String, WorkSpace> getOwnedWorkSpaces() {
        return ownedWorkSpaces;
    }

    public Map<String, WorkSpace> getSubscribedWorkSpaces() {
        return subscribedWorkSpaces;
    }

    public WorkSpace getOwnedWorkspaceByName(String workSpaceName) {
        return ownedWorkSpaces.get(workSpaceName);
    }

    @Override
    public String toString(){
        return nickname;
    }
}
