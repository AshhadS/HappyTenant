package com.example.tenantapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import java.util.concurrent.Executors;

import com.example.tenantapp.data.WatchmanRepository;
import com.example.tenantapp.util.Result;
import com.example.tenantapp.net.SupabaseService;
//import com.example.tenantapp.data.remote.SupabaseApi;
import com.example.tenantapp.data.TenantRepository;
import com.example.tenantapp.ui.TenantAdapter;
import com.example.tenantapp.net.ApiClient;

public class WatchmanTenantsActivity extends AppCompatActivity {

    private TextView tvBuildingTitle, tvBuildingAddress, tvEmpty;
    private ProgressBar progress;
    private RecyclerView rvTenants;
    private View fabAdd;

    private TenantRepository repo;
    private SupabaseService supabase;

    private String watchmanId;
    private String buildingName;
    private String buildingAddress;

    private TenantAdapter adapter;
    private SessionStore sessionStore;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchman_tenants);

        tvBuildingTitle = findViewById(R.id.tvBuildingTitle);
        tvBuildingAddress = findViewById(R.id.tvBuildingAddress);
        tvEmpty = findViewById(R.id.tvEmpty);
        progress = findViewById(R.id.progress);
        rvTenants = findViewById(R.id.rvTenants);
        fabAdd = findViewById(R.id.fabAddTenant);

        rvTenants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TenantAdapter(new ArrayList<>());
        rvTenants.setAdapter(adapter);

        repo  = TenantRepository.getInstance(getApplicationContext());
        this.sessionStore = SessionStore.getInstance(getApplicationContext());

        loadHeaderAndTenants();

        fabAdd.setOnClickListener(v -> {
            if (watchmanId == null) {
                Toast.makeText(this, "Watchman not loaded yet", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AddTenantActivity.class);
            intent.putExtra("watchman_id", watchmanId);
            intent.putExtra("building_name", buildingName);
            startActivityForResult(intent, 1001);
        });
    }

    private void loadHeaderAndTenants() {
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            String authUid = sessionStore.getAuthUserId(); // your stored auth user id
            Result<Map<String,Object>> wres = repo.getMyWatchman(authUid);
            if (!wres.isOk()) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed: " + wres.getError(), Toast.LENGTH_LONG).show();
                });
                return;
            }

            Map<String,Object> w = wres.get();
            watchmanId = String.valueOf(w.get("id"));
            buildingName = String.valueOf(w.get("building_name"));
            buildingAddress = "Building Addrress...";

            Result<List<Map<String,Object>>> tRes = repo.listTenants(watchmanId);

            runOnUiThread(() -> {
                progress.setVisibility(View.GONE);

                tvBuildingTitle.setText(buildingName != null ? buildingName : "My Building");
                tvBuildingAddress.setText(buildingAddress != null ? buildingAddress : "");

                if (!tRes.isOk()) {
                    Toast.makeText(this, "Load tenants failed: " + tRes.getError(), Toast.LENGTH_LONG).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                    return;
                }

                List<Map<String,Object>> tenants = tRes.get();
                adapter.replace(tenants);
                tvEmpty.setVisibility(tenants == null || tenants.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Refresh list after add
            if (watchmanId != null) {
                progress.setVisibility(View.VISIBLE);
                Executors.newSingleThreadExecutor().execute(() -> {
                    Result<List<Map<String,Object>>> tRes = repo.listTenants(watchmanId);
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        if (tRes.isOk()) {
                            adapter.replace(tRes.get());
                            tvEmpty.setVisibility(tRes.get()==null || tRes.get().isEmpty()? View.VISIBLE: View.GONE);
                        } else {
                            Toast.makeText(this, "Refresh failed: " + tRes.getError(), Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        }
    }
}
