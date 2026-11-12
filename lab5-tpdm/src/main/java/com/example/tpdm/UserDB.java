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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access class: every SQL operation on users goes through here.
 * User input is always passed through "?" placeholders, never concatenated (SQL injection).
 * Passwords are hashed before they are written (see PasswordHasher).
 */
public class UserDB {

    private static final int VERSION_DB = 2;
    private static final String DB_NAME = "Bis.db";

    private SQLiteDatabase db;
    private final SetupSqliteDB mydb;

    public UserDB(Context context) {
        this(context, DB_NAME);
    }

    /** @param dbName database file name; null creates an in-memory database (used by tests). */
    UserDB(Context context, @Nullable String dbName) {
        mydb = new SetupSqliteDB(context.getApplicationContext(), dbName, null, VERSION_DB);
    }

    public void open() {
        db = mydb.getWritableDatabase();
    }

    public void close() {
        mydb.close();
    }

    public SQLiteDatabase getDB() {
        return db;
    }

    // ===================== CREATE =====================

    /** @return the new row id, or -1 if the insertion failed (for example CIN already used). */
    public long insertUser(User user) {
        ContentValues values = new ContentValues();
        values.put(COL_ID, user.getCin());
        values.put(COL_FNAME, user.getFname());
        values.put(COL_LNAME, user.getLname());
        values.put(COL_USER, user.getUsername());
        values.put(COL_PASSWORD, PasswordHasher.hash(user.getPwd()));
        values.put(COL_ROLE, user.getRole());
        try {
            // Like insert(), but a duplicate CIN is reported with -1 without logging a stack trace.
            return db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_NONE);
        } catch (SQLException e) {
            return -1;
        }
    }

    // ===================== READ =====================

    /** @return true if this username exists and the password matches its stored hash. */
    public boolean checkCredentials(String username, String password) {
        try (Cursor c = db.query(TABLE_USER, new String[]{COL_PASSWORD},
                COL_USER + "=?", new String[]{username}, null, null, null)) {
            while (c.moveToNext()) {
                if (PasswordHasher.verify(password, c.getString(0))) {
                    return true;
                }
            }
            return false;
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
        try (Cursor cursor = db.query(TABLE_USER, new String[]{COL_ROLE},
                COL_USER + "=?", new String[]{username}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COL_ROLE));
            }
            return null;
        }
    }

    public long countByRole(String role) {
        return DatabaseUtils.queryNumEntries(db, TABLE_USER, COL_ROLE + "=?", new String[]{role});
    }

    public long countEmployees() {
        return countByRole(User.ROLE_EMPLOYEE);
    }

    /**
     * Employees sorted by first name, copied out of the Cursor so it can be closed right away.
     * The password hash is deliberately not loaded: no screen needs it.
     *
     * @param firstNameFilter keeps only first names containing this text; empty = everyone.
     */
    public List<User> getEmployees(String firstNameFilter) {
        String selection = COL_ROLE + "=?";
        String[] args = new String[]{User.ROLE_EMPLOYEE};
        if (!firstNameFilter.isEmpty()) {
            selection += " AND " + COL_FNAME + " LIKE ?";
            args = new String[]{User.ROLE_EMPLOYEE, "%" + firstNameFilter + "%"};
        }

        List<User> employees = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_USER,
                new String[]{COL_ID, COL_FNAME, COL_LNAME, COL_USER},
                selection, args, null, null, COL_FNAME + " COLLATE NOCASE ASC")) {
            while (cursor.moveToNext()) {
                User u = new User();
                u.setCin(cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)));
                u.setFname(cursor.getString(cursor.getColumnIndexOrThrow(COL_FNAME)));
                u.setLname(cursor.getString(cursor.getColumnIndexOrThrow(COL_LNAME)));
                u.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER)));
                u.setRole(User.ROLE_EMPLOYEE);
                employees.add(u);
            }
        }
        return employees;
    }

    // ===================== UPDATE =====================

    /**
     * Updates an employee. The password only changes when a new one is provided.
     *
     * @return the number of rows updated (0 = employee not found).
     */
    public int updateEmployee(String cin, User user) {
        ContentValues values = new ContentValues();
        values.put(COL_FNAME, user.getFname());
        values.put(COL_LNAME, user.getLname());
        values.put(COL_USER, user.getUsername());
        if (user.getPwd() != null && !user.getPwd().isEmpty()) {
            values.put(COL_PASSWORD, PasswordHasher.hash(user.getPwd()));
        }

        return db.update(TABLE_USER, values,
                COL_ID + "=? AND " + COL_ROLE + "=?",
                new String[]{cin, User.ROLE_EMPLOYEE});
    }

    /**
     * Sets a new password for the account matching both the username and the CIN.
     *
     * @return the number of rows updated (0 = no such username / CIN pair).
     */
    public int resetPassword(String username, String cin, String newPassword) {
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, PasswordHasher.hash(newPassword));
        return db.update(TABLE_USER, values,
                COL_USER + "=? AND " + COL_ID + "=?",
                new String[]{username, cin});
    }

    // ===================== DELETE =====================

    /** @return the number of rows deleted (0 = employee not found). */
    public int deleteEmployee(String cin) {
        return db.delete(TABLE_USER,
                COL_ID + "=? AND " + COL_ROLE + "=?",
                new String[]{cin, User.ROLE_EMPLOYEE});
    }
}
