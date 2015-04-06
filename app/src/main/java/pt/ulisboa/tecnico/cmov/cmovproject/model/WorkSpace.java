package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A WorkSpace is responsible for maintaining files
 * and maintaining a very simple ACL
 * (a list of permitted users) for itself.
 */
public class WorkSpace {
    private String name;
    private int quota;
    private Collection<String> tags;
    private Collection<File> files;
    private boolean isPublic;
    private Collection<User> permittedUsers;
    private User owner;

    WorkSpace(String name, int quota, boolean isPublic, User owner) {
        this.name = name;
        this.quota = quota;
        this.files = new ArrayList<File>();
        this.isPublic = isPublic;
        this.permittedUsers = new ArrayList<User>();
        this.owner = owner;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlLoadFiles() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT name, size FROM FILES WHERE workSpace = ? AND owner = ?";
        String[] args = new String[] {name, owner.getEmail()};
        Cursor c = db.rawQuery(query, args);
        while (c.moveToNext()) {
            String fileName = c.getString(0);
            int size = c.getInt(1);
            files.add(new File(fileName, size));
        }
    }

    synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO WORKSPACES VALUES(?, ?, ?, ?)";
        String[] args = new String[]{name, Integer.toString(quota), Boolean.toString(isPublic), owner.getEmail()};
        db.execSQL(query, args);
    }

    private synchronized void sqlUpdate(String previousName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "UPDATE WORKSPACES SET workSpace = ?, quota = ?, isPublic = ? WHERE name = ?";
        String[] args = new String[]{name, Integer.toString(quota), Boolean.toString(isPublic), previousName};
        db.execSQL(query, args);
        // TODO: Change other tables. Create another method to update when no name change is done
    }

    synchronized void sqlDelete() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM WORKSPACES WHERE name = ? AND owner = ?";
        String[] args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);

        query = "DELETE FROM FILES WHERE workSpace = ? AND owner = ?";
        args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);

        query = "DELETE FROM SUBSCRIPTIONS WHERE workSpace = ? AND owner = ?";
        args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);

        query = "DELETE FROM TAGS WHERE workSpace = ? AND owner = ?";
        args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);
    }

    private synchronized void sqlInsertTag(String tag) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO TAGS VALUES(?, ?, ?)";
        String[] args = new String[]{name, owner.getEmail(), tag};
        db.execSQL(query, args);
    }

    private synchronized void sqlDeleteTag(String tag) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM TAGS WHERE tag = ? AND workSpace = ? AND owner = ?";
        String[] args = new String[]{tag, name, owner.getEmail()};
        db.execSQL(query, args);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /*
     * Domain Logic
     */

    public int getUsedQuota() {
        int res = 0;
        for (File f : files) {
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

    public Collection<File> getFiles() {
        return files;
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
        for(File file : files) {
            if(file.getName().equals(name)){
                files.remove(file);
                file.sqlDelete(this.name, owner.getEmail());
                break;
            }
        }
    }

    public void renameFile(String oldName, String newName) {
        for(File file : files) {
            if(file.getName().equals(oldName)){
                file.setName(newName);
                file.sqlUpdate(oldName, name, owner.getEmail());
                break;
            }
        }
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

    void addFile(File f) {
        files.add(f);
        f.sqlInsert(name, owner.getEmail());
    }

    void removeFile(File f) {
        files.remove(f);
        f.sqlDelete(name, owner.getEmail());
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
