package com.braintreepayments.api.demo.utils

import android.content.Context
import android.content.SharedPreferences
import com.braintreepayments.api.BrowserSwitchPendingRequest

class PendingRequestStore {

    companion object {

        private const val SHARED_PREFS_KEY = "PENDING_REQUESTS"
        private const val PENDING_REQUEST_KEY = "BROWSER_SWITCH_REQUEST"

        @JvmStatic
        fun put(context: Context, pendingRequestToken: String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putString(PENDING_REQUEST_KEY, pendingRequestToken).apply()
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