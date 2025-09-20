package com.example.tenantapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;

import com.example.tenantapp.R;
import com.example.tenantapp.TranslationHelper;
import com.example.tenantapp.util.UserLanguageStore;

import java.util.Locale;

public class TranslationButton extends AppCompatImageButton {

    private TranslationHelper helper;

    public TranslationButton(Context context) {
        super(context);
        init(context);
    }

    public TranslationButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TranslationButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setImageResource(R.drawable.ic_translate); // your translate icon

        setOnClickListener(v -> {
            Toast.makeText(context, "Switching language...", Toast.LENGTH_SHORT).show();

            // Toggle EN ↔ AR
            String currentLang = UserLanguageStore.getInstance(context).getLang();
            String newLang = currentLang.equals("en") ? "ar" : "en";

            // Save new language
            UserLanguageStore.getInstance(context).setLang(newLang);

            // Apply new locale immediately
            Locale locale = new Locale(newLang);
            Locale.setDefault(locale);

            Configuration config = context.getResources().getConfiguration();
            config.setLocale(locale);

            // 🔑 Set layout direction (RTL for Arabic, LTR for English)
            if (newLang.equals("ar")) {
                config.setLayoutDirection(locale);
            } else {
                config.setLayoutDirection(Locale.ENGLISH);
            }

            context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            // Restart the current activity to refresh UI
            if (context instanceof Activity) {
                ((Activity) context).recreate();
            }

            Toast.makeText(context,
                    "Language set to " + (newLang.equals("ar") ? "Arabic" : "English"),
                    Toast.LENGTH_SHORT).show();
        });
    }


}
