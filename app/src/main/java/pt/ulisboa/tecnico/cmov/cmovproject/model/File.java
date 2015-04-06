package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class abstracts a file. Before opening, closing and reading from a
 * file it is needed to set the implementation using {@link File#setImpl(FileImpl)}.
 */
public class File {
    private static FileImpl impl;

    private String name;
    private Date lastChangeTime; // not used for now
    private User lastChangeBy; // not used for now
    private int size;

    public File(String name, int size) {
        this.name = name;
        this.lastChangeTime = new Date();
        this.size = size;
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////// SQL operation methods ///////////////////////////
    //////////////////////////////////////////////////////////////////////

    synchronized void sqlInsert(String wsName, String ownerEmail) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO FILES VALUES(?, ?, ?, ?)";
        String[] args = new String[]{wsName, ownerEmail, name, Integer.toString(size)};
        db.execSQL(query, args);
    }

    synchronized void sqlUpdate(String oldName, String wsName, String ownerEmail) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("size", size);
        String[] whereArgs = new String[]{wsName, ownerEmail, oldName};
        db.update("FILES", values, "workSpace = ? AND owner = ? AND name = ?", whereArgs);
    }

    synchronized void sqlDelete(String wsName, String ownerEmail) {
        SQLiteOpenHelper dbHelper = new MyOpenHelper(AirDesk.getContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] whereArgs = new String[]{wsName, ownerEmail, name};
        db.delete("FILES", "workSpace = ? AND owner = ? AND name = ?", whereArgs);
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /**
     * Use this method to set an implementation for the file reader
     *
     * @param impl The implementation of the file reader
     */
    public static void setImpl(FileImpl impl) {
        File.impl = impl;
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
     * Only the method {@link File#write()} should use this method
     * (and this is why it is private). We are using an attribute
     * to speed up size queries.
     *
     * @param size
     */
    private void setSize(int size) {
        this.size = size;
    }
}
