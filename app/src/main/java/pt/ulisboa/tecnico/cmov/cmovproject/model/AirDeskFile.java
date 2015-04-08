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

/**
 * This class abstracts a file. Before opening, closing and reading from a
 * file it is needed to set the implementation using {@link AirDeskFile#setImpl(FileImpl)}.
 */
public class AirDeskFile {
    private static FileImpl impl;

    private String name;
    private Date lastChangeTime; // not used for now
    private User lastChangeBy; // not used for now
    private int size;
    private static final String filesRootFolder = "/airDesk/";

    public AirDeskFile(String name, int size) {
        this.name = name;
        this.lastChangeTime = new Date();
        this.size = size;
        saveFile("");
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

    /**
     * Use this method to set an implementation for the file reader
     *
     * @param impl The implementation of the file reader
     */
    public static void setImpl(FileImpl impl) {
        AirDeskFile.impl = impl;
    }

    public void close() {
        impl.close();
    }

    public String read() {
        return impl.read();
    }

    public void write() {
        impl.write();
        // TODO: Change size & last changes
    }

    public void open() {
        impl.open();
    }

    /*
     * Getters and setters
     */

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }


    /*
     * Private methods
     */

    /**
     * Only the method {@link AirDeskFile#write()} should use this method
     * (and this is why it is private). We are using an attribute
     * to speed up size queries.
     *
     * @param size
     */
    private void setSize(int size) {
        this.size = size;
    }

    void saveFile(String outputString) {
        if (isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(),filesRootFolder);
                if (!root.exists())
                    root.mkdirs();
                java.io.File file = new java.io.File(root, name + ".txt");
                FileWriter fWriter = new FileWriter(file);
                BufferedWriter bWriter = new BufferedWriter(fWriter);
                bWriter.write(outputString);
                bWriter.flush();
                bWriter.close();
                size = outputString.length();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public String readFile(){
        String strRet = "";
        if (isExternalStorageReadable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(), filesRootFolder);
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
                Log.e("login activity", "File not found: " + e.toString());;
            } catch (IOException e) {
                Log.e("login activity", "Can not read file: " + e.toString());
            }
        }
        return strRet;
    }

    /* Checks if external storage is available for read and write */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;
        return false;
    }

    /* Checks if external storage is available to at least read */
    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            return true;
        return false;
    }

    public void deleteStoredFile() {
        if (isExternalStorageWritable()) {
            try {
                java.io.File root = new java.io.File(Environment.getExternalStorageDirectory(),filesRootFolder);
                if (!root.exists())
                    root.mkdirs();
                java.io.File file = new java.io.File(root, name + ".txt");
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }
}
