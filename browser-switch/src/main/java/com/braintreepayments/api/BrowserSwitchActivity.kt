package com.braintreepayments.api

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.braintreepayments.api.browserswitch.R

class BrowserSwitchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser_switch)
    }

    override fun onResume() {
        super.onResume()
        setResult(RESULT_OK, Intent())
        finish()
    }
}