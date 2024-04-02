package com.braintreepayments.api.demo.utils

import android.content.Context
import android.content.SharedPreferences

class PendingRequestStore {

    companion object {

        private const val SHARED_PREFS_KEY = "PENDING_REQUESTS"
        private const val PENDING_REQUEST_KEY = "BROWSER_SWITCH_REQUEST"

        @JvmStatic
        fun put(context: Context, pendingRequestState: String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putString(PENDING_REQUEST_KEY, pendingRequestState).apply()
        }

        @JvmStatic
        fun get(context: Context): String? {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            return sharedPreferences.getString(PENDING_REQUEST_KEY, null)
        }

        @JvmStatic
        fun clear(context: Context) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().remove(PENDING_REQUEST_KEY).apply()
        }
    }
}