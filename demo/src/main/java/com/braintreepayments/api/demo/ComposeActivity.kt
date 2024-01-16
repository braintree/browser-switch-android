package com.braintreepayments.api.demo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.braintreepayments.api.BrowserSwitchClient
import com.braintreepayments.api.BrowserSwitchOptions
import com.braintreepayments.api.BrowserSwitchPendingRequest
import com.braintreepayments.api.demo.viewmodel.BrowserSwitchViewModel
import org.json.JSONException
import org.json.JSONObject

class ComposeActivity : ComponentActivity() {

    private val RETURN_URL_SCHEME = "my-custom-url-scheme-standard"

    private val viewModel by viewModels<BrowserSwitchViewModel>()

    private lateinit var browserSwitchClient: BrowserSwitchClient
    private var pendingRequest: BrowserSwitchPendingRequest.Started? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserSwitchClient = BrowserSwitchClient()

        setContent {
            BrowserSwitchButton {
                startBrowserSwitch()
            }
            BrowserSwitchResult(viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        pendingRequest?.let { startedRequest ->
            val browserSwitchResult = browserSwitchClient.parseResult(startedRequest, intent)
            browserSwitchResult?.let { result ->
                viewModel.browserSwitchResult = result
            }
        }
    }

    private fun startBrowserSwitch() {
        val url = buildBrowserSwitchUrl()
        val browserSwitchOptions = BrowserSwitchOptions()
            .metadata(buildMetadataObject())
            .requestCode(1)
            .url(url)
            .launchAsNewTask(true)
            .returnUrlScheme(RETURN_URL_SCHEME)
        when (val pendingRequest = browserSwitchClient.start(this, browserSwitchOptions)) {
            is BrowserSwitchPendingRequest.Started -> {}
            is BrowserSwitchPendingRequest.Failure -> { viewModel.browserSwitchError = pendingRequest.cause }
        }
    }

    private fun buildBrowserSwitchUrl(): Uri? {
        val url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=${RETURN_URL_SCHEME}://"
        return Uri.parse(url)
    }

    private fun buildMetadataObject(): JSONObject? {
        try {
            return JSONObject().put("test_key","test_value")
        } catch (ignore: JSONException) {
            // do nothing
        }
        return null
    }
}

@Composable
fun BrowserSwitchResult(viewModel: BrowserSwitchViewModel) {
}

@Composable
fun BrowserSwitchButton(onClick: () -> Unit) {
    Button(modifier = Modifier.fillMaxWidth(),
        onClick = onClick) {
        Text(text = "Start Browser Switch")
    }
}

