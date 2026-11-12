package com.example.tpdm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Creates and upgrades the SQLite schema.
 * The table and column names live here only, so UserDB and the screens share one definition.
 */
public class SetupSqliteDB extends SQLiteOpenHelper {

    static final String TABLE_USER = "table_user";
    static final String COL_ID = "CIN";
    static final String COL_FNAME = "Firstname";
    static final String COL_LNAME = "Lastname";
    static final String COL_USER = "user";
    static final String COL_PASSWORD = "Password";
    static final String COL_ROLE = "Role";

    private static final String CREATE_DB = "CREATE TABLE " + TABLE_USER + " ("
            + COL_ID + " TEXT PRIMARY KEY, "
            + COL_FNAME + " TEXT, "
            + COL_LNAME + " TEXT, "
            + COL_USER + " TEXT NOT NULL, "
            + COL_PASSWORD + " TEXT NOT NULL, "
            + COL_ROLE + " TEXT NOT NULL"
            + ");";

    public SetupSqliteDB(@Nullable Context context, @Nullable String name,
                         @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}
