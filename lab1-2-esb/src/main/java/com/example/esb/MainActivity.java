package com.example.esb;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * LAB 1 and 2: a single screen where the user enters an identity
 * (first name, last name, date and city of birth) and gets a summary pop-up.
 */
public class MainActivity extends AppCompatActivity {

    private EditText editPrenom;
    private EditText editNom;
    private EditText editDateNaissance;
    private EditText editVilleNaissance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int padding = getResources().getDimensionPixelSize(R.dimen.screen_padding);
            v.setPadding(systemBars.left + padding, systemBars.top + padding,
                    systemBars.right + padding, systemBars.bottom + padding);
            return insets;
        });

        editPrenom = findViewById(R.id.editPrenom);
        editNom = findViewById(R.id.editNom);
        editDateNaissance = findViewById(R.id.editDateNaissance);
        editVilleNaissance = findViewById(R.id.editVilleNaissance);
    }

    /**
     * Called by the Validate button through android:onClick="validateAction".
     * The method must be public, return void and take a single View parameter.
     */
    public void validateAction(View v) {
        String prenom = textOf(editPrenom);
        String nom = textOf(editNom);
        String dateNaissance = textOf(editDateNaissance);
        String villeNaissance = textOf(editVilleNaissance);

        if (prenom.isEmpty() || nom.isEmpty() || dateNaissance.isEmpty() || villeNaissance.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.summary_title)
                .setMessage(getString(R.string.summary_message,
                        prenom, nom, dateNaissance, villeNaissance))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private static String textOf(EditText field) {
        return field.getText().toString().trim();
    }
}
