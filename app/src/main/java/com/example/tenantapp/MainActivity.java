package com.example.tenantapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.hello_text);
        tv.setText("HappyTenant — Home");

        Button btnWatchman = findViewById(R.id.btnWatchmanOnboarding);
        btnWatchman.setOnClickListener(v ->
                startActivity(new Intent(this, WatchmanLoginActivity.class)));

//        Button btnTenant = findViewById(R.id.btnTenantOnboarding);
//        btnWatchman.setOnClickListener(v ->
//                startActivity(new Intent(this, WatchmanTenantsActivity.class)));


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-redirect first run
        if (!Prefs.isWatchmanOnboarded(this)) {
            startActivity(new Intent(this, WatchmanRegisterActivity.class));
        }
    }
}
