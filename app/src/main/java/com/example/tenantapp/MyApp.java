package com.example.tenantapp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.example.tenantapp.util.UserLanguageStore;

import java.util.Locale;

public class MyApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateLanguage(base));
    }

    private Context updateLanguage(Context context) {
        UserLanguageStore store = new UserLanguageStore(context);
        String lang = store.getLang(); // default "en" if none stored

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }
}
