package com.example.tpdm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.database.Cursor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented tests: they run on an emulator or device against a real SQLite engine.
 * Each test gets a fresh in-memory database, so the app's own data is never touched.
 */
@RunWith(AndroidJUnit4.class)
public class UserDBTest {

    private UserDB db;

    @Before
    public void setUp() {
        db = new UserDB(ApplicationProvider.getApplicationContext(), null);
        db.open();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private static User employee(String fname, String username, String cin) {
        return new User(username, fname, "Test", cin, "pass1234", User.ROLE_EMPLOYEE);
    }

    @Test
    public void insert_thenCredentialsAreAccepted() {
        assertTrue(db.insertUser(employee("Sara", "sara", "11111111")) > 0);

        assertTrue(db.checkCredentials("sara", "pass1234"));
        assertFalse(db.checkCredentials("sara", "wrong"));
        assertFalse(db.checkCredentials("nobody", "pass1234"));
        assertEquals(User.ROLE_EMPLOYEE, db.getUserRole("sara"));
        assertNull(db.getUserRole("nobody"));
    }

    @Test
    public void insert_storesAHashNotThePassword() {
        db.insertUser(employee("Sara", "sara", "11111111"));

        try (Cursor c = db.getDB().rawQuery(
                "SELECT " + SetupSqliteDB.COL_PASSWORD + " FROM " + SetupSqliteDB.TABLE_USER, null)) {
            assertTrue(c.moveToFirst());
            assertNotEquals("pass1234", c.getString(0));
            assertTrue(PasswordHasher.isHashed(c.getString(0)));
        }
    }

    @Test
    public void insert_duplicateCinFails() {
        assertTrue(db.insertUser(employee("Sara", "sara", "11111111")) > 0);
        assertEquals(-1, db.insertUser(employee("Karim", "karim", "11111111")));
        assertEquals(1, db.countEmployees());
    }

    @Test
    public void usernameExists_ignoresTheEditedUser() {
        db.insertUser(employee("Sara", "sara", "11111111"));

        assertTrue(db.usernameExists("sara", null));
        assertFalse(db.usernameExists("sara", "11111111"));
        assertTrue(db.usernameExists("sara", "22222222"));
        assertFalse(db.usernameExists("karim", null));
    }

    @Test
    public void getEmployees_sortsByFirstNameFiltersAndSkipsAdmins() {
        db.insertUser(employee("Sara", "sara", "11111111"));
        db.insertUser(employee("Karim", "karim", "22222222"));
        db.insertUser(employee("Lina", "lina", "33333333"));
        db.insertUser(new User("boss", "Amine", "Admin", "44444444", "pass1234", User.ROLE_ADMIN));

        List<User> all = db.getEmployees("");
        assertEquals(3, all.size());
        assertEquals("Karim", all.get(0).getFname());
        assertEquals("Lina", all.get(1).getFname());
        assertEquals("Sara", all.get(2).getFname());

        List<User> filtered = db.getEmployees("ar");
        assertEquals(2, filtered.size()); // Karim, Sara

        assertEquals(3, db.countByRole(User.ROLE_EMPLOYEE));
        assertEquals(1, db.countByRole(User.ROLE_ADMIN));
    }

    @Test
    public void update_keepsThePasswordWhenNoneIsGiven() {
        db.insertUser(employee("Sara", "sara", "11111111"));

        User changes = new User();
        changes.setFname("Sarah");
        changes.setLname("Ben");
        changes.setUsername("sarah");
        changes.setPwd("");
        assertEquals(1, db.updateEmployee("11111111", changes));

        assertEquals("Sarah", db.getEmployees("").get(0).getFname());
        assertTrue(db.checkCredentials("sarah", "pass1234"));

        changes.setPwd("newpass");
        assertEquals(1, db.updateEmployee("11111111", changes));
        assertTrue(db.checkCredentials("sarah", "newpass"));
        assertFalse(db.checkCredentials("sarah", "pass1234"));
    }

    @Test
    public void updateAndDelete_neverTouchAdministrators() {
        db.insertUser(new User("boss", "Amine", "Admin", "44444444", "pass1234", User.ROLE_ADMIN));

        assertEquals(0, db.updateEmployee("44444444", employee("X", "x", "44444444")));
        assertEquals(0, db.deleteEmployee("44444444"));
        assertEquals(1, db.countByRole(User.ROLE_ADMIN));
    }

    @Test
    public void delete_removesTheEmployee() {
        db.insertUser(employee("Sara", "sara", "11111111"));

        assertEquals(1, db.deleteEmployee("11111111"));
        assertEquals(0, db.deleteEmployee("11111111"));
        assertEquals(0, db.countEmployees());
    }

    @Test
    public void resetPassword_needsTheMatchingUsernameAndCin() {
        db.insertUser(employee("Sara", "sara", "11111111"));

        assertEquals(0, db.resetPassword("sara", "99999999", "newpass"));
        assertTrue(db.checkCredentials("sara", "pass1234"));

        assertEquals(1, db.resetPassword("sara", "11111111", "newpass"));
        assertTrue(db.checkCredentials("sara", "newpass"));
    }
}
