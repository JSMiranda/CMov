package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;
import pt.ulisboa.tecnico.cmov.cmovproject.exception.InvalidQuotaException;
import pt.ulisboa.tecnico.cmov.cmovproject.exception.WorkspaceAlreadyExistsException;

/**
 * A User has a set of workspaces (owned)
 * and is represented by their nickname and email.
 * A user is responsible for managing their own workspaces.
 */
public class User {
    private Map<String, OwnedWorkspace> ownedWorkSpaces;
    private Map<String, ForeignWorkspace> foreignWorkSpaces;
    private String nickname;
    private String email;

    /**
     * Initializes this {@code User}. Can be used to create a new user or load an existing one
     *
     * @param nickname User's nickname
     * @param email    User's email
     */
    public User(String nickname, String email) {
        this.ownedWorkSpaces = new HashMap<String, OwnedWorkspace>();
        this.foreignWorkSpaces = new HashMap<String, ForeignWorkspace>();
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
        // TODO: Remove these line in the 2nd part of the project
        List<User> listUsers = new ArrayList<User>();
        listUsers.addAll(users);
        listUsers.add(sqlLoadMainUser()); // assuming that is not null (can assume that)


        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM WORKSPACES";
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()) {
            String wsName = c.getString(0);
            int quota = c.getInt(1);
            boolean isPublic = c.getInt(2) == 0 ? false : true;

            // note that all workspaces in db are owned by the "main user"
            ownedWorkSpaces.put(wsName, new OwnedWorkspace(wsName, quota, isPublic, this));
            OwnedWorkspace ws = getOwnedWorkspaceByName(wsName);

            // for each workspace, create the file list, tag list, and the permitted users list
            ws.sqlLoadFiles();
            query = "SELECT tag FROM TAGS WHERE workSpace = ?";
            String[] args = new String[] {wsName};
            Cursor c2 = db.rawQuery(query, args);
            while(c2.moveToNext()) {
                ws.getTags().add(c2.getString(0)); // FIXME: violating encapsulation
            }
            query = "SELECT user FROM SUBSCRIPTIONS WHERE workSpace = ? AND owner = ?";
            args = new String[] {wsName, this.getEmail()};
            Cursor c3 = db.rawQuery(query, args);
            while(c3.moveToNext()) {
                // find by email. When found, add to permitted users.
                for(User u : listUsers) {
                    if(u.getEmail().equals(c3.getString(0))) {
                        ws.addPermittedUser(u);
                        // we do not need to add this ws to the user's foreign list,
                        // because they already have it
                        break;
                    }
                }
            }
        }
    }

    // TODO:
    // This method should be used only after networking is implemented.
    // At that point, think more carefully about visibility
    private synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO USERS VALUES(?, ?)";
        String[] args = new String[]{nickname, email};
        db.execSQL(query, args);
    }

    // TODO:
    // This method should be used only after networking is implemented.
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
    // This method should be used only after networking is implemented.
    // At that point, think more carefully about visibility
    private synchronized void sqlDelete() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM USERS WHERE name = ? AND email = ?";
        String[] args = new String[]{nickname, email};
        db.execSQL(query, args);
    }

    private synchronized void sqlInsertSubscription(OwnedWorkspace ws) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO SUBSCRIPTIONS VALUES(?, ?, ?)";
        String[] args = new String[]{this.getEmail(), ws.getName(), ws.getOwner().getEmail()};
        db.execSQL(query, args);
    }

    private synchronized void sqlDeleteSubscription(OwnedWorkspace ws) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM SUBSCRIPTIONS WHERE user = ? AND workSpace = ? AND owner = ?";
        String[] args = new String[]{this.getEmail(), ws.getName(), ws.getOwner().getEmail()};
        db.execSQL(query, args);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    public void createWorkspace(String name, int quota, boolean isPublic) throws WorkspaceAlreadyExistsException {
        if(ownedWorkSpaces.get(name) != null) {
            throw new WorkspaceAlreadyExistsException(name);
        }
        OwnedWorkspace ws = new OwnedWorkspace(name, quota, isPublic, this);
        ownedWorkSpaces.put(name, ws);
        ws.sqlInsert();
    }

    /**
     * Delete a given workspace. To get user's workspaces use {@link User#getOwnedWorkSpaces}
     *
     * @param ws instance of OwnedWorkspace to delete.
     */
    public void deleteWorkspace(OwnedWorkspace ws) {
        ws.delete();
        ownedWorkSpaces.remove(ws.getName());
    }

    public void subscribeWorkspace(ForeignWorkspace ws) {
        // TODO: Send msg
        // if ok, do
        foreignWorkSpaces.put(ws.getName(), ws);
        ws.sqlInsert();
    }

    public void unsubscribeWorkspace(ForeignWorkspace ws) {
        // TODO: Send msg
        // if ok, do
        foreignWorkSpaces.remove(ws.getName());
        ws.sqlDelete();
    }

    public void addUserToWorkSpace(String workSpaceName, User u) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.addPermittedUser(u);
    }

    public void removeUserFromWorkSpace(String workSpaceName, User u) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removePermittedUser(u);
    }

    public void shareWorkspace(String workspaceName, User user) {
        // TODO: Send msg
        // if ok, do
        addUserToWorkSpace(workspaceName, user);
    }

    public void unshareWorkspace(String workspaceName, User user) {
        // TODO: Send msg
        // if ok, do
        removeUserFromWorkSpace(workspaceName, user);
    }

    public void setWorkSpaceQuota(String workSpaceName, int quota) throws InvalidQuotaException {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setQuota(quota);
    }

    public void addTagToWorkSpace(String workSpaceName, String tag) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.addTag(tag);
    }

    public void removeTagFromWorkSpace(String workSpaceName, String tag) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeTag(tag);
    }

    public void removeAllTagsFromWorkSpace(String workSpaceName) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeAllTags();
    }

    public void addFileToWorkSpace(String workSpaceName, AirDeskFile f) throws FileAlreadyExistsException{
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.addFile(f);
    }

    public void removeFileFromWorkSpace(String workSpaceName, String fileName) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.removeFile(fileName);
    }

    public void setWorkSpaceToPublic(String workSpaceName) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setPublic(true);
    }

    public void setWorkSpaceToPrivate(String workSpaceName) {
        OwnedWorkspace ws = getOwnedWorkspaceByName(workSpaceName);
        ws.setPublic(false);
    }

    public void setWorkSpaceName(String oldName, String newName) throws WorkspaceAlreadyExistsException {
        if(!oldName.equals(newName) && ownedWorkSpaces.get(newName) != null) {
            throw new WorkspaceAlreadyExistsException(newName);
        }
        OwnedWorkspace ws = getOwnedWorkspaceByName(oldName);
        ws.setName(newName);
        ownedWorkSpaces.remove(oldName);
        ownedWorkSpaces.put(newName, ws);
    }

    public ArrayList<String> getOwnedWorkspaceNames() {
        ArrayList<String> workspaces = new ArrayList<String>();
        for(Map.Entry<String, OwnedWorkspace> entry: ownedWorkSpaces.entrySet()) {
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

    public Map<String, OwnedWorkspace> getOwnedWorkSpaces() {
        return ownedWorkSpaces;
    }

    public Map<String, ForeignWorkspace> getForeignWorkSpaces() {
        return foreignWorkSpaces;
    }

    public OwnedWorkspace getOwnedWorkspaceByName(String workSpaceName) {
        return ownedWorkSpaces.get(workSpaceName);
    }

 //   @Override
 //   public String toString(){ return nickname;
//    }


}
