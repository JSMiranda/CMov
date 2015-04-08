package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A WorkSpace is responsible for maintaining airDeskFiles
 * and maintaining a very simple ACL
 * (a list of permitted users) for itself.
 */
public class WorkSpace {
    private String name;
    private int quota;
    private Collection<String> tags;
    private Collection<airDeskFile> airDeskFiles;
    private boolean isPublic;
    private Collection<User> permittedUsers;
    private User owner;

    WorkSpace(String name, int quota, boolean isPublic, User owner) {
        this.name = name;
        this.quota = quota;
        this.airDeskFiles = new ArrayList<airDeskFile>();
        this.isPublic = isPublic;
        this.tags = new ArrayList<String>();
        this.permittedUsers = new ArrayList<User>();
        this.owner = owner;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlLoadFiles() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT name, size FROM FILES WHERE workSpace = ?";
        String[] args = new String[] {name};
        Cursor c = db.rawQuery(query, args);
        while (c.moveToNext()) {
            String fileName = c.getString(0);
            int size = c.getInt(1);
            airDeskFiles.add(new airDeskFile(fileName, size));
        }
    }

    synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO WORKSPACES VALUES(?, ?, ?)";
        String[] args = new String[]{name, Integer.toString(quota), isPublic ? "1" : "0"};
        db.execSQL(query, args);
    }

    private synchronized void sqlUpdate(String previousName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "UPDATE WORKSPACES SET name = ?, quota = ?, isPublic = ? WHERE name = ?";
        String[] args = new String[]{name, Integer.toString(quota), isPublic ? "1" : "0", previousName};
        db.execSQL(query, args);
        // TODO: Change other tables. Create another method to update when no name change is done
    }

    synchronized void sqlDelete() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM WORKSPACES WHERE name = ?";
        String[] args = new String[]{name};
        db.execSQL(query, args);

        query = "DELETE FROM FILES WHERE workSpace = ?";
        args = new String[]{name};
        db.execSQL(query, args);

        query = "DELETE FROM SUBSCRIPTIONS WHERE workSpace = ? AND owner = ?";
        args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);

        query = "DELETE FROM TAGS WHERE workSpace = ?";
        args = new String[]{name};
        db.execSQL(query, args);
    }

    private synchronized void sqlInsertTag(String tag) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO TAGS VALUES(?, ?)";
        String[] args = new String[]{name, tag};
        db.execSQL(query, args);
    }

    private synchronized void sqlDeleteTag(String tag) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM TAGS WHERE tag = ? AND workSpace = ?";
        String[] args = new String[]{tag, name};
        db.execSQL(query, args);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /*
     * Domain Logic
     */

    public int getUsedQuota() {
        int res = 0;
        for (airDeskFile f : airDeskFiles) {
            res += f.getSize();
        }
        return res;
    }


    /*
     * Getters and setters
     */

    public String getName() {
        return name;
    }

    public int getQuota() {
        return quota;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public Collection<airDeskFile> getAirDeskFiles() {
        return airDeskFiles;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Collection<User> getPermittedUsers() {
        return permittedUsers;
    }

    public User getOwner() {
        return owner;
    }

    public void removeFileByName(String name) {
        for(airDeskFile airDeskFile : airDeskFiles) {
            if(airDeskFile.getName().equals(name)){
                airDeskFiles.remove(airDeskFile);
                airDeskFile.sqlDelete(this.name);
                break;
            }
        }
    }

    public void renameFile(String oldName, String newName) {//in which workspace? new name already exists in this workspace?
        for(airDeskFile airDeskFile : airDeskFiles) {
            if(airDeskFile.getName().equals(oldName)){
                airDeskFile.setName(newName);
                airDeskFile.sqlUpdate(oldName, name);
                break;
            }
        }
    }

    public void removeAllPermittedUsers(){
        permittedUsers.clear();
    }

    /*
     * Package access methods
     */

    void setQuota(int quota) {
        if (quota >= getUsedQuota()) {
            this.quota = quota;
            sqlUpdate(name);
        } else {
            throw new IllegalStateException("Trying to set quota to a value lower than used quota");
        }
    }

    void addTag(String tag) {
        tags.add(tag);
        sqlInsertTag(tag);
    }

    void removeTag(String tag) {
        tags.remove(tag);
        sqlDeleteTag(tag);
    }

    void removeAllTags() {
        for(String tag : tags) {
            sqlDeleteTag(tag);
        }
        tags.clear();
    }

    void addFile(airDeskFile f) {
        airDeskFiles.add(f);
        f.sqlInsert(name);
    }

    void removeFile(airDeskFile f) {
        airDeskFiles.remove(f);
        f.sqlDelete(name);
    }

    void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        sqlUpdate(name);
    }

    void setName(String name) {
        String prevName = this.name;
        this.name = name;
        sqlUpdate(prevName);
    }

    void addPermittedUser(User u) {
        permittedUsers.add(u);
    }

    void removePermittedUser(User u) {
        permittedUsers.remove(u);
    }

}
