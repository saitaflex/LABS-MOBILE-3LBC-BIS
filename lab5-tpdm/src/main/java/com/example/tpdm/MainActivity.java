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
    }

    /** insertUser returns -1 when the CIN already exists, so later launches change nothing. */
    private void createDefaultAdmin() {
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

        User u1 = new User();
        u1.setUsername(userstr);
        u1.setPwd(pwdstr);

        if (userdb1.getUserWithLog(u1) == 0) {
            txtformvaljava.setText(R.string.erreur_identifiants);
            return;
        }

        // Constant first: no NullPointerException if the role is null.
        if (!User.ROLE_ADMIN.equalsIgnoreCase(userdb1.getUserRole(userstr))) {
            txtformvaljava.setText(R.string.erreur_acces_admin);
            return;
        }

        Intent homeactivity = new Intent(MainActivity.this, HomeActivity.class);
        homeactivity.putExtra(HomeActivity.EXTRA_USER, userstr);
        startActivity(homeactivity);
        finish();
    }

    /** Called by the "Create an account" link (android:onClick="gotoSignup"). */
    public void gotoSignup(View view) {
        Intent signup = new Intent(MainActivity.this, SignupActivity.class);
        startActivity(signup);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userdb1 != null) userdb1.close();
    }
}
