package com.example.tenantapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import com.example.tenantapp.BuildConfig;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class SessionStore {
    private static final String PREFS = "supabase_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_AUTH_USER_ID  = "auth_user_id";

    // TODO: put your Supabase anon/public key here (NOT service_role)
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

    /* ---------------- session lifecycle ---------------- */

    /** Save token + userId (userId may be empty; we'll derive from JWT on demand). */
    public void updateSession(String accessToken, String authUserId) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken == null ? "" : accessToken)
                .putString(KEY_AUTH_USER_ID, authUserId == null ? "" : authUserId)
                .apply();
    }

    /** Convenience: store session directly from Supabase Auth response body. */
    @SuppressWarnings("unchecked")
    public void updateSessionFromAuthResponse(Map<String, Object> authBody) {
        if (authBody == null) return;
        String token = String.valueOf(authBody.get("access_token"));
        String userId = "";
        Object u = authBody.get("user");
        if (u instanceof Map) {
            Object id = ((Map<?, ?>) u).get("id");
            if (id != null) userId = String.valueOf(id);
        }
        if (userId == null || userId.isEmpty()) userId = jwtSub(token);
        updateSession(token, userId);
    }

    public void clearSession() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).remove(KEY_AUTH_USER_ID).apply();
    }

    /* ---------------- values used by repos/interceptors ---------------- */

    public String getApiKey()         { return SUPABASE_ANON_KEY; }
    public String getAccessToken()    { return prefs.getString(KEY_ACCESS_TOKEN, ""); }

    /** Returns cached id, else derives from JWT `sub` and caches it. */
    public String getAuthUserId() {
        String id = prefs.getString(KEY_AUTH_USER_ID, "");
        if (id != null && !id.isEmpty()) return id;

        String jwt = getAccessToken();
        String sub = jwtSub(jwt);
        if (sub != null && !sub.isEmpty()) {
            prefs.edit().putString(KEY_AUTH_USER_ID, sub).apply();
        }
        return sub == null ? "" : sub;
    }

    public String requireAuthUserId() {
        String uid = getAuthUserId();
        if (uid == null || uid.isEmpty()) {
            throw new IllegalStateException("No auth user id. Did you call updateSession() after login?");
        }
        return uid;
    }

    public boolean hasSession() {
        String t = getAccessToken();
        return t != null && !t.isEmpty();
    }

    /* ---------------- JWT helper (moved from JwtUtils) ---------------- */

    /** Public so you can call SessionStore.jwtSub(token) anywhere you used JwtUtils.getSub(). */
    public static String jwtSub(String jwt) {
        try {
            if (jwt == null || jwt.isEmpty()) return "";
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return "";
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP);
            JSONObject payload = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            return payload.optString("sub", "");
        } catch (Exception e) {
            return "";
        }
    }
}
