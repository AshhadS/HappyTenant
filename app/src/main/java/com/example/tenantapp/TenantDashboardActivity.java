package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TenantDashboardActivity extends AppCompatActivity {

    private TextView txtWelcomeTenant;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_dashboard);

        txtWelcomeTenant = findViewById(R.id.txtWelcomeTenant);
        btnLogout = findViewById(R.id.btnLogoutTenant);

        // Show tenant email passed from signup
        String email = getIntent().getStringExtra("email");
        if (email != null) {
            txtWelcomeTenant.setText("Welcome, " + email + "!");
        }

        btnLogout.setOnClickListener(v -> {
            // Just go back to MainActivity
            Intent i = new Intent(TenantDashboardActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}
