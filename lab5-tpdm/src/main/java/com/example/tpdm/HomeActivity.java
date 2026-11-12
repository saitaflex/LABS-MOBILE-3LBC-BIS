package com.example.tpdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/** Admin home screen: entry point to user management. */
public class HomeActivity extends AppCompatActivity {

    /** Intent extra carrying the username of the signed-in administrator. */
    public static final String EXTRA_USER = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bonus 2: welcome message built from the extra sent by MainActivity.
        String user = getIntent().getStringExtra(EXTRA_USER);
        TextView txtWelcome = findViewById(R.id.txtWelcome);
        txtWelcome.setText(getString(R.string.bienvenue_utilisateur, user == null ? "" : user));
    }

    /** Sign out: Home is finished so Back cannot reopen it. */
    public void gotosignin(View view) {
        Intent signin = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(signin);
        finish();
    }

    /** Home stays on the back stack, so Back (or "Back to Home") returns here. */
    public void goToUsersList(View view) {
        Intent usersList = new Intent(HomeActivity.this, UsersListActivity.class);
        startActivity(usersList);
    }

    /** Bonus 5: only a signed-in administrator can open registration with the Admin role. */
    public void goToCreateUser(View view) {
        Intent signup = new Intent(HomeActivity.this, SignupActivity.class);
        signup.putExtra(SignupActivity.EXTRA_FROM_ADMIN, true);
        startActivity(signup);
    }
}
