package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.example.tenantapp.R;
import com.example.tenantapp.util.Result;
import com.example.tenantapp.net.SupabaseService;
//import com.example.tenantapp.data.remote.SupabaseApi;
import com.example.tenantapp.data.TenantRepository;
import com.example.tenantapp.net.ApiClient;

public class AddTenantActivity extends AppCompatActivity {

    private EditText etFullName, etPhone, etUnit, etFloor, etUserIdOptional;
    private Button btnSave, btnCancel;
    private TextView tvHeader;

    private String watchmanId;
    private String buildingName;

    private TenantRepository repo;
    private SupabaseService supabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tenant);

        watchmanId = getIntent().getStringExtra("watchman_id");
        buildingName = getIntent().getStringExtra("building_name");

        tvHeader = findViewById(R.id.tvHeader);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        etUnit = findViewById(R.id.etUnit);
        etFloor = findViewById(R.id.etFloor);
        etUserIdOptional = findViewById(R.id.etUserIdOptional);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        String headerTxt = "Add Tenant — " + (buildingName != null ? buildingName : "Building");
        tvHeader.setText(headerTxt);


        repo = TenantRepository.getInstance(getApplicationContext());

        btnSave.setOnClickListener(v -> save());
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void save() {
        String full = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String floorStr = etFloor.getText().toString().trim();
        String userIdOpt = etUserIdOptional.getText().toString().trim();
        Integer floor = floorStr.isEmpty() ? null : Integer.valueOf(floorStr);

        if (full.isEmpty() || unit.isEmpty()) {
            Toast.makeText(this, "Full name and unit are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (watchmanId == null) {
            Toast.makeText(this, "Missing watchman id", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);

        Executors.newSingleThreadExecutor().execute(() -> {
            Result<Map<String,Object>> res = repo.createTenant(
                    watchmanId, full, phone, unit, floor,
                    userIdOpt.isEmpty() ? null : userIdOpt
            );

            runOnUiThread(() -> {
                btnSave.setEnabled(true);
                if (res.isOk()) {
                    Toast.makeText(this, "Tenant created", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent());
                    finish();
                } else {
                    Toast.makeText(this, "Failed: " + res.getError(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
