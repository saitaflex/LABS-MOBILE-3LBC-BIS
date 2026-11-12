package com.example.tpdm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Creates and upgrades the SQLite schema.
 * The table and column names live here only, so UserDB and the screens share one definition.
 *
 * Version history:
 * 1 - passwords stored in clear text (original lab).
 * 2 - passwords stored as salted hashes (see PasswordHasher).
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

    /** Migrates step by step and keeps the existing users (no DROP TABLE). */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            hashExistingPasswords(db);
        }
    }

    private void hashExistingPasswords(SQLiteDatabase db) {
        try (Cursor c = db.query(TABLE_USER, new String[]{COL_ID, COL_PASSWORD},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                String cin = c.getString(0);
                String password = c.getString(1);
                if (PasswordHasher.isHashed(password)) {
                    continue;
                }
                ContentValues values = new ContentValues();
                values.put(COL_PASSWORD, PasswordHasher.hash(password));
                db.update(TABLE_USER, values, COL_ID + "=?", new String[]{cin});
            }
        }
    }
}
