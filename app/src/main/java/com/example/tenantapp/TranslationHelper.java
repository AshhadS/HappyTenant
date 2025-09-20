package com.example.tenantapp;

import android.content.Context;
import android.util.Log;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class TranslationHelper {

    private static final String TAG = "TranslationHelper";
    private Translator translator;

    public TranslationHelper(String sourceLang, String targetLang) {
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLang)
                        .setTargetLanguage(targetLang)
                        .build();
        translator = Translation.getClient(options);
    }

    // Download language models (only once per device)
    public void downloadModel(Context context, Runnable onSuccess) {
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Model downloaded.");
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Model download failed: " + e.getMessage()));
    }

    // Translate text
    public void translateText(String text, TranslationCallback callback) {
        translator.translate(text)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface TranslationCallback {
        void onSuccess(String translatedText);
        void onFailure(String error);
    }
}
