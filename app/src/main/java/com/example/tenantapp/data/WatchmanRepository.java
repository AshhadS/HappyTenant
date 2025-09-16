// com/example/tenantapp/data/WatchmanRepository.java
package com.example.tenantapp.data;

import com.example.tenantapp.SessionStore;
import com.example.tenantapp.net.ApiClient;
import com.example.tenantapp.net.SupabaseService;
import com.example.tenantapp.util.Result;
import android.content.Context;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class WatchmanRepository {

    private static WatchmanRepository INSTANCE;
//    private final SupabaseService api = ApiClient.get().create(SupabaseService.class);
//    private SessionStore sessionStore = SessionStore.getInstance(this.getApplicationContext());


    private final Context appCtx;          // ✅ hold app context here
    private final SupabaseService api;
    private final SessionStore sessionStore;

    private WatchmanRepository(Context context) {
        this.appCtx  = context.getApplicationContext();   // ✅ convert once
        this.sessionStore = SessionStore.getInstance(appCtx);
        this.api     = ApiClient.get().create(SupabaseService.class);
    }

    public static WatchmanRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WatchmanRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WatchmanRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    // Insert into rest/v1/watchman (or rest/v1/watchmen)
    // WatchmanRepository.java
    public Result<Map<String, Object>> registerWatchmanWithBuilding(
            String accessToken,           // can be null
            String authUserId,
            String buildingId,
            String buildingName,
            String watchmanName,
            String watchmanPhone
    ) {
        try {
            Map<String, Object> row = new HashMap<>();
            row.put("user_id", authUserId);
            row.put("building_id", buildingId);
            row.put("building_name", buildingName);
            row.put("watchman_name", watchmanName);
            row.put("watchman_phone", watchmanPhone);

            java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
            rows.add(row);

            // IMPORTANT: only build "Bearer ..." if we actually have a token
            String bearer = (accessToken != null && !accessToken.isEmpty()) ? "Bearer " + accessToken : null;



            Response<List<Map<String, Object>>> res =
                    api.createWatchman(bearer, rows).execute();

            if (res.isSuccessful()) {
                List<Map<String, Object>> data = res.body();
                Map<String, Object> first = (data != null && !data.isEmpty()) ? data.get(0) : null;
                return Result.ok(first);
            }
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    public Result<Map<String,Object>> getMyWatchman() {
        try {
            String uid = sessionStore.requireAuthUserId();
            Response<List<Map<String,Object>>> res = api.getMyWatchman(
                    bearer(),
                    sessionStore.getApiKey(),
                    "id,user_id,building_id,building_name,created_at",
                    "eq." + uid
            ).execute();
            if (!res.isSuccessful()) return Result.fail(parseErr(res));
            List<Map<String,Object>> rows = res.body();
            if (rows == null || rows.isEmpty()) return Result.fail("not_found");
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
