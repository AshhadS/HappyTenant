package com.example.tenantapp;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String FILE = "tenantprefs";
    private static final String KEY_ONBOARDED = "watchman_onboarded";
    private static final String KEY_BUILDING_ID = "building_id";
    private static final String KEY_BUILDING_NAME = "building_name";
    private static final String KEY_WATCHMAN_NAME = "watchman_name";
    private static final String KEY_WATCHMAN_PHONE = "watchman_phone";

    public static void setWatchmanOnboarded(Context c, boolean v){ edit(c).putBoolean(KEY_ONBOARDED, v).apply(); }
    public static boolean isWatchmanOnboarded(Context c){ return prefs(c).getBoolean(KEY_ONBOARDED, false); }

    public static void saveProfile(Context c, String buildingId, String buildingName,
                                   String watchmanName, String watchmanPhone) {
        edit(c)
                .putString(KEY_BUILDING_ID, buildingId)
                .putString(KEY_BUILDING_NAME, buildingName)
                .putString(KEY_WATCHMAN_NAME, watchmanName)
                .putString(KEY_WATCHMAN_PHONE, watchmanPhone)
                .apply();
    }

    public static String getBuildingId(Context c){ return prefs(c).getString(KEY_BUILDING_ID, ""); }
    public static String getBuildingName(Context c){ return prefs(c).getString(KEY_BUILDING_NAME, ""); }
    public static String getWatchmanName(Context c){ return prefs(c).getString(KEY_WATCHMAN_NAME, ""); }
    public static String getWatchmanPhone(Context c){ return prefs(c).getString(KEY_WATCHMAN_PHONE, ""); }

    private static SharedPreferences prefs(Context c){ return c.getSharedPreferences(FILE, Context.MODE_PRIVATE); }
    private static SharedPreferences.Editor edit(Context c){ return prefs(c).edit(); }
}