package com.example.tpdm;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Remembers which administrator is signed in, using SharedPreferences
 * (a small key/value file private to the app). Only the username is kept, never the password.
 */
final class SessionManager {

    private static final String PREFS_NAME = "session";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences prefs;

    SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    void signIn(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    void signOut() {
        prefs.edit().remove(KEY_USERNAME).apply();
    }

    /** @return the signed-in username, or null if nobody is signed in. */
    @Nullable
    String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
}
