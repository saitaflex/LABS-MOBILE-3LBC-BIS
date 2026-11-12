package com.example.esb_2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * LAB 3 and 4: login screen built from the Figma prototype.
 * Checks the two mandatory fields, then the credentials, then opens the Dashboard.
 */
public class MainActivity extends AppCompatActivity {

    // Static (fake) data standing in for a database, as required by LAB 4.
    private static final String VALID_USERNAME = "Admin";
    private static final String VALID_PASSWORD = "Admin";

    private EditText editTextUsername;
    private EditText editTextPassword;
    private TextView txtFormal;
    private TextView txtUsername;
    private TextView txtPwd;

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

        editTextUsername = findViewById(R.id.editTextusername);
        editTextPassword = findViewById(R.id.editTextPassword);
        txtFormal = findViewById(R.id.txtformal);
        txtUsername = findViewById(R.id.txtusername);
        txtPwd = findViewById(R.id.txtpwd);

        Button btnConnect = findViewById(R.id.btnconnect1);
        btnConnect.setOnClickListener(v -> onConnectClicked());
    }

    private void onConnectClicked() {
        String userstr = editTextUsername.getText().toString().trim();
        String pwdstr = editTextPassword.getText().toString();

        // Reset the messages left by a previous attempt.
        txtFormal.setText("");
        txtUsername.setText("");
        txtPwd.setText("");

        boolean formValid = true;

        if (userstr.isEmpty()) {
            txtUsername.setText(R.string.error_username_required);
            formValid = false;
        }
        if (pwdstr.isEmpty()) {
            txtPwd.setText(R.string.error_password_required);
            formValid = false;
        }
        if (!formValid) {
            return;
        }

        if (VALID_USERNAME.equals(userstr) && VALID_PASSWORD.equals(pwdstr)) {
            Intent dashboard = new Intent(MainActivity.this, Dashboard.class);
            dashboard.putExtra(Dashboard.EXTRA_USERNAME, userstr);
            startActivity(dashboard);
            // The user is logged in: Back must not return to the login form.
            finish();
        } else {
            txtFormal.setText(R.string.error_invalid_credentials);
        }
    }
}
