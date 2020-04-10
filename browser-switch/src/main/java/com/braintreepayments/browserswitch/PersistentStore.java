package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.SharedPreferences;

class PersistentStore {

    static void put(String key, String value, Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("BraintreeApi", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(key, value).apply();
    }

    static String get(String key, Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("BraintreeApi", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    static void remove(String key, Context context) {
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("BraintreeApi", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(key).apply();
    }
}
