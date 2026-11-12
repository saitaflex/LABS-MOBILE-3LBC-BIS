package com.example.tpdm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Registration screen.
 * Opened from sign-in it only creates Employee accounts; opened by a signed-in
 * administrator (EXTRA_FROM_ADMIN) it can also create Admin accounts (bonus 5).
 */
public class SignupActivity extends AppCompatActivity {

    /** Boolean extra: true when the screen is opened from the admin home. */
    public static final String EXTRA_FROM_ADMIN = "com.example.tpdm.extra.FROM_ADMIN";

    private UserDB userdb1;
    private boolean fromAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_signup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userdb1 = new UserDB(this);
        userdb1.open();

        fromAdmin = getIntent().getBooleanExtra(EXTRA_FROM_ADMIN, false);

        // The Spinner values come from res/values/arrays.xml.
        Spinner roleSpinner = findViewById(R.id.role);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                fromAdmin ? R.array.roles : R.array.roles_public,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        if (fromAdmin) {
            // An administrator is already signed in: the "Already registered?" link is useless.
            findViewById(R.id.mot_de_passe).setVisibility(View.GONE);
        }
    }

    /** Called by the Sign up button (android:onClick="signup"). */
    public void signup(View view) {
        EditText fnameEdit = findViewById(R.id.fname);
        EditText lnameEdit = findViewById(R.id.lname);
        EditText cinEdit = findViewById(R.id.cin);
        EditText usernameEdit = findViewById(R.id.username);
        EditText passwordEdit = findViewById(R.id.mot_de_pass);
        Spinner roleSpinner = findViewById(R.id.role);
        TextView txtformat = findViewById(R.id.txtformval);

        String fnamestr = fnameEdit.getText().toString().trim();
        String lnamestr = lnameEdit.getText().toString().trim();
        String cinstr = cinEdit.getText().toString().trim();
        String usernamestr = usernameEdit.getText().toString().trim();
        String pwdstr = passwordEdit.getText().toString().trim();
        Object selectedRole = roleSpinner.getSelectedItem();
        String rolestr = selectedRole == null ? "" : selectedRole.toString();

        if (fnamestr.isEmpty() || lnamestr.isEmpty() || cinstr.isEmpty()
                || usernamestr.isEmpty() || pwdstr.isEmpty()) {
            txtformat.setText(R.string.erreur_champs_vides);
            return;
        }

        if (rolestr.isEmpty()) {
            txtformat.setText(R.string.erreur_role);
            return;
        }

        // Never trust the UI alone: enforce the rule in code as well.
        if (!fromAdmin && User.ROLE_ADMIN.equalsIgnoreCase(rolestr)) {
            txtformat.setText(R.string.erreur_role_admin_interdit);
            return;
        }

        if (userdb1.usernameExists(usernamestr, null)) {
            txtformat.setText(R.string.erreur_utilisateur_existe);
            return;
        }

        User newU = new User(usernamestr, fnamestr, lnamestr, cinstr, pwdstr, rolestr);

        if (userdb1.insertUser(newU) > 0) {
            if (fromAdmin) {
                Toast.makeText(this, R.string.utilisateur_cree, Toast.LENGTH_SHORT).show();
                finish(); // back to the admin home, still on the back stack
            } else {
                gotoSignin(view);
            }
        } else {
            // Typically: this CIN (primary key) is already used.
            txtformat.setText(R.string.erreur_inscription);
        }
    }

    /** Called by the "Already registered?" link (android:onClick="gotoSignin"). */
    public void gotoSignin(View view) {
        Intent signin = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(signin);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userdb1 != null) {
            userdb1.close();
        }
    }
}
