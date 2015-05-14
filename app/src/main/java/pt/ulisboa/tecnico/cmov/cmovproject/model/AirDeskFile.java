package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.cmovproject.connectivity.ConnectivityService;

public class AirDeskFile {
    private String name;
    private Date lastChangeTime; // not used for now
    private User lastChangeBy; // not used for now
    private int size;
    private boolean isShared = false;

    public void setShadowContent(String shadowContent) {
        this.shadowContent = shadowContent;
    }

    private String shadowContent = null;

    public void setFetched(boolean isFetched) {
        this.isFetched = isFetched;
    }

    public boolean isFetched() {
        return isFetched;
    }

    private boolean isFetched = true;

    public AirDeskFile(String name, int size, boolean isShared) {
        this.name = name;
        this.lastChangeTime = new Date();
        this.size = size;
        this.isShared = isShared;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlInsert(String wsName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO FILES VALUES(?, ?, ?)";
        String[] args = new String[]{wsName, name, Integer.toString(size)};
        db.execSQL(query, args);
    }

    synchronized void sqlUpdate(String oldName, String wsName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("size", size);
        String[] whereArgs = new String[]{wsName, oldName};
        db.update("FILES", values, "workSpace = ? AND name = ?", whereArgs);
    }

    synchronized void sqlDelete(String wsName) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] whereArgs = new String[]{wsName, name};
        db.delete("FILES", "workSpace = ? AND name = ?", whereArgs);
    }


    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /*
     * Getters and setters
     */

    public String getName() {
        return name;
    }

    void setName(String rootFolder, String newFileName) {
        renameStoredFile(rootFolder, newFileName);
        this.name = newFileName;
    }

    public int getSize() {
        return size;
    }


    /*
     * Private methods
     */

    /**
     * Only the method {@link AirDeskFile#saveFile(String, String)} ()} should use this method
     * (and this is why it is private). We are using an attribute
     * to speed up size queries.
     *
     * @param size
     */
    private void setSize(int size) {
        this.size = size;
    }

    void saveFile(String rootFolder, String outputString, String workspaceName, String ownerEmail) {
        if(!isShared) {
            if (AirDesk.isExternalStorageWritable()) {
                try {
                    java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), rootFolder);
                    if (!root.exists())
                        root.mkdirs();
                    java.io.File file = new java.io.File(root, name + ".txt");
                    FileWriter fWriter = new FileWriter(file);
                    BufferedWriter bWriter = new BufferedWriter(fWriter);
                    bWriter.write(outputString);
                    bWriter.flush();
                    bWriter.close();
                    setSize(outputString.length());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            AirDesk.getInstance().getConnService().saveFile(workspaceName, name, ownerEmail, outputString);
        }

    }

    String readFile(String rootFolder, String workspaceName, String ownerEmail){
        String strRet = "";
        if(!isShared) {
            if (AirDesk.isExternalStorageReadable()) {
                try {
                    java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), rootFolder);
                    java.io.File file = new java.io.File(root, name + ".txt");
                    InputStream secondInputStream = new BufferedInputStream(
                            new FileInputStream(file));
                    BufferedReader bReader = new BufferedReader(new InputStreamReader(
                            secondInputStream));
                    StringBuilder total = new StringBuilder();
                    String line = bReader.readLine();
                    if(line!=null)
                        total.append(line);
                    while ((line = bReader.readLine()) != null) {
                        total.append("\n").append(line);
                    }
                    bReader.close();
                    secondInputStream.close();
                    strRet = total.toString();
                } catch (FileNotFoundException e) {
                    Log.e("login activity", "File not found: " + e.toString());
                } catch (IOException e) {
                    Log.e("login activity", "Can not read file: " + e.toString());
                }
            }
        } else {
            isFetched = false; //setting fetch to false the activity will have to wait
            AirDesk.getInstance().getConnService().fetchFile(workspaceName, name, ownerEmail);
            while(!isFetched()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            strRet = shadowContent;
        }


        return strRet;
    }

    void deleteStoredFile(String rootFolder) {
        if (AirDesk.isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), rootFolder);
                if (root.exists()) {
                    java.io.File file = new java.io.File(root, name + ".txt");
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void renameStoredFile(String rootFolder, String newFileName) {
        if (AirDesk.isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), rootFolder);
                if (root.exists()) {
                    java.io.File fileOld = new java.io.File(root, name + ".txt");
                    java.io.File fileNew = new java.io.File(root, newFileName + ".txt");
                    fileOld.renameTo(fileNew);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
