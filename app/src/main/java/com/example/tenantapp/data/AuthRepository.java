package com.example.tenantapp.data;

import androidx.annotation.Nullable;
import com.example.tenantapp.net.ApiClient;
import com.example.tenantapp.net.SupabaseService;
import com.example.tenantapp.util.Result;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Response;

public class AuthRepository {
    private final SupabaseService api = ApiClient.get().create(SupabaseService.class);

    public Result<Map<String, Object>> signUpEmail(String email, String password) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            Response<Map<String, Object>> res = api.signUpEmail(body).execute();
            if (res.isSuccessful()) return Result.ok(res.body());
            return Result.fail(parseErr(res));
        } catch (IOException e) {
            return Result.fail(e.getMessage());
        }
    }

    public Result<Map<String, Object>> signInEmail(String email, String password) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("email", email);
            body.put("password", password);
            Response<Map<String, Object>> res = api.signInEmail(body).execute();
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

    @Nullable
    public static String bearer(@Nullable String accessToken){
        return accessToken == null ? null : ("Bearer " + accessToken);
    }
}
