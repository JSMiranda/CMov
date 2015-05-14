package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.StatFs;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;
import pt.ulisboa.tecnico.cmov.cmovproject.exception.InvalidQuotaException;

/**
 * A OwnedWorkspace is responsible for maintaining files
 * and maintaining a very simple ACL
 * (a list of permitted users) for itself.
 */
public class OwnedWorkspace extends Workspace {
    private Collection<User> permittedUsers;

    OwnedWorkspace(String name, int quota, boolean isPublic, User owner) {
        super(name, quota, owner, isPublic);
        this.permittedUsers = new ArrayList<User>();
        createStoringFolder();
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
            airDeskFiles.add(new AirDeskFile(fileName, size, false));
        }
    }

    public void delete(){
        for(User u : permittedUsers) {
            // TODO: send msg to u
        }
        sqlDelete();
        deleteStoredFolder();
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
        for (AirDeskFile f : airDeskFiles) {
            res += f.getSize();
        }
        return res;
    }


    /*
     * Getters and setters
     */

    @Override
    public Collection<AirDeskFile> getAirDeskFiles() {
        return airDeskFiles;
    }

    public ArrayList<String> getFileNames() {
        ArrayList<String> names = new ArrayList<>();
        for(AirDeskFile f : airDeskFiles) {
            names.add(f.getName());
        }
        return names;
    }

    public Collection<User> getPermittedUsers() {
        return permittedUsers;
    }

    @Override
    public void renameFile(String oldName, String newName) throws FileAlreadyExistsException {
        if(!oldName.equals(newName) && existsFile(newName))
            throw new FileAlreadyExistsException(newName);
        for(AirDeskFile airDeskFile : airDeskFiles) {
            if(airDeskFile.getName().equals(oldName)){
                airDeskFile.setName(rootFolder, newName);
                airDeskFile.sqlUpdate(oldName, name);
                break;
            }
        }
    }

    @Override
    public boolean saveFile(String fileName, String content) {
        AirDeskFile file = getFile(fileName);
        if((quota - getUsedQuota() + file.getSize()) >= content.length()) {
            file.saveFile(rootFolder, content, name, owner.getEmail());
            return true;
        }
        return false;
    }

    @Override
    public String openFileByName(String fileName){
        return getFile(fileName).readFile(rootFolder, name, owner.getEmail());
    }

    /*
     * Package access methods
     */

    void setQuota(int quota) throws InvalidQuotaException {
        final long max = getFreeMemory();
        final long usedQuota = getUsedQuota();
        if (quota >= usedQuota && quota < getFreeMemory()) {
            this.quota = quota;
            sqlUpdate(name);
        } else {
            throw new InvalidQuotaException(usedQuota, max);
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

    @Override
    void addFile(AirDeskFile f) throws FileAlreadyExistsException {
        if(existsFile(f.getName())) {
            throw new FileAlreadyExistsException(f.getName());
        }
        airDeskFiles.add(f);
        f.sqlInsert(name);
        saveFile(f.getName(),"");
    }

    @Override
    public void removeFile(String fileName) {
        AirDeskFile f = getFile(fileName);
        f.sqlDelete(name);
        f.deleteStoredFile(rootFolder);
        airDeskFiles.remove(f);
    }

    void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
        sqlUpdate(name);
    }

    @Override
    public boolean tryLock(String filename, String email, String workspacename) {
        AirDeskFile f = getFile(filename);
        if(f.tryLock()) {
            owner.putLock(email, workspacename, filename);
            return true;
        }
        return false;
    }

    @Override
    public void unlock(String filename, String email, String workspacename) {
        owner.removeLock(email);
        AirDeskFile f = getFile(filename);
        f.unlock();
    }

    void addPermittedUser(User u) {
        permittedUsers.add(u);
    }

    void removePermittedUser(User u) {
        permittedUsers.remove(u);
    }

    private long getFreeMemory() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
        return (statFs.getAvailableBlocks() * statFs.getBlockSize());
    }

    protected synchronized void sqlDelete() {
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

    synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO WORKSPACES VALUES(?, ?, ?)";
        String[] args = new String[]{name, Integer.toString(quota), isPublic ? "1" : "0"};
        db.execSQL(query, args);
    }
}

