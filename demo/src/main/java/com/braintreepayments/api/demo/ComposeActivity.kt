package com.braintreepayments.api.demo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.braintreepayments.api.BrowserSwitchOptions
import com.braintreepayments.api.BrowserSwitchStartResult
import com.braintreepayments.api.BrowserSwitchParseResult
import com.braintreepayments.api.demo.utils.PendingRequestStore
import com.braintreepayments.api.demo.viewmodel.BrowserSwitchViewModel
import org.json.JSONObject
import java.lang.Exception

class ComposeActivity : ComponentActivity() {

    private val viewModel by viewModels<BrowserSwitchViewModel>()

    private lateinit var browserSwitchClient: BrowserSwitchClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserSwitchClient = BrowserSwitchClient()

        setContent {
            Column(modifier = Modifier.padding(10.dp)) {
                BrowserSwitchButton {
                    startBrowserSwitch()
                }
                BrowserSwitchResult(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PendingRequestStore.get(this)?.let { pendingRequestState ->
            when (val result = browserSwitchClient.parseResult(intent, pendingRequestState)) {
                is BrowserSwitchParseResult.Success -> {
                    viewModel.browserSwitchParseResult = result
                    PendingRequestStore.clear(this)
                }

                is BrowserSwitchParseResult.Failure ->
                    viewModel.browserSwitchError = result.error

                is BrowserSwitchParseResult.NoResult ->
                    viewModel.browserSwitchError = Exception("User did not complete browser switch")
            }
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
        when (val pendingRequest = browserSwitchClient.start(this, browserSwitchOptions)) {
            is BrowserSwitchStartResult.Success ->
                PendingRequestStore.put(this, pendingRequest.pendingRequestState)

            is BrowserSwitchStartResult.Failure -> viewModel.browserSwitchError =
                pendingRequest.cause
        }
    }

    private fun buildBrowserSwitchUrl(): Uri? {
        val url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=${RETURN_URL_SCHEME}://"
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
    uiState.browserSwitchParseResult?.let {
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
fun BrowserSwitchSuccess(result: BrowserSwitchParseResult.Success) {
    val returnUrl = result.deepLinkUrl

    val color = returnUrl.getQueryParameter("color")
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

