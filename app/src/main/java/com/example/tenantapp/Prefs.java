package com.example.tenantapp;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {
    private static final String FILE = "tenant_prefs";

    private static final String KEY_SIGNED_IN = "signed_in";
    private static final String KEY_WATCHMAN_ONBOARDED = "watchman_onboarded";

    private static final String KEY_BUILDING_ID = "building_id";
    private static final String KEY_BUILDING_NAME = "building_name";
    private static final String KEY_WATCHMAN_NAME = "watchman_name";
    private static final String KEY_WATCHMAN_PHONE = "watchman_phone";

    private Prefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    // --- Auth / session ---
    public static void setSignedIn(Context c, boolean value) {
        sp(c).edit().putBoolean(KEY_SIGNED_IN, value).apply();
    }

    public static boolean isSignedIn(Context c) {
        return sp(c).getBoolean(KEY_SIGNED_IN, false);
    }

    // --- Onboarding state ---
    public static void setWatchmanOnboarded(Context c, boolean value) {
        sp(c).edit().putBoolean(KEY_WATCHMAN_ONBOARDED, value).apply();
    }

    public static boolean isWatchmanOnboarded(Context c) {
        return sp(c).getBoolean(KEY_WATCHMAN_ONBOARDED, false);
    }

    // --- Profile cache (used by your onboarding screens) ---
    public static void saveProfile(Context c,
                                   String buildingId,
                                   String buildingName,
                                   String watchmanName,
                                   String watchmanPhone) {
        sp(c).edit()
                .putString(KEY_BUILDING_ID, buildingId)
                .putString(KEY_BUILDING_NAME, buildingName)
                .putString(KEY_WATCHMAN_NAME, watchmanName)
                .putString(KEY_WATCHMAN_PHONE, watchmanPhone)
                .apply();
    }

    public static String getBuildingId(Context c)    { return sp(c).getString(KEY_BUILDING_ID, null); }
    public static String getBuildingName(Context c)  { return sp(c).getString(KEY_BUILDING_NAME, null); }
    public static String getWatchmanName(Context c)  { return sp(c).getString(KEY_WATCHMAN_NAME, null); }
    public static String getWatchmanPhone(Context c) { return sp(c).getString(KEY_WATCHMAN_PHONE, null); }

    // --- Utilities ---
    public static void clearAll(Context c) {
        sp(c).edit().clear().apply();
    }
}
