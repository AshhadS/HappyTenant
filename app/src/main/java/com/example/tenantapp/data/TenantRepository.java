// com/example/tenantapp/data/WatchmanRepository.java
package com.example.tenantapp.data;

import android.content.Context;

import com.example.tenantapp.net.ApiClient;
import com.example.tenantapp.net.SupabaseService;
import com.example.tenantapp.util.Result;
import com.example.tenantapp.SessionStore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class TenantRepository {

    private static TenantRepository INSTANCE;
    private final SupabaseService api;
    private final String bearer;
    private final String apiKey;
    private final Context appCtx;
    private SessionStore sessionStore;



    public static TenantRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TenantRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TenantRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void clearInstance() {
        synchronized (TenantRepository.class) {
            INSTANCE = null;
        }
    }

    private TenantRepository(Context context) {
        this.appCtx = context.getApplicationContext();

        this.sessionStore = SessionStore.getInstance(appCtx);
        this.apiKey = sessionStore.getApiKey();
        this.bearer = "Bearer " + sessionStore.getAccessToken();

        this.api = ApiClient.get().create(SupabaseService.class);
    }

    /** Fetch the watchman row for the logged-in auth user (includes building info kept in the row). */
    public Result<Map<String,Object>> getMyWatchman(String authUserId) {
        try {
            Response<List<Map<String,Object>>> res = api.getMyWatchman(
                    this.bearer(), this.apiKey,
                    "id, user_id, building_id, building_name, created_at",
                    "eq." + authUserId
            ).execute();

            if (!res.isSuccessful()) return Result.fail(parseErr(res));
            List<Map<String,Object>> rows = res.body();
            if (rows == null || rows.isEmpty()) return Result.fail("Watchman not found for user");
            return Result.ok(rows.get(0));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** List tenants for a given watchman. */
    public Result<List<Map<String,Object>>> listTenants(String watchmanId) {
        try {
            Response<List<Map<String,Object>>> res = api.listTenantsByWatchman(
                    this.bearer(), this.apiKey,
                    "id, user_id, full_name, phone, unit_number, floor, created_at",
                    "eq." + watchmanId
            ).execute();

            if (res.isSuccessful()) return Result.ok(res.body());
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * Create a tenant under this watchman.
     *
     * NOTE about schema:
     * - If your `tenant.user_id` is NOT NULL, you MUST pass a real user id (e.g., from an invite flow).
     * - If you allow creating a row before account signup, make `user_id` nullable and omit it below.
     */
    public Result<Map<String,Object>> createTenant(
            String watchmanId,
            String fullName,
            String phone,
            String email,
            String unitNumber,
            Integer floor,
            String optionalUserId // pass null if your schema allows it
    ) {
        try {
            Map<String,Object> body = new HashMap<>();
            body.put("watchman_id", watchmanId);
            body.put("full_name", fullName);
            body.put("phone", phone);
            body.put("email", email);
            body.put("unit_number", unitNumber);
            body.put("floor", floor);

            if (optionalUserId != null && !optionalUserId.isEmpty()) {
                body.put("user_id", optionalUserId);
            }

            Response<List<Map<String,Object>>> res = api.createTenant(
                    this.bearer(), this.apiKey, body
            ).execute();

            if (!res.isSuccessful()) return Result.fail(parseErr(res));
            List<Map<String,Object>> rows = res.body();
            if (rows == null || rows.isEmpty()) return Result.fail("Empty response from createTenant");
            return Result.ok(rows.get(0));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    private String parseErr(Response<?> res) {
        try { if (res.errorBody() != null) return res.errorBody().string(); }
        catch (IOException ignored) {}
        return "HTTP " + res.code();
    }

    private String bearer() {
        String t = this.sessionStore.getAccessToken();
        return (t == null || t.isEmpty()) ? "" : "Bearer " + t;
    }
}
