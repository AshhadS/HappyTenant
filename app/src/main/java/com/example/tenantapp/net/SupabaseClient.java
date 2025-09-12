package com.example.tenantapp.net;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tenantapp.BuildConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.Collections;
import java.util.Arrays;
import okhttp3.Protocol;
import okhttp3.ConnectionSpec;


public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final String anonKey;
    private final OkHttpClient http;

    public SupabaseClient() {
        this(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY);
    }

    public SupabaseClient(String baseUrl, String anonKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.anonKey = anonKey;

        HttpLoggingInterceptor log = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        log.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.http = new OkHttpClient.Builder()
                .addInterceptor(log)
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                .retryOnConnectionFailure(false)
                .callTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void ping() {
        HttpUrl url = HttpUrl.parse(BuildConfig.SUPABASE_URL + "/rest/v1/app_health")
                .newBuilder()
                .addQueryParameter("select", "message")
                .addQueryParameter("limit", "1")
                .build();

        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                .get()
                .build();

        Log.d(TAG, "Calling: " + url);

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Ping failed: " + e.getMessage(), e);
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Ping HTTP " + response.code() + " -> " + body);
                    return;
                }
                Log.i(TAG, "Ping OK -> " + body);
            }
        });
    }

    private Request.Builder baseRequest(HttpUrl url) {
        return new Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer " + anonKey);
    }

    /** Insert JSON array into a table and return representation. */
    public void insertJson(String table, String jsonArrayBody, okhttp3.Callback cb) {
        Log.d(TAG, "Insert into " + table + " body=" + jsonArrayBody);
        HttpUrl url = HttpUrl.parse(baseUrl + "/rest/v1/" + table)
                .newBuilder()
                .addQueryParameter("select", "*")
                .build();
        Request req = baseRequest(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(RequestBody.create(jsonArrayBody.getBytes(StandardCharsets.UTF_8), JSON))
                .build();
        http.newCall(req).enqueue(cb);
    }

    /** Upsert JSON array into a table (merge duplicates) and return representation. */
    public void upsertJson(String table, String jsonArrayBody, @Nullable String onConflict, okhttp3.Callback cb) {
        Log.d(TAG, "Upsert into " + table + " body=" + jsonArrayBody + " conflict=" + onConflict);
        HttpUrl.Builder b = HttpUrl.parse(baseUrl + "/rest/v1/" + table).newBuilder()
                .addQueryParameter("select", "*");
        if (onConflict != null && !onConflict.isEmpty()) {
            b.addQueryParameter("on_conflict", onConflict);
        }
        Request req = baseRequest(b.build())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(RequestBody.create(jsonArrayBody.getBytes(StandardCharsets.UTF_8), JSON))
                .build();
        http.newCall(req).enqueue(cb);
    }

    /** Flow used by onboarding: create watchman user → create building pointing to that user. */
    public void registerBuildingWithWatchman(String buildingName, String buildingId,
                                             String watchmanName, String watchmanPhone,
                                             SimpleCallback cb) {
        Log.d(TAG, "Register building flow started");
        String userJson = "{"
                + "\"email\": null,"
                + "\"password_hash\": null,"
                + "\"full_name\": \""+escape(watchmanName)+"\","
                + "\"role\": \"watchman\""
                + "}";
        Log.d(TAG, "User JSON: " + userJson);

        upsertJson("users", "["+userJson+"]", null, new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Log.e(TAG, "User upsert failed: " + e.getMessage(), e);
                cb.onError(e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) { cb.onError("users: HTTP " + response.code()); return; }
                String body = response.body() != null ? response.body().string() : "[]";
                Log.d(TAG, "User upsert response: code=" + response.code() + " body=" + body);
                String userId = com.example.tenantapp.Json.firstUuidFromArray(body);
                if (userId == null) { cb.onError("users: could not parse id"); return; }

                String bJson = "{"
                        + "\"name\":\""+escape(buildingName)+"\","
                        + "\"address\": null,"
                        + "\"watchman_id\":\""+userId+"\""
                        + "}";
                insertJson("buildings", "["+bJson+"]", new Callback() {
                    @Override public void onFailure(Call call, IOException e) { cb.onError(e.getMessage()); }
                    @Override public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) { cb.onError("buildings: HTTP " + response.code()); return; }
                        String b = response.body() != null ? response.body().string() : "[]";
                        cb.onSuccess(b);
                    }
                });
            }
        });
    }

    private static String escape(String s){
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public interface SimpleCallback {
        void onSuccess(String json);
        void onError(String message);
    }
}
