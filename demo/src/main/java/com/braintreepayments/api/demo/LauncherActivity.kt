package com.braintreepayments.api.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.braintreepayments.api.BrowserSwitchLauncher

class LauncherActivity : AppCompatActivity() {

    private lateinit var launcher: BrowserSwitchLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        launcher =
            BrowserSwitchLauncher(this) { result ->
                Log.d("SKOOP", result)
            }
        val launchButton = findViewById<Button>(R.id.browser_switch_launcher)
        launchButton.setOnClickListener { launchBrowserSwitch() }
    }

    private fun launchBrowserSwitch() {
        launcher.launch()
    }

}