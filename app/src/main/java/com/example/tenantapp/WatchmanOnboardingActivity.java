//package com.example.tenantapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.tenantapp.util.Result;
//import com.example.tenantapp.data.WatchmanRepository;
//
//import java.util.Map;
//
//public class WatchmanOnboardingActivity extends AppCompatActivity {
//
//    private static final String TAG = "OnboardingActivity";
//
//    private EditText inputBuildingName, inputBuildingId, inputWatchmanName, inputWatchmanPhone;
//    private Button btnSubmit, btnTest;
//    private ProgressBar progress;
//
//    private WatchmanRepository watchmanRepo;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_watchman_onboarding);
//
//        // ---- UI ----
//        btnTest = findViewById(R.id.btnTestSupabase);
//        inputBuildingName  = findViewById(R.id.inputBuildingName);
//        inputBuildingId    = findViewById(R.id.inputBuildingId);
//        inputWatchmanName  = findViewById(R.id.inputWatchmanName);
//        inputWatchmanPhone = findViewById(R.id.inputWatchmanPhone);
//        btnSubmit          = findViewById(R.id.btnSubmit);
//        progress           = findViewById(R.id.progress);
//
//        // ---- Repo ----
//        watchmanRepo = WatchmanRepository.getInstance();
//
//        // Test “ping” via repository (blocking call wrapped in a background thread)
//        btnTest.setOnClickListener(v -> {
//            Toast.makeText(this, "Pinging Supabase…", Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "Testing Supabase connection via repository…");
//            new Thread(() -> {
//                Result<Map<String, Object>> result = watchmanRepo.ping(); // blocking .execute() inside
//                runOnUiThread(() -> {
//                    if (result.isOk()) {
//                        Log.d(TAG, "Ping OK: " + result.get());
//                        Toast.makeText(WatchmanOnboardingActivity.this, "Ping OK", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Log.e(TAG, "Ping error: " + result.getError());
//                        Toast.makeText(WatchmanOnboardingActivity.this, "Ping failed: " + result.getError(), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }).start();
//        });
//
//        btnSubmit.setOnClickListener(v -> submit());
//    }
//
//    private void submit() {
//        String buildingName  = inputBuildingName.getText().toString().trim();
//        String buildingId    = inputBuildingId.getText().toString().trim();
//        String watchmanName  = inputWatchmanName.getText().toString().trim();
//        String watchmanPhone = inputWatchmanPhone.getText().toString().trim();
//
//        if (TextUtils.isEmpty(buildingName))  { inputBuildingName.setError(getString(R.string.err_required)); return; }
//        if (TextUtils.isEmpty(buildingId))    { inputBuildingId.setError(getString(R.string.err_required)); return; }
//        if (TextUtils.isEmpty(watchmanName))  { inputWatchmanName.setError(getString(R.string.err_required)); return; }
//        if (TextUtils.isEmpty(watchmanPhone)) { inputWatchmanPhone.setError(getString(R.string.err_required)); return; }
//
//        btnSubmit.setEnabled(false);
//        progress.setVisibility(View.VISIBLE);
//
//        Log.d(TAG, "Submitting onboarding: building=" + buildingName + ", id=" + buildingId
//                + ", watchman=" + watchmanName + ", phone=" + watchmanPhone);
//
//        // Run blocking network call on a background thread
//        new Thread(() -> {
//            Result<Map<String, Object>> result =
//                    watchmanRepo.registerWatchmanWithBuilding(buildingId, buildingName, watchmanName, watchmanPhone); // blocking .execute() inside
//
//            runOnUiThread(() -> {
//                if (result.isOk()) {
//                    Map<String, Object> payload = result.get();
//                    Log.d(TAG, "Supabase success: " + (payload != null ? payload.toString() : "null"));
//
//                    Prefs.saveProfile(
//                            WatchmanOnboardingActivity.this,
//                            buildingId, buildingName, watchmanName, watchmanPhone
//                    );
//                    Prefs.setWatchmanOnboarded(WatchmanOnboardingActivity.this, true);
//                    Toast.makeText(WatchmanOnboardingActivity.this, "Saved to Supabase!", Toast.LENGTH_SHORT).show();
//                    goHome();
//                } else {
//                    Log.e(TAG, "Supabase error: " + result.getError());
//                    onError(result.getError());
//                }
//            });
//        }).start();
//    }
//
//    private void onError(String msg){
//        btnSubmit.setEnabled(true);
//        progress.setVisibility(View.GONE);
//        Toast.makeText(this,
//                "Failed: " + msg + "\n" + getString(R.string.msg_supabase_hint),
//                Toast.LENGTH_LONG).show();
//    }
//
//    private void goHome() {
//        btnSubmit.setEnabled(true);
//        progress.setVisibility(View.GONE);
//        Intent i = new Intent(this, MainActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(i);
//        finish();
//    }
//}
