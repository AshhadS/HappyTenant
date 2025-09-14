// com/example/tenantapp/data/WatchmanRepository.java
package com.example.tenantapp.data;

import com.example.tenantapp.net.ApiClient;
import com.example.tenantapp.net.SupabaseService;
import com.example.tenantapp.util.Result;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class WatchmanRepository {

    private static WatchmanRepository INSTANCE;
    private final SupabaseService api = ApiClient.get().create(SupabaseService.class);

    public static synchronized WatchmanRepository getInstance() {
        if (INSTANCE == null) INSTANCE = new WatchmanRepository();
        return INSTANCE;
    }

    // Insert into rest/v1/watchman (or rest/v1/watchmen)
    public Result<Map<String, Object>> registerWatchmanWithBuilding(
            String accessToken,
            String authUserId,
            String buildingId,
            String buildingName,
            String watchmanName,
            String watchmanPhone
    ) {
        try {
            Map<String, Object> row = new HashMap<>();
            row.put("auth_user_id", authUserId);
            row.put("building_id", buildingId);
            row.put("building_name", buildingName);
            row.put("watchman_name", watchmanName);
            row.put("watchman_phone", watchmanPhone);

            java.util.List<Map<String, Object>> rows = new java.util.ArrayList<>();
            rows.add(row);

            Response<List<Map<String, Object>>> res =
                    api.createWatchman("Bearer " + accessToken, rows).execute();

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

    private String parseErr(Response<?> res) {
        try { if (res.errorBody() != null) return res.errorBody().string(); }
        catch (IOException ignored) {}
        return "HTTP " + res.code();
    }
}
