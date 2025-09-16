package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tenantapp.util.Result;
import com.example.tenantapp.SessionStore;
import com.example.tenantapp.data.AuthRepository;
import com.example.tenantapp.data.WatchmanRepository;

import java.util.Map;
import java.util.concurrent.Executors;

public class WatchmanLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progress;

    private AuthRepository authRepo;
    private WatchmanRepository watchmanRepo;
    private SessionStore session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        session = SessionStore.getInstance(getApplicationContext());
        authRepo = new AuthRepository();;
        watchmanRepo = WatchmanRepository.getInstance(getApplicationContext());

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        progress.setVisibility(ProgressBar.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            Result<Map<String,Object>> r = authRepo.signInEmail(email, pass);

            runOnUiThread(() -> {
                btnLogin.setEnabled(true);
                progress.setVisibility(ProgressBar.INVISIBLE);
            });

            if (!r.isOk() || r.get() == null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Login failed: " + (r.isOk() ? "empty body" : r.getError()),
                                Toast.LENGTH_LONG).show());
                return;
            }

            Map<String,Object> body = r.get();
            String accessToken = String.valueOf(body.get("access_token"));
            String userId = "";
            Object u = body.get("user");
            if (u instanceof Map) {
                Object id = ((Map<?,?>)u).get("id");
                if (id != null) userId = String.valueOf(id);
            }
            if (userId.isEmpty()) userId = session.jwtSub(accessToken);

            // Save session (now AuthHeaderInterceptor will send the right Bearer)
            session.updateSession(accessToken, userId);

            // Fetch watchman row to decide next screen
            Result<Map<String,Object>> w = watchmanRepo.getMyWatchman();
            if (w.isOk()) {
                // Found -> go to tenants list
                runOnUiThread(() -> {
                    startActivity(new Intent(this, WatchmanTenantsActivity.class));
                    finish();
                });
            } else {
                // Not found -> go to watchman onboarding
                runOnUiThread(() -> {
                    startActivity(new Intent(this, WatchmanRegisterActivity.class));
                    finish();
                });
            }
        });
    }
}
