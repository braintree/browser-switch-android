package com.braintreepayments.api

import android.net.Uri
import org.json.JSONObject
import org.junit.Test

class BrowserSwitchOptionsUnitTest {

    @Test
    fun createOptions_doesSomething() {
        val browserSwitchOptions = BrowserSwitchOptions()
        
        browserSwitchOptions.url(Uri.parse("something"))
        browserSwitchOptions.metadata(JSONObject())
    }


}