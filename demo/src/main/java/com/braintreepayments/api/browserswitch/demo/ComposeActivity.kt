package com.braintreepayments.api.browserswitch.demo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.braintreepayments.api.BrowserSwitchClient
import com.braintreepayments.api.BrowserSwitchFinalResult
import com.braintreepayments.api.BrowserSwitchOptions
import com.braintreepayments.api.BrowserSwitchStartResult
import com.braintreepayments.api.browserswitch.demo.utils.PendingRequestStore
import com.braintreepayments.api.demo.viewmodel.BrowserSwitchViewModel
import org.json.JSONObject

class ComposeActivity : ComponentActivity() {

    private val viewModel by viewModels<BrowserSwitchViewModel>()
    private lateinit var browserSwitchClient: BrowserSwitchClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize BrowserSwitchClient with the parameterized constructor
        browserSwitchClient = BrowserSwitchClient(this)
        setContent {
            Column(modifier = Modifier.safeGesturesPadding()) {
                BrowserSwitchButton {
                    startBrowserSwitch()
                }
                BrowserSwitchResult(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PendingRequestStore.get(this)?.let { startedRequest ->
            val completeRequestResult = browserSwitchClient.completeRequest(intent, startedRequest)
            handleBrowserSwitchResult(completeRequestResult)
            PendingRequestStore.clear(this)
            intent.data = null
        }
    }

    private fun handleBrowserSwitchResult(result: BrowserSwitchFinalResult) {
        when (result) {
            is BrowserSwitchFinalResult.Success ->
                viewModel.browserSwitchFinalResult = result

            is BrowserSwitchFinalResult.NoResult ->
                viewModel.browserSwitchError = Exception("User did not complete browser switch")

            is BrowserSwitchFinalResult.Failure ->
                viewModel.browserSwitchError = result.error
        }
    }

    private fun startBrowserSwitch() {
        val url = buildBrowserSwitchUrl()
        val browserSwitchOptions = BrowserSwitchOptions()
            .metadata(buildMetadataObject())
            .requestCode(1)
            .url(url)
            .launchAsNewTask(false)
            .returnUrlScheme(RETURN_URL_SCHEME)

        when (val startResult = browserSwitchClient.start(this, browserSwitchOptions)) {
            is BrowserSwitchStartResult.Started -> {
                    PendingRequestStore.put(this, startResult.pendingRequest)
            }
            is BrowserSwitchStartResult.Failure ->
                viewModel.browserSwitchError = startResult.error
        }
    }

    private fun buildBrowserSwitchUrl(): Uri? {
        val url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=$RETURN_URL_SCHEME://"
        return Uri.parse(url)
    }

    private fun buildMetadataObject(): JSONObject? {
        return JSONObject().put("test_key", "test_value")
    }

    companion object {
        private const val RETURN_URL_SCHEME = "my-custom-url-scheme-standard"
    }
}

@Composable
fun BrowserSwitchResult(viewModel: BrowserSwitchViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    (uiState.browserSwitchFinalResult as? BrowserSwitchFinalResult.Success)?.let {
        BrowserSwitchSuccess(result = it)
    }
    uiState.browserSwitchError?.let { BrowserSwitchError(exception = it) }

}

@Composable
fun BrowserSwitchButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(text = "Start Browser Switch")
    }
}

@Composable
fun BrowserSwitchSuccess(result: BrowserSwitchFinalResult.Success) {
    val color = result.returnUrl.getQueryParameter("color")
    val selectedColorString = "Selected color: $color"
    val metadataOutput = result.requestMetadata?.getString("test_key")?.let { "test_key=$it" }
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            text = "Browser Switch Successful"
        )
        Text(text = selectedColorString, color = Color.White)
        metadataOutput?.let {
            Text(text = "Metadata: $it", color = Color.White)
        }
    }
}

@Composable
fun BrowserSwitchError(exception: Exception) {
    Column(modifier = Modifier.padding(10.dp)) {
        Text(
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            text = "Browser Switch Error"
        )
        exception.message?.let { Text(text = it, color = Color.White) }
    }
}