package com.example.tpdm;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/** Employee list with search, edit and delete (CRUD on the SQLite database). */
public class UsersListActivity extends AppCompatActivity {

    private UserDB userdb1;

    private EditText idEdit, fnameEdit, lnameEdit, usernameEdit, passwordEdit, searchEdit;
    private LinearLayout employeesContainer;
    private TextView txtTotal, txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_users_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userdb1 = new UserDB(this);
        userdb1.open();

        idEdit = findViewById(R.id.id);
        fnameEdit = findViewById(R.id.fname);
        lnameEdit = findViewById(R.id.lname);
        usernameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.mot_de_pass);
        searchEdit = findViewById(R.id.txtSearch);
        employeesContainer = findViewById(R.id.employeesContainer);
        txtTotal = findViewById(R.id.txtTotalEmployees);
        txtEmpty = findViewById(R.id.txtEmpty);

        // Bonus 6: refresh the list each time the search text changes.
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                afficherEmployees();
            }
        });

        afficherEmployees();
    }

    // ===================== 1. DISPLAY ALL =====================
    public void afficherEmployees() {
        String filter = searchEdit.getText().toString().trim();
        List<User> employees = userdb1.getEmployees(filter);

        txtTotal.setText(String.valueOf(userdb1.countEmployees()));
        txtEmpty.setVisibility(employees.isEmpty() ? View.VISIBLE : View.GONE);

        // Without removeAllViews() the cards would pile up on every refresh.
        employeesContainer.removeAllViews();
        for (User u : employees) {
            employeesContainer.addView(creerCarte(u));
        }
    }

    /** Builds one employee card entirely in Java. */
    private View creerCarte(User u) {
        LinearLayout carte = new LinearLayout(this);
        carte.setOrientation(LinearLayout.VERTICAL);
        carte.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        carte.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        carte.setLayoutParams(params);

        TextView nom = new TextView(this);
        nom.setText(getString(R.string.nom_complet, u.getFname(), u.getLname()));
        nom.setTextColor(ContextCompat.getColor(this, R.color.list_title));
        nom.setTextSize(16);
        nom.setTypeface(null, Typeface.BOLD);
        carte.addView(nom);

        TextView infos = new TextView(this);
        infos.setText(getString(R.string.carte_infos, u.getUsername(), u.getCin()));
        infos.setTextColor(ContextCompat.getColor(this, R.color.list_secondary));
        infos.setTextSize(13);
        carte.addView(infos);

        LinearLayout boutons = new LinearLayout(this);
        boutons.setOrientation(LinearLayout.HORIZONTAL);
        boutons.setPadding(0, dp(10), 0, 0);

        AppCompatButton btnModifier = new AppCompatButton(this);
        btnModifier.setText(R.string.modifier);
        btnModifier.setAllCaps(false);
        boutons.addView(btnModifier,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        AppCompatButton btnSupprimer = new AppCompatButton(this);
        btnSupprimer.setText(R.string.supprimer);
        btnSupprimer.setAllCaps(false);
        btnSupprimer.setTextColor(ContextCompat.getColor(this, R.color.delete));
        boutons.addView(btnSupprimer,
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        carte.addView(boutons);

        // Edit only copies the employee into the form; the database changes on "Save changes".
        btnModifier.setOnClickListener(v -> {
            idEdit.setText(u.getCin());
            fnameEdit.setText(u.getFname());
            lnameEdit.setText(u.getLname());
            usernameEdit.setText(u.getUsername());
            passwordEdit.setText(u.getPwd());
        });

        btnSupprimer.setOnClickListener(v -> confirmerSuppression(u));

        return carte;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    // ===================== 2. UPDATE =====================
    /** Called by the Save changes button (android:onClick="modifierEmployee"). */
    public void modifierEmployee(View view) {
        String cin = idEdit.getText().toString().trim();
        String fname = fnameEdit.getText().toString().trim();
        String lname = lnameEdit.getText().toString().trim();
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (cin.isEmpty()) {
            Toast.makeText(this, R.string.selectionner_un_employe, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fname.isEmpty() || lname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.erreur_champs_vides, Toast.LENGTH_SHORT).show();
            return;
        }
        if (userdb1.usernameExists(username, cin)) {
            Toast.makeText(this, R.string.erreur_utilisateur_existe, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setFname(fname);
        user.setLname(lname);
        user.setUsername(username);
        user.setPwd(password);

        if (userdb1.updateEmployee(cin, user) > 0) {
            Toast.makeText(this, R.string.employe_modifie, Toast.LENGTH_SHORT).show();
            viderChamps();
            afficherEmployees();
        } else {
            Toast.makeText(this, R.string.employe_introuvable, Toast.LENGTH_SHORT).show();
        }
    }

    // ===================== 3. DELETE =====================
    /** Bonus 4: ask for confirmation before deleting. */
    private void confirmerSuppression(User u) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmer_suppression_titre)
                .setMessage(R.string.confirmer_suppression_message)
                .setPositiveButton(R.string.supprimer, (dialog, which) -> supprimerEmployee(u.getCin()))
                .setNegativeButton(R.string.annuler, null)
                .show();
    }

    private void supprimerEmployee(String cin) {
        if (userdb1.deleteEmployee(cin) > 0) {
            Toast.makeText(this, R.string.employe_supprime, Toast.LENGTH_SHORT).show();
            viderChamps();
            afficherEmployees();
        } else {
            Toast.makeText(this, R.string.employe_introuvable, Toast.LENGTH_SHORT).show();
        }
    }

    private void viderChamps() {
        idEdit.setText("");
        fnameEdit.setText("");
        lnameEdit.setText("");
        usernameEdit.setText("");
        passwordEdit.setText("");
    }

    /** Bonus 3: HomeActivity is still below this screen, so finishing is enough. */
    public void retourAccueil(View view) {
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
