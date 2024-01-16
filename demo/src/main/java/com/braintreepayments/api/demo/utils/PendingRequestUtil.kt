package com.braintreepayments.api.demo.utils

import android.content.Context
import android.content.SharedPreferences
import com.braintreepayments.api.BrowserSwitchPendingRequest

class PendingRequestUtil {

    companion object {

        private const val SHARED_PREFS_KEY = "PENDING_REQUESTS"
        private const val PENDING_REQUEST_KEY = "BROWSER_SWITCH_REQUEST"

        fun putPendingRequest(context: Context, pendingRequest: BrowserSwitchPendingRequest.Started) {
            put(context, PENDING_REQUEST_KEY, pendingRequest.toJsonString())
        }

        fun getPendingRequest(context: Context) : BrowserSwitchPendingRequest.Started? {
            get(context, PENDING_REQUEST_KEY)?.let {
                return BrowserSwitchPendingRequest.Started(it)
            }
            return null
        }

        fun clearPendingRequest(context: Context) {
            clear(context, PENDING_REQUEST_KEY)
        }

        private fun put(context: Context, key: String, value: String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().putString(key, value).apply()
        }

        fun get(context: Context, key: String) : String? {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            return sharedPreferences.getString(key, null)
        }

        fun clear(context: Context, key: String) {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                SHARED_PREFS_KEY,
                Context.MODE_PRIVATE
            )
            sharedPreferences.edit().remove(key).apply()
        }
    }

}