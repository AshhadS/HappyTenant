package com.example.tenantapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal helper to extract "id" from Supabase PostgREST responses. */
public class Json {
    private static final Pattern UUID_RX = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([0-9a-fA-F-]{36})\\\"");

    public static String firstUuidFromArray(String json){
        if (json == null) return null;
        Matcher m = UUID_RX.matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
