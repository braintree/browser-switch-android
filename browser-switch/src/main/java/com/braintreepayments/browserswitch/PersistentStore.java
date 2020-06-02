package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.SharedPreferences;

class PersistentStore {

    private static final String PREFERENCES_KEY =
        "com.braintreepayament.browserswitch.persistentstore";

    static void put(String key, String value, Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
            applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }

    static String get(String key, Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
            applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    static void remove(String key, Context context) {
        Context applicationContext = context.getApplicationContext();
        SharedPreferences sharedPreferences =
            applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(key).apply();
    }
}
