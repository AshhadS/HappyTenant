// com/example/tenantapp/WatchmanRegisterAndOnboardActivity.java
package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tenantapp.data.AuthRepository;
import com.example.tenantapp.data.WatchmanRepository;
import com.example.tenantapp.util.Result;

import java.util.Map;

public class WatchmanRegisterActivity extends AppCompatActivity {

    private static final String TAG = "WatchmanRegOnboard";

    private EditText inputEmail, inputPassword;
    private EditText inputBuildingName, inputBuildingId, inputWatchmanName, inputWatchmanPhone;
    private Button btnSubmit;
    private ProgressBar progress;

    private AuthRepository authRepo;
    private WatchmanRepository watchmanRepo;
    private SessionStore sessionStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_register);

        inputEmail         = findViewById(R.id.etEmail);
        inputPassword      = findViewById(R.id.etPassword);
        inputBuildingName  = findViewById(R.id.inputBuildingName);
        inputBuildingId    = findViewById(R.id.inputBuildingId);
        inputWatchmanName  = findViewById(R.id.etFullName);
        inputWatchmanPhone = findViewById(R.id.etPhone);
        btnSubmit          = findViewById(R.id.btnRegisterEmail);
        progress           = findViewById(R.id.progress);

        authRepo      = new AuthRepository();
        watchmanRepo  = WatchmanRepository.getInstance(getApplicationContext());

        this.sessionStore = SessionStore.getInstance(getApplicationContext());
        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String email         = inputEmail.getText().toString().trim();
        String password      = inputPassword.getText().toString().trim();
        String buildingName  = inputBuildingName.getText().toString().trim();
        String buildingId    = inputBuildingId.getText().toString().trim();
        String watchmanName  = inputWatchmanName.getText().toString().trim();
        String watchmanPhone = inputWatchmanPhone.getText().toString().trim();

        if (TextUtils.isEmpty(email))         { inputEmail.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(password))      { inputPassword.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(buildingName))  { inputBuildingName.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(buildingId))    { inputBuildingId.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(watchmanName))  { inputWatchmanName.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(watchmanPhone)) { inputWatchmanPhone.setError(getString(R.string.err_required)); return; }

        btnSubmit.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        new Thread(() -> {
            // Step 1: sign up
            Result<Map<String, Object>> signUp = authRepo.signUpEmail(email, password);
            if (!signUp.isOk()) {
                postError("Signup failed: " + signUp.getError());
                return;
            }

            Map<String, Object> signUpResp = signUp.get();
            Log.d(TAG, "Signup OK: " + signUpResp);

            // Get auth user id from response
            String authUserId  = authRepo.extractAuthUserId(signUp.get());
            String accessToken = authRepo.extractAccessToken(signUp.get());
            if (authUserId == null) {
                postError("Signup OK but no user.id returned");
                return;
            }

            this.sessionStore.updateSession(accessToken, authUserId);

            // Optional: save session/email in Prefs here
            Prefs.setSignedIn(WatchmanRegisterActivity.this, true);

            // Step 2: register watchman with building (REST)
            Result<Map<String, Object>> reg = watchmanRepo.registerWatchmanWithBuilding(
                    accessToken, authUserId, buildingId, buildingName, watchmanName, watchmanPhone
            );

            if (!reg.isOk()) {
                postError("Watchman register failed: " + reg.getError());
                return;
            }

            Map<String, Object> created = reg.get();
            Log.d(TAG, "Watchman create OK: " + created);

            // Save basic profile locally
            Prefs.saveProfile(
                    WatchmanRegisterActivity.this,
                    buildingId, buildingName, watchmanName, watchmanPhone
            );
            Prefs.setWatchmanOnboarded(WatchmanRegisterActivity.this, true);

            runOnUiThread(() -> {
                Toast.makeText(this, "Registration & Onboarding complete!", Toast.LENGTH_SHORT).show();
                goNext();
            });
        }).start();
    }

    private void postError(String msg) {
        Log.e(TAG, msg);
        runOnUiThread(() -> {
            btnSubmit.setEnabled(true);
            progress.setVisibility(View.GONE);
            Toast.makeText(this, msg + "\n" + getString(R.string.msg_supabase_hint), Toast.LENGTH_LONG).show();
        });
    }

    private void goNext() {
        btnSubmit.setEnabled(true);
        progress.setVisibility(View.GONE);
        Intent i = new Intent(this, WatchmanTenantsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
