package com.braintreepayments.api.demo

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.braintreepayments.api.BrowserSwitchOptions
import com.braintreepayments.api.BrowserSwitchResult
import com.braintreepayments.api.BrowserSwitchStatus
import com.braintreepayments.api.BrowserSwitchLauncher
import org.json.JSONException
import org.json.JSONObject

class LauncherActivityNoButton : AppCompatActivity() {

    private lateinit var launcher: BrowserSwitchLauncher
    private var browserSwitchHandled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher_no_button)
        launcher =
            BrowserSwitchLauncher(this) { browserSwitchResult ->
                onBrowserSwitchResult(browserSwitchResult)
                browserSwitchHandled = true
            }
        launcher.handleReturnToAppFromBrowser(this, 1, intent)

        if (!browserSwitchHandled) {
            launchBrowserSwitch()
        }
    }

    override fun onResume() {
        super.onResume()
        launcher.handleReturnToAppFromBrowser(this, 1, intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
        }
    }
    private fun launchBrowserSwitch() {
        val metadata: JSONObject? = buildMetadataObject()
        val url: Uri = buildBrowserSwitchUrl()

        val browserSwitchOptions = BrowserSwitchOptions()
            .requestCode(1)
            .metadata(metadata)
            .url(url)
            .returnUrlScheme("launcher-activity-no-button")
        launcher.launch(browserSwitchOptions)
    }

    fun onBrowserSwitchResult(result: BrowserSwitchResult) {
        var resultText: String? = null
        var selectedColorText = ""
        val statusCode = result.status
        when (statusCode) {
            BrowserSwitchStatus.SUCCESS -> {
                resultText = "Browser Switch Successful"
                val returnUrl = result.deepLinkUrl
                if (returnUrl != null) {
                    val color = returnUrl.getQueryParameter("color")
                    selectedColorText = String.format("Selected color: %s", color)
                }
            }

            BrowserSwitchStatus.CANCELED -> resultText = "Browser Switch Cancelled by User"
        }
        var metadataOutput: String? = null
        val requestMetadata = result.requestMetadata
        if (requestMetadata != null) {
            try {
                val metadataValue = result.requestMetadata!!.getString("testKey")
                metadataOutput = String.format("%s=%s", "testKey", metadataValue)
            } catch (ignore: JSONException) {
                // do nothing
            }
        }
        Toast.makeText(this, resultText, Toast.LENGTH_LONG).show()
    }
    private fun buildMetadataObject(): JSONObject? {
        try {
            return JSONObject().put("testKey", "testValue")
        } catch (ignore: JSONException) {
            // do nothing
        }
        return null
    }

    private fun buildBrowserSwitchUrl(): Uri {
        val url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" +
                "launcher-activity-no-button" + "://"
        return Uri.parse(url)
    }
}