package pt.ulisboa.tecnico.cmov.cmovproject.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class is responsible for creating the SQLite Database.
 * It is on package model because we decided to merge domain and data access layers,
 * since the domain logic is not very complex and it would be way faster to implement
 * than totally splitting domain logic from storage (Table Data Gateway)
 */
public class MyOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    MyOpenHelper(Context context) {
        super(context, MyOpenHelper.class.getName(), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        populate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTables(SQLiteDatabase db) {
        final String MAIN_USER_CREATE = "CREATE TABLE MAIN_USER (name TEXT, email TEXT)";
        final String TABLE1_CREATE =
                "CREATE TABLE WORKSPACES (name TEXT, quota INTEGER, isPublic INTEGER);";
        final String TABLE2_CREATE =
                "CREATE TABLE USERS (nickname TEXT, email TEXT);";
        final String TABLE3_CREATE =
                "CREATE TABLE SUBSCRIPTIONS (user TEXT, workSpace TEXT, owner TEXT);";
        final String TABLE4_CREATE =
                "CREATE TABLE FILES (workSpace TEXT, name TEXT, size INTEGER);";
        final String TABLE5_CREATE =
                "CREATE TABLE TAGS (workSpace TEXT, tag TEXT);";
        final String[] CREATE_STRINGS = { MAIN_USER_CREATE, TABLE1_CREATE, TABLE2_CREATE, TABLE3_CREATE, TABLE4_CREATE, TABLE5_CREATE};

        for(String createStr : CREATE_STRINGS) {
            db.execSQL(createStr);
        }
    }


    private void populate(SQLiteDatabase db) {

        // Populate users' table
        String query = "INSERT INTO USERS VALUES(?, ?)";
        String[] args = new String[]{"Anna", "anna@gmail.com"};
        db.execSQL(query, args);
        args = new String[]{"Katherine", "kathie91_m@hotmail.com"};
        db.execSQL(query, args);
        args = new String[]{"Sarah", "sarah_w@tecnico.ulisboa.pt"};
        db.execSQL(query, args);

        // Populate workspaces' table
        query = "INSERT INTO WORKSPACES VALUES(?, ?, ?)";
        final String FALSE = "0";
        args = new String[]{"Deserts", Integer.toString(1024), FALSE};
        db.execSQL(query, args);
        args = new String[]{"Meat", Integer.toString(512), FALSE};
        db.execSQL(query, args);
        args = new String[]{"Fish", Integer.toString(1024), FALSE};
        db.execSQL(query, args);

        // Populate files' table
        query = "INSERT INTO FILES VALUES(?, ?, ?)";
        args = new String[]{"Deserts", "Banana Split", Integer.toString(8)};
        db.execSQL(query, args);
        args = new String[]{"Deserts", "Strawberry Cheesecake", Integer.toString(4)};
        db.execSQL(query, args);
        args = new String[]{"Deserts", "Brownie", Integer.toString(8)};
        db.execSQL(query, args);
        args = new String[]{"Fish", "Asian seared tuna", Integer.toString(16)};
        db.execSQL(query, args);
    }
}