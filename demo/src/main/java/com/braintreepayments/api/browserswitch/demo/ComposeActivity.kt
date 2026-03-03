package com.braintreepayments.api.browserswitch.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.ui.Modifier

class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(modifier = Modifier.safeGesturesPadding()) {
                MainContent()
            }
        }
    }
}
