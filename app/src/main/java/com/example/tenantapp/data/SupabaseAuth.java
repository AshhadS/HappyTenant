package com.example.tenantapp.data;

import android.util.Log;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class SupabaseAuth {
    private static final String TAG = "SupabaseAuth";
    private final String supabaseUrl;     // https://<project>.supabase.co
    private final String anonKey;         // anon key
    private final OkHttpClient http;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public SupabaseAuth(String supabaseUrl, String anonKey) {
        this.supabaseUrl = supabaseUrl;
        this.anonKey = anonKey;
        this.http = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1)) // avoid HTTP/2 issues
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .retryOnConnectionFailure(false)
                .callTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public interface Callback2 {
        void onSuccess(String body);
        void onError(String message, Throwable t);
    }

    /** Email sign-up (creates a user; depending on project settings may send confirmation email) */
    public void signUpEmail(String email, String password, Callback2 cb) {
        String url = supabaseUrl + "/auth/v1/signup";
        JSONObject payload = new JSONObject();
        try {
            payload.put("email", email);
            payload.put("password", password);
        } catch (Exception ignored) {}

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey)
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                cb.onError("SignUp failed: " + e.getMessage(), e);
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    cb.onError("SignUp HTTP " + resp.code() + " -> " + body, null);
                    return;
                }
                cb.onSuccess(body);
            }
        });
    }

    /** Exchange Google ID token for Supabase session */
    public void signInWithGoogleIdToken(String googleIdToken, Callback2 cb) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=id_token";
        JSONObject payload = new JSONObject();
        try {
            payload.put("provider", "google");
            payload.put("id_token", googleIdToken);
        } catch (Exception ignored) {}

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey)
                .addHeader("Accept", "application/json")
                .post(RequestBody.create(payload.toString(), JSON))
                .build();

        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                cb.onError("Google exchange failed: " + e.getMessage(), e);
            }
            @Override public void onResponse(Call call, Response resp) throws IOException {
                String body = resp.body() != null ? resp.body().string() : "";
                if (!resp.isSuccessful()) {
                    cb.onError("Google exchange HTTP " + resp.code() + " -> " + body, null);
                    return;
                }
                cb.onSuccess(body); // contains access_token, refresh_token, user, etc.
            }
        });
    }

    /** Simple REST health check to /rest/v1/app_health */
    public void ping(Callback2 cb) {
        HttpUrl url = HttpUrl.parse(supabaseUrl + "/rest/v1/app_health")
                .newBuilder()
                .addQueryParameter("select", "message")
                .addQueryParameter("limit", "1")
                .build();
        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey)
                .addHeader("Accept", "application/json")
                .get()
                .build();
        http.newCall(req).enqueue(new okhttp3.Callback() {
            @Override public void onFailure(Call call, IOException e) {
                cb.onError("Ping failed: " + e.getMessage(), e);
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    cb.onError("Ping HTTP " + response.code() + " -> " + body, null);
                    return;
                }
                cb.onSuccess(body);
            }
        });
    }
}
