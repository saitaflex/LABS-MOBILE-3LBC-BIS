package com.example.tpdm;

import static com.example.tpdm.SetupSqliteDB.COL_FNAME;
import static com.example.tpdm.SetupSqliteDB.COL_ID;
import static com.example.tpdm.SetupSqliteDB.COL_LNAME;
import static com.example.tpdm.SetupSqliteDB.COL_PASSWORD;
import static com.example.tpdm.SetupSqliteDB.COL_ROLE;
import static com.example.tpdm.SetupSqliteDB.COL_USER;
import static com.example.tpdm.SetupSqliteDB.TABLE_USER;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access class: every SQL operation on users goes through here.
 * User input is always passed through "?" placeholders, never concatenated (SQL injection).
 */
public class UserDB {

    private static final int VERSION_DB = 1;
    private static final String DB_NAME = "Bis.db";

    private SQLiteDatabase db;
    private final SetupSqliteDB mydb;

    public UserDB(Context context) {
        mydb = new SetupSqliteDB(context.getApplicationContext(), DB_NAME, null, VERSION_DB);
    }

    public void open() {
        db = mydb.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public SQLiteDatabase getDB() {
        return db;
    }

    /** @return the new row id, or -1 if the insertion failed (for example CIN already used). */
    public long insertUser(User user) {
        ContentValues values = new ContentValues();
        values.put(COL_ID, user.getCin());
        values.put(COL_FNAME, user.getFname());
        values.put(COL_LNAME, user.getLname());
        values.put(COL_USER, user.getUsername());
        values.put(COL_PASSWORD, user.getPwd());
        values.put(COL_ROLE, user.getRole());
        // CONFLICT_NONE behaves like insert() but reports a duplicate CIN with -1
        // instead of logging a stack trace.
        try {
            return db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_NONE);
        } catch (android.database.SQLException e) {
            return -1;
        }
    }

    /** @return the number of users matching this username / password pair. */
    public int getUserWithLog(User user) {
        try (Cursor c = db.rawQuery(
                "SELECT * FROM " + TABLE_USER + " WHERE " + COL_USER + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{user.getUsername(), user.getPwd()})) {
            return c.getCount();
        }
    }

    /**
     * @param excludeCin CIN of the user being edited, ignored in the check; null on registration.
     * @return true if another user already owns this username.
     */
    public boolean usernameExists(String username, @Nullable String excludeCin) {
        String selection = COL_USER + "=?";
        String[] args = new String[]{username};
        if (excludeCin != null) {
            selection += " AND " + COL_ID + "<>?";
            args = new String[]{username, excludeCin};
        }
        return DatabaseUtils.queryNumEntries(db, TABLE_USER, selection, args) > 0;
    }

    /** @return the role of this username, or null if it does not exist. */
    @Nullable
    public String getUserRole(String username) {
        try (Cursor cursor = db.query(
                TABLE_USER,
                new String[]{COL_ROLE},
                COL_USER + "=?",
                new String[]{username},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE));
            }
            return null;
        }
    }

    public Cursor getAllEmployees() {
        return db.query(
                TABLE_USER,
                null,
                COL_ROLE + "=?",
                new String[]{User.ROLE_EMPLOYEE},
                null,
                null,
                COL_FNAME + " ASC");
    }

    public long countEmployees() {
        return DatabaseUtils.queryNumEntries(db, TABLE_USER,
                COL_ROLE + "=?", new String[]{User.ROLE_EMPLOYEE});
    }

    /**
     * Employees sorted by first name, copied out of the Cursor so it can be closed right away.
     *
     * @param firstNameFilter keeps only first names containing this text; empty = everyone.
     */
    public List<User> getEmployees(String firstNameFilter) {
        List<User> employees = new ArrayList<>();
        try (Cursor cursor = firstNameFilter.isEmpty()
                ? getAllEmployees()
                : db.query(
                        TABLE_USER,
                        null,
                        COL_ROLE + "=? AND " + COL_FNAME + " LIKE ?",
                        new String[]{User.ROLE_EMPLOYEE, "%" + firstNameFilter + "%"},
                        null,
                        null,
                        COL_FNAME + " ASC")) {
            while (cursor.moveToNext()) {
                User u = new User();
                u.setCin(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
                u.setFname(cursor.getString(cursor.getColumnIndexOrThrow(COL_FNAME)));
                u.setLname(cursor.getString(cursor.getColumnIndexOrThrow(COL_LNAME)));
                u.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER)));
                u.setPwd(cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD)));
                u.setRole(User.ROLE_EMPLOYEE);
                employees.add(u);
            }
        }
        return employees;
    }

    /** @return the number of rows updated (0 = employee not found). */
    public int updateEmployee(String cin, User user) {
        ContentValues values = new ContentValues();
        values.put(COL_FNAME, user.getFname());
        values.put(COL_LNAME, user.getLname());
        values.put(COL_USER, user.getUsername());
        values.put(COL_PASSWORD, user.getPwd());

        return db.update(TABLE_USER, values,
                COL_ID + "=? AND " + COL_ROLE + "=?",
                new String[]{cin, User.ROLE_EMPLOYEE});
    }

    /** @return the number of rows deleted (0 = employee not found). */
    public int deleteEmployee(String cin) {
        return db.delete(TABLE_USER,
                COL_ID + "=? AND " + COL_ROLE + "=?",
                new String[]{cin, User.ROLE_EMPLOYEE});
    }
}
