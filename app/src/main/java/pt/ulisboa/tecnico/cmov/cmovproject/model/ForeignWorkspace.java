package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;

public class ForeignWorkspace extends Workspace {

    ForeignWorkspace(String name, int quota, boolean isPublic, User owner) {
        super(name, quota, owner, isPublic);
        createStoringFolder();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO SUBSCRIPTIONS VALUES(?, ?)";
        String[] args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);
    }

    protected synchronized void sqlDelete() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "DELETE FROM SUBSCRIPTIONS WHERE workSpace = ? AND owner = ?";
        String[] args = new String[]{name, owner.getEmail()};
        db.execSQL(query, args);
    }

    //////////////////////////////////////////////////////////////////////

    /*
     * Getters and setters
     */

    @Override
    public Collection<AirDeskFile> getAirDeskFiles() {
        return airDeskFiles;
    }

    @Override
    public void renameFile(String oldName, String newName) throws FileAlreadyExistsException {
        if(!oldName.equals(newName) && existsFile(newName))
            ;// TODO: send msg
    }

    public void notifyFileRenamed(String oldName, String newName) {
        for(AirDeskFile airDeskFile : airDeskFiles) {
            if(airDeskFile.getName().equals(oldName)){
                airDeskFile.setName(rootFolder, newName);
                break;
            }
        }
    }

    @Override
    public boolean saveFile(String fileName, String content) {
        // TODO: send msg
        return false;
    }

    @Override
    public String openFileByName(String fileName){
        return null;
        // TODO: send msg ........ getFile(fileName).readFile(rootFolder);
    }

    @Override
    void addFile(AirDeskFile f) throws FileAlreadyExistsException {
        // TODO: Send message
    }

    public void notifyAddedFile(String fileName) {
        airDeskFiles.add(new AirDeskFile(fileName, 0));
    }

    @Override
    public void removeFile(String fileName) {
        // TODO: Send message
    }


    public void notifyFileRemoved(String fileName) {
        AirDeskFile f = getFile(fileName);
        airDeskFiles.remove(f);
    }
}

