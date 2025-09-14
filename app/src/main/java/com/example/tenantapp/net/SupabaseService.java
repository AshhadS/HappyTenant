package com.example.tenantapp.net;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

/** Centralized Supabase endpoints: Auth + PostgREST + RPC */
public interface SupabaseService {

    /* ---------- AUTH (email/password) ---------- */
    @POST("auth/v1/signup")
    Call<Map<String, Object>> signUpEmail(@Body Map<String, Object> body);

    @POST("auth/v1/token?grant_type=password")
    Call<Map<String, Object>> signInEmail(@Body Map<String, Object> body);

    @POST("auth/v1/token?grant_type=id_token")
    Call<Map<String, Object>> signInWithIdToken(@Body Map<String, Object> body);

    /* ------- PING -----------------*/
    @GET("health")   // 👈 adjust endpoint if you use another for ping
    Call<Map<String, Object>> ping();


    /* ---------- PUBLIC TABLES ---------- */
    // users
    @GET("rest/v1/users")
    Call<List<Map<String, Object>>> getUserById(@Header("Authorization") String bearer,
                                                @Query("id") String eqId, @Query("select") String select);

    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    @POST("rest/v1/users")
    Call<List<Map<String, Object>>> insertUser(@Header("Authorization") String bearer,
                                               @Body List<Map<String, Object>> body);

    @Headers({"Prefer: resolution=merge-duplicates,return=representation", "Content-Type: application/json"})
    @POST("rest/v1/users")
    Call<List<Map<String, Object>>> upsertUser(@Header("Authorization") String bearer,
                                               @Query("on_conflict") String onConflict,
                                               @Body List<Map<String, Object>> body);

    // buildings
    @GET("rest/v1/watchman")
    Call<List<Map<String, Object>>> getWatchman(@Header("Authorization") String bearer,
                                                          @Query("user_id") String eqUserId);

    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    @POST("rest/v1/watchman") // or watchmen if that's your table
    Call<List<Map<String, Object>>> createWatchman(
            @Header("Authorization") String bearer,
            @Body List<Map<String, Object>> body
    );

    @Headers({"Prefer: resolution=merge-duplicates,return=representation", "Content-Type: application/json"})
    @POST("rest/v1/watchman")
    Call<List<Map<String, Object>>> upsertWatchman(@Header("Authorization") String bearer,
                                                   @Query("on_conflict") String onConflict,
                                                   @Body List<Map<String, Object>> body);

    @Headers({
            "Content-Type: application/json",
            "Accept: application/json"
    })
    @POST("rpc/register_watchman_with_building")
    Call<Map<String, Object>> registerWatchmanWithBuilding(@Body Map<String, Object> body);

    // buildings
    @GET("rest/v1/buildings")
    Call<List<Map<String, Object>>> getBuildingByWatchman(@Header("Authorization") String bearer,
                                                          @Query("watchman_id") String eqWatchmanId);

    @Headers({"Prefer: return=representation", "Content-Type: application/json"})
    @POST("rest/v1/buildings")
    Call<List<Map<String, Object>>> insertBuilding(@Header("Authorization") String bearer,
                                                   @Body List<Map<String, Object>> body);

    @Headers({"Prefer: resolution=merge-duplicates,return=representation", "Content-Type: application/json"})
    @POST("rest/v1/buildings")
    Call<List<Map<String, Object>>> upsertBuilding(@Header("Authorization") String bearer,
                                                   @Query("on_conflict") String onConflict,
                                                   @Body List<Map<String, Object>> body);

    // apartments (example)
    @GET("rest/v1/apartments")
    Call<List<Map<String, Object>>> getApartments(@Header("Authorization") String bearer,
                                                  @Query("building_id") String eqBuildingId,
                                                  @Query("select") String select);

    /* ---------- RPC examples (if you created SQL functions) ---------- */
    @Headers({"Content-Type: application/json"})
    @POST("rest/v1/rpc/assign_watchman_to_building")
    Call<Map<String, Object>> assignWatchmanToBuilding(@Header("Authorization") String bearer,
                                                       @Body Map<String, Object> body);
}
