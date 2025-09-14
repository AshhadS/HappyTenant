package com.example.tenantapp.net;

import android.util.Log;
import com.example.tenantapp.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public final class ApiClient {
    private static final String TAG = "ApiClient";
    private static volatile Retrofit retrofit;

    private ApiClient() {}

    public static Retrofit get() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    Interceptor supabaseHeaders = chain -> {
                        Request req = chain.request().newBuilder()
                                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                                .addHeader("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY)
                                .build();
                        Response res = chain.proceed(req);
                        return res;
                    };

                    OkHttpClient http = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .addInterceptor(chain -> {
                                okhttp3.Request orig = chain.request();
                                okhttp3.Request.Builder b = orig.newBuilder()
                                        .header("apikey", BuildConfig.SUPABASE_ANON_KEY)   // always include apikey
                                        .header("Accept", "application/json");

                                // Only inject a fallback Authorization if caller DID NOT set one
                                if (orig.header("Authorization") == null) {
                                    b.header("Authorization", "Bearer " + BuildConfig.SUPABASE_ANON_KEY);
                                }

                                // Content-Type is set by Retrofit for @Body; add only if you need a default
                                return chain.proceed(b.build());
                            })
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BuildConfig.SUPABASE_URL + "/")
                            .addConverterFactory(MoshiConverterFactory.create())
                            .client(http)
                            .build();
                }
            }
        }
        return retrofit;
    }
}
