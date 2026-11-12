package com.example.tpdm;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * "Forgot password" screen. The user proves who they are with their username and CIN,
 * then chooses a new password.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private UserDB userdb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userdb1 = new UserDB(this);
        userdb1.open();
    }

    /** Called by the Reset button (android:onClick="resetPassword"). */
    public void resetPassword(View view) {
        EditText usernameEdit = findViewById(R.id.username);
        EditText cinEdit = findViewById(R.id.cin);
        EditText passwordEdit = findViewById(R.id.mot_de_pass);
        EditText confirmEdit = findViewById(R.id.confirm_mot_de_pass);
        TextView txtformval = findViewById(R.id.txtformval);

        String username = usernameEdit.getText().toString().trim();
        String cin = cinEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String confirm = confirmEdit.getText().toString().trim();

        if (username.isEmpty() || cin.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            txtformval.setText(R.string.erreur_champs_vides);
            return;
        }
        if (!Validator.isValidPassword(password)) {
            txtformval.setText(getString(R.string.erreur_mot_de_passe_court,
                    Validator.MIN_PASSWORD_LENGTH));
            return;
        }
        if (!password.equals(confirm)) {
            txtformval.setText(R.string.erreur_confirmation);
            return;
        }

        if (userdb1.resetPassword(username, cin, password) > 0) {
            Toast.makeText(this, R.string.mot_de_passe_modifie, Toast.LENGTH_SHORT).show();
            finish(); // back to sign-in
        } else {
            txtformval.setText(R.string.erreur_compte_introuvable);
        }
    }

    /** Called by the "Back to sign in" link. */
    public void retourConnexion(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userdb1 != null) userdb1.close();
    }
}
