package com.example.tpdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Sign-in screen. Only users with the Admin role may enter the app. */
public class MainActivity extends AppCompatActivity {

    // Test administrator created on first launch (lab requirement).
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "1234";
    private static final String DEFAULT_ADMIN_CIN = "AB123456";

    private UserDB userdb1;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userdb1 = new UserDB(this);
        userdb1.open();
        createDefaultAdmin();

        // Stay signed in: skip the form if an administrator session is still valid.
        session = new SessionManager(this);
        String savedUser = session.getUsername();
        if (savedUser != null) {
            if (User.ROLE_ADMIN.equalsIgnoreCase(userdb1.getUserRole(savedUser))) {
                openHome(savedUser);
            } else {
                session.signOut(); // account deleted or no longer an administrator
            }
        }
    }

    /** Runs on every launch but only inserts the first time. */
    private void createDefaultAdmin() {
        if (userdb1.usernameExists(DEFAULT_ADMIN_USERNAME, null)) {
            return;
        }
        userdb1.insertUser(new User(DEFAULT_ADMIN_USERNAME, "Test", "User",
                DEFAULT_ADMIN_CIN, DEFAULT_ADMIN_PASSWORD, User.ROLE_ADMIN));
    }

    /** Called by the Sign in button (android:onClick="login"). */
    public void login(View view) {
        EditText userEdit = findViewById(R.id.username);
        EditText pwdEdit = findViewById(R.id.mot_de_pass);
        TextView txtformvaljava = findViewById(R.id.txtformval);

        String userstr = userEdit.getText().toString().trim();
        String pwdstr = pwdEdit.getText().toString().trim();

        if (userstr.isEmpty() || pwdstr.isEmpty()) {
            txtformvaljava.setText(R.string.erreur_champs_vides);
            return;
        }

        if (!userdb1.checkCredentials(userstr, pwdstr)) {
            txtformvaljava.setText(R.string.erreur_identifiants);
            return;
        }

        // Constant first: no NullPointerException if the role is null.
        if (!User.ROLE_ADMIN.equalsIgnoreCase(userdb1.getUserRole(userstr))) {
            txtformvaljava.setText(R.string.erreur_acces_admin);
            return;
        }

        session.signIn(userstr);
        openHome(userstr);
    }

    private void openHome(String username) {
        Intent homeactivity = new Intent(MainActivity.this, HomeActivity.class);
        homeactivity.putExtra(HomeActivity.EXTRA_USER, username);
        startActivity(homeactivity);
        finish();
    }

    /** Called by the "Create an account" link (android:onClick="gotoSignup"). */
    public void gotoSignup(View view) {
        Intent signup = new Intent(MainActivity.this, SignupActivity.class);
        startActivity(signup);
        finish();
    }

    /** Called by the "Forgot password" link. Sign-in stays below, so Back returns here. */
    public void gotoResetPassword(View view) {
        startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userdb1 != null) userdb1.close();
    }
}
