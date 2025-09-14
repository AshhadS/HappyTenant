package com.example.tenantapp.data;

import com.example.tenantapp.net.ApiClient;
import com.example.tenantapp.net.SupabaseService;
import com.example.tenantapp.util.Result;
import java.io.IOException;
import java.util.*;
import retrofit2.Response;

public class WatchmanRepository {
    private final SupabaseService api = ApiClient.get().create(SupabaseService.class);

    // Create or update the watchman row in public.users with role='watchman'
    public Result<List<Map<String, Object>>> upsertWatchman(String accessToken, String userId, String email, String fullName){
        try {
            Map<String, Object> row = new HashMap<>();
            row.put("id", userId);
            row.put("email", email);
            row.put("full_name", fullName);
            row.put("role", "watchman");

            Response<List<Map<String, Object>>> res = api.upsertUser(AuthRepository.bearer(accessToken),
                    "id", Collections.singletonList(row)).execute();
            if (res.isSuccessful()) return Result.ok(res.body());
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    // Create or update building for this watchman (one-to-one)
    public Result<List<Map<String, Object>>> upsertBuilding(String accessToken, String buildingIdOrNull,
                                                            String name, String address, String watchmanId){
        try {
            Map<String, Object> row = new HashMap<>();
            if (buildingIdOrNull != null) row.put("id", buildingIdOrNull);
            row.put("name", name);
            row.put("address", address);
            row.put("watchman_id", watchmanId);

            Response<List<Map<String, Object>>> res = api.upsertBuilding(AuthRepository.bearer(accessToken),
                    "id", Collections.singletonList(row)).execute();
            if (res.isSuccessful()) return Result.ok(res.body());
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    public Result<List<Map<String, Object>>> getBuildingForWatchman(String accessToken, String watchmanId){
        try {
            Response<List<Map<String, Object>>> res =
                    api.getBuildingByWatchman(AuthRepository.bearer(accessToken),
                            "eq." + watchmanId).execute();
            if (res.isSuccessful()) return Result.ok(res.body());
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    private String parseErr(Response<?> res){
        try { return res.errorBody() != null ? res.errorBody().string() : ("HTTP "+res.code()); }
        catch (Exception ignored) {}
        return "HTTP " + res.code();
    }
}
