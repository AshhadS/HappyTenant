package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tenantapp.BuildConfig;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.example.tenantapp.data.SupabaseAuth;

public class WatchmanRegisterActivity extends AppCompatActivity {

    private static final String TAG = "WatchmanRegister";
    private static final int RC_GOOGLE_SIGN_IN = 101;

    // TODO: replace with your real values
    private static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    private static final String GOOGLE_WEB_CLIENT_ID = "856945731946-qpn8vshq652kbtjfp3me4k4o3kncnn9c.apps.googleusercontent.com";

    private SupabaseAuth auth;

    private EditText etFullName, etPhone, etEmail, etPassword;
    private SignInButton btnGoogle;
    private Button btnRegisterEmail, btnTestSupabase;

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_register);

        auth     = new SupabaseAuth(SUPABASE_URL, SUPABASE_ANON_KEY);

        etFullName = findViewById(R.id.etFullName);
        etPhone    = findViewById(R.id.etPhone);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegisterEmail = findViewById(R.id.btnRegisterEmail);
        btnGoogle        = findViewById(R.id.btnGoogle);
        btnTestSupabase  = findViewById(R.id.btnTestSupabase);

        // Google Sign-In config (requestIdToken is required to exchange with Supabase)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnRegisterEmail.setOnClickListener(v -> doEmailRegister());
        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
        btnTestSupabase.setOnClickListener(v ->
                auth.ping(new SupabaseAuth.Callback2() {
                    @Override public void onSuccess(String body) {
                        runOnUiThread(() -> Toast.makeText(WatchmanRegisterActivity.this, "Ping OK: " + body, Toast.LENGTH_SHORT).show());
                        Log.i(TAG, "Ping OK -> " + body);
                    }
                    @Override public void onError(String message, Throwable t) {
                        runOnUiThread(() -> Toast.makeText(WatchmanRegisterActivity.this, message, Toast.LENGTH_LONG).show());
                        Log.e(TAG, message, t);
                    }
                })
        );
    }

    private void doEmailRegister() {
        String name = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email and Password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegisterEmail.setEnabled(false);
        auth.signUpEmail(email, pass, new SupabaseAuth.Callback2() {
            @Override public void onSuccess(String body) {
                Log.i(TAG, "Email sign-up OK -> " + body);
                runOnUiThread(() -> {
                    btnRegisterEmail.setEnabled(true);
                    Toast.makeText(WatchmanRegisterActivity.this, "Registered. Check email if confirmation is required.", Toast.LENGTH_LONG).show();
                    // TODO: (optional) upsert watchman profile to your 'watchmen' table here
                });
            }
            @Override public void onError(String message, Throwable t) {
                Log.e(TAG, message, t);
                runOnUiThread(() -> {
                    btnRegisterEmail.setEnabled(true);
                    Toast.makeText(WatchmanRegisterActivity.this, message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void startGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acct = task.getResult(ApiException.class);
                String idToken = acct.getIdToken();
                if (idToken == null) {
                    Toast.makeText(this, "Google ID token is null", Toast.LENGTH_LONG).show();
                    return;
                }
                exchangeGoogleIdToken(idToken);
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed: " + e.getStatusCode(), e);
                Toast.makeText(this, "Google sign-in failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void exchangeGoogleIdToken(String idToken) {
        auth.signInWithGoogleIdToken(idToken, new SupabaseAuth.Callback2() {
            @Override public void onSuccess(String body) {
                Log.i(TAG, "Supabase Google session -> " + body);
                runOnUiThread(() ->
                        Toast.makeText(WatchmanRegisterActivity.this, "Google sign-in OK", Toast.LENGTH_SHORT).show()
                );
                // TODO: (optional) upsert watchman profile to your 'watchmen' table here
            }
            @Override public void onError(String message, Throwable t) {
                Log.e(TAG, message, t);
                runOnUiThread(() ->
                        Toast.makeText(WatchmanRegisterActivity.this, message, Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}
