package com.braintreepayments.api.demo.utils

import android.content.Context
import android.content.SharedPreferences
import com.braintreepayments.api.BrowserSwitchPendingRequest

class PendingRequestStore {

    companion object {

        private const val SHARED_PREFS_KEY = "PENDING_REQUESTS"
        private const val PENDING_REQUEST_KEY = "BROWSER_SWITCH_REQUEST"

        fun put(context: Context, pendingRequest: BrowserSwitchPendingRequest.Started) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putString(PENDING_REQUEST_KEY, pendingRequest.token).apply()
        }

        fun get(context: Context) : BrowserSwitchPendingRequest.Started? {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            val pendingRequestString = sharedPreferences.getString(PENDING_REQUEST_KEY, null)
            pendingRequestString?.let {
                return BrowserSwitchPendingRequest.Started(it)
            }
            return null
        }

        fun clear(context: Context) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().remove(PENDING_REQUEST_KEY).apply()
        }
    }
}