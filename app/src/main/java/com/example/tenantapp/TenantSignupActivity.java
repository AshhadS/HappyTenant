package com.example.tenantapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tenantapp.data.AuthRepository;
import com.example.tenantapp.data.TenantRepository;
import com.example.tenantapp.util.Result;

import java.util.Map;

public class TenantSignupActivity extends AppCompatActivity {

    private static final String TAG = "TenantSignup";
    private EditText inputEmail, inputPassword;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tenant_signup);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnSignup = findViewById(R.id.btnSignupTenant);

        Context appCtx = getApplicationContext();
        AuthRepository authRepo = new AuthRepository();;

        btnSignup.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(appCtx, "Email and Password required", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                // Step 1: Check if tenant exists
//                Result<Map<String, Object>> tenantRes =
//                        TenantRepository.getInstance(appCtx).getTenantByEmail(email);
//
//                if (!tenantRes.isOk()) {
//                    runOnUiThread(() ->
//                            Toast.makeText(appCtx, "This email is not registered by a watchman", Toast.LENGTH_LONG).show());
//                    return; // stop here ❌ no signup
//                }

                // Step 2: Continue with Supabase Auth signup
                Result<Map<String, Object>> authRes =
                        authRepo.signUpEmail(email, password);

                if (!authRes.isOk()) {
                    runOnUiThread(() ->
                            Toast.makeText(appCtx, "Signup failed: " + authRes.getError(), Toast.LENGTH_LONG).show());
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> userObj = (Map<String, Object>) authRes.get().get("user");
                String userId = (String) userObj.get("id");

                Log.d(TAG, "User Id: " + userId);
                Log.d(TAG, "AuthRes: " + authRes.get());

                // Step 3: Link tenant row
                Result<Map<String, Object>> linkRes =
                        TenantRepository.getInstance(appCtx).linkTenantToSelfByEmail(email, userId);

                runOnUiThread(() -> {
                    if (linkRes.isOk()) {
                        Toast.makeText(appCtx, "Tenant signup success", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Tenant linked: " + linkRes.get());
                        // TODO: Navigate to tenant dashboard

                        Intent i = new Intent(TenantSignupActivity.this, TenantDashboardActivity.class);
                        i.putExtra("email", email);
                        startActivity(i);
                        finish(); // close signup screen
                    } else {
                        Toast.makeText(appCtx, "Failed to link tenant: " + linkRes.getError(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Link tenant failed: " + linkRes.getError());
                    }
                });
            }).start();
        });
    }
}
