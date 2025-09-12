package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;   // ⬅️ Add this
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tenantapp.net.SupabaseClient;

public class WatchmanOnboardingActivity extends AppCompatActivity {

    private static final String TAG = "OnboardingActivity";

    private EditText inputBuildingName, inputBuildingId, inputWatchmanName, inputWatchmanPhone;
    private Button btnSubmit;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_onboarding);


        SupabaseClient supa = new SupabaseClient();
        Button btnTest = findViewById(R.id.btnTestSupabase);
        btnTest.setOnClickListener(v -> {
            Log.d(TAG, "Testing Supabase connection...");
            Toast.makeText(this, "Pinging Supabase…", Toast.LENGTH_SHORT).show();
            supa.ping();
        });

        inputBuildingName = findViewById(R.id.inputBuildingName);
        inputBuildingId = findViewById(R.id.inputBuildingId);
        inputWatchmanName = findViewById(R.id.inputWatchmanName);
        inputWatchmanPhone = findViewById(R.id.inputWatchmanPhone);
        btnSubmit = findViewById(R.id.btnSubmit);
        progress = findViewById(R.id.progress);

        btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String buildingName = inputBuildingName.getText().toString().trim();
        String buildingId = inputBuildingId.getText().toString().trim();
        String watchmanName = inputWatchmanName.getText().toString().trim();
        String watchmanPhone = inputWatchmanPhone.getText().toString().trim();

        if (TextUtils.isEmpty(buildingName)) { inputBuildingName.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(buildingId))   { inputBuildingId.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(watchmanName)) { inputWatchmanName.setError(getString(R.string.err_required)); return; }
        if (TextUtils.isEmpty(watchmanPhone)){ inputWatchmanPhone.setError(getString(R.string.err_required)); return; }

        btnSubmit.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        Log.d(TAG, "Submitting onboarding: building=" + buildingName + ", id=" + buildingId
                + ", watchman=" + watchmanName + ", phone=" + watchmanPhone);

        SupabaseClient supa = new SupabaseClient();
        supa.registerBuildingWithWatchman(buildingName, buildingId, watchmanName, watchmanPhone,
                new SupabaseClient.SimpleCallback() {
                    @Override public void onSuccess(String json) {
                        Log.d(TAG, "Supabase success: " + json);
                        runOnUiThread(() -> {
                            Prefs.saveProfile(WatchmanOnboardingActivity.this, buildingId, buildingName, watchmanName, watchmanPhone);
                            Prefs.setWatchmanOnboarded(WatchmanOnboardingActivity.this, true);
                            Toast.makeText(WatchmanOnboardingActivity.this, "Saved to Supabase!", Toast.LENGTH_SHORT).show();
                            goHome();
                        });
                    }

                    @Override public void onError(String message) {
                        Log.e(TAG, "Supabase error: " + message);
                        runOnUiThread(() -> onError(message));
                    }
                });
    }

    private void onError(String msg){
        btnSubmit.setEnabled(true);
        progress.setVisibility(View.GONE);
        Toast.makeText(this,
                "Failed: " + msg + "\n" + getString(R.string.msg_supabase_hint),
                Toast.LENGTH_LONG).show();
    }

    private void goHome() {
        btnSubmit.setEnabled(true);
        progress.setVisibility(View.GONE);
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
}
