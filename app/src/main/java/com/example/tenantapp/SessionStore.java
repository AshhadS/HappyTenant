package com.example.tenantapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import com.example.tenantapp.BuildConfig;


public final class SessionStore {
    private static final String PREFS = "supabase_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_AUTH_USER_ID  = "auth_user_id";

    // TODO: replace with your Supabase anon/public key (NOT service_role)
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    private static volatile SessionStore INSTANCE;
    private final SharedPreferences prefs;

    private SessionStore(Context appCtx) {
        this.prefs = appCtx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static SessionStore getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SessionStore.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SessionStore(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // --- session lifecycle (call after login) ---
    public void updateSession(String accessToken, String authUserId) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken == null ? "" : accessToken)
                .putString(KEY_AUTH_USER_ID, authUserId == null ? "" : authUserId)
                .apply();
    }

    public void clearSession() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_AUTH_USER_ID).apply();
    }

    // --- values used by repos ---
    public String getApiKey() { return SUPABASE_ANON_KEY; }
    public String getAccessToken() { return prefs.getString(KEY_ACCESS_TOKEN, ""); }

    /** Returns cached id if present, else derives from JWT `sub` and caches it. */
    public String getAuthUserId() {
        String id = prefs.getString(KEY_AUTH_USER_ID, "");
        if (id != null && !id.isEmpty()) return id;

        String jwt = getAccessToken();
        if (jwt == null || jwt.isEmpty()) return "";
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return "";
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            String sub = payload.optString("sub", "");
            if (!sub.isEmpty()) prefs.edit().putString(KEY_AUTH_USER_ID, sub).apply();
            return sub;
        } catch (Exception e) {
            return "";
        }
    }

    public String requireAuthUserId() {
        String uid = getAuthUserId();
        if (uid == null || uid.isEmpty()) {
            throw new IllegalStateException("No auth user id. Call updateSession() after login.");
        }
        return uid;
    }
}

