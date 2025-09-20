package com.example.tenantapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLanguageStore {
    private static final String PREFS_NAME = "tenantapp_settings";
    private static final String KEY_LANG = "lang";
    private static UserLanguageStore INSTANCE;
    private final SharedPreferences prefs;

    public UserLanguageStore(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static UserLanguageStore getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserLanguageStore(context.getApplicationContext());
        }
        return INSTANCE;
    }

    public String getLang() {
        return prefs.getString(KEY_LANG, "en");
    }

    public void setLang(String lang) {
        prefs.edit().putString(KEY_LANG, lang).apply();
    }
}
