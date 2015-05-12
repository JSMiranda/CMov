package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;

public abstract class Workspace {
    protected String name;
    protected int quota;
    protected Collection<String> tags;
    protected boolean isPublic;
    protected User owner;
    protected String rootFolder;
    protected Collection<AirDeskFile> airDeskFiles;

    public Workspace(String name, int quota, User owner, boolean isPublic) {
        this.tags = new ArrayList<String>();
        this.name = name;
        this.quota = quota;
        this.owner = owner;
        rootFolder ="/airDesk/"+this.name;
        this.isPublic = isPublic;
        this.airDeskFiles = new ArrayList<AirDeskFile>();
    }

    abstract void sqlInsert();

    protected synchronized void sqlUpdate(String previousName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "UPDATE WORKSPACES SET name = ?, quota = ?, isPublic = ? WHERE name = ?";
        String[] args = new String[]{name, Integer.toString(quota), isPublic ? "1" : "0", previousName};
        db.execSQL(query, args);

        if (!previousName.equals(name)) {
            // These tables only need to be updated if we change workspace name
            query = "UPDATE TAGS SET workSpace = ? WHERE workSpace = ?";
            args = new String[]{name, previousName};
            db.execSQL(query, args);

            query = "UPDATE FILES SET workSpace = ? WHERE workSpace = ?";
            args = new String[]{name, previousName};
            db.execSQL(query, args);
        }
    }

    protected abstract void sqlDelete();

    public String getName() {
        return name;
    }

    public int getQuota() {
        return quota;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public abstract Collection<AirDeskFile> getAirDeskFiles();

    public AirDeskFile getFile(String fileName) {
        for (AirDeskFile airDeskFile : airDeskFiles) {
            if (airDeskFile.getName().equals(fileName)) {
                return airDeskFile;
            }
        }

        return null;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public User getOwner() {
        return owner;
    }

    public abstract void renameFile(String oldName, String newName) throws FileAlreadyExistsException;

    public abstract boolean saveFile(String fileName, String content);

    public abstract String openFileByName(String fileName);

    abstract void addFile(AirDeskFile f) throws FileAlreadyExistsException;

    protected boolean existsFile(String name) {
        for(AirDeskFile file : airDeskFiles) {
            if(file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public abstract void removeFile(String fileName);

    void setName(String name) {
        String prevName = this.name;
        this.name = name;
        sqlUpdate(prevName);
    }

    protected void createStoringFolder() {
        if (AirDesk.isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(),rootFolder);
                if (!root.exists())
                    root.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void deleteStoredFolder() {
        if (AirDesk.isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(),rootFolder);
                if (root.exists())
                    root.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
