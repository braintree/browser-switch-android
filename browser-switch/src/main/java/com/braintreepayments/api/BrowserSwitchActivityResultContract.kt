package com.braintreepayments.api

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class BrowserSwitchActivityResultContract : ActivityResultContract<String, String>() {
    override fun createIntent(context: Context, input: String): Intent {
        return Intent().setClass(context, BrowserSwitchActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String {
        if (resultCode == RESULT_OK) {
            return "Result"
        } else {
            return "No result"
        }
    }
}