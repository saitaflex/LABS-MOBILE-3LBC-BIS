package com.example.esb_2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * LAB 4: second activity, reached after a successful login.
 * Reads the username passed by MainActivity through the Intent.
 */
public class Dashboard extends AppCompatActivity {

    /** Key of the Intent extra carrying the logged-in username. */
    public static final String EXTRA_USERNAME = "com.example.esb_2025.extra.USERNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String username = getIntent().getStringExtra(EXTRA_USERNAME);

        TextView textView = findViewById(R.id.textView);
        textView.setText(getString(R.string.dashboard_welcome, username == null ? "" : username));

        Button btnLogout = findViewById(R.id.btnt);
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, MainActivity.class));
            finish();
        });
    }
}
