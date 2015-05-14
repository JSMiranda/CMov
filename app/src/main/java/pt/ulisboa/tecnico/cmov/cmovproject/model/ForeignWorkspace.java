package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Collection;

import pt.ulisboa.tecnico.cmov.cmovproject.exception.FileAlreadyExistsException;

public class ForeignWorkspace extends Workspace {

    ForeignWorkspace(String name, User owner) {
        super(name, 0, owner, false); //FIXME
        createStoringFolder();
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlInsert() {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO SUBSCRIPTIONS VALUES(?, ?, ?)";
        String[] args = new String[]{AirDesk.getInstance().getMainUser().getEmail(), name, owner.getEmail()};
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
        AirDeskFile file = getFile(fileName);
        if(file == null)
            return false;
        file.saveFile(rootFolder, content, name, owner.getEmail());
        return true;
    }

    @Override
    public String openFileByName(String fileName){
        return getFile(fileName).readFile(rootFolder, name, owner.getEmail());
    }

    @Override
    public boolean tryLock(String filename, String email, String workspacename) {
        AirDesk.getInstance().getConnService().tryLock(filename, email, workspacename, owner.getEmail());
        while(!getFile(filename).isLockMessageReceived()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return getFile(filename).isLocked();
    }

    @Override
    public void unlock(String filename, String email, String workspacename) {
        AirDesk.getInstance().getConnService().unlock(filename, email, workspacename, owner.getEmail());
    }

    @Override
    void addFile(AirDeskFile f) throws FileAlreadyExistsException {
        // TODO: Send message
    }

    public void notifyAddedFile(String fileName) {
        airDeskFiles.add(new AirDeskFile(fileName, 0, true));
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

