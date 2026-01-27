package com.braintreepayments.api.browserswitch.demo

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGesturesPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.braintreepayments.api.BrowserSwitchClient
import com.braintreepayments.api.BrowserSwitchException
import com.braintreepayments.api.BrowserSwitchFinalResult
import com.braintreepayments.api.BrowserSwitchOptions
import com.braintreepayments.api.BrowserSwitchStartResult
import com.braintreepayments.api.browserswitch.demo.utils.PendingRequestStore
import com.braintreepayments.api.browserswitch.demo.viewmodel.BrowserSwitchViewModel
import org.json.JSONObject

private const val RETURN_URL_SCHEME = "my-custom-url-scheme-standard"

@Composable
fun MainContent() {
    val viewModel: BrowserSwitchViewModel = viewModel { BrowserSwitchViewModel() }
    val browserSwitchClient: BrowserSwitchClient = LocalActivityResultRegistryOwner.current?.let { BrowserSwitchClient(it.activityResultRegistry) }
            ?: BrowserSwitchClient()

    val context = LocalContext.current
    val activity = context.findActivity()

    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        PendingRequestStore.get(context)?.let { pendingRequest ->
            try {
                browserSwitchClient.restorePendingRequest(pendingRequest)
            } catch (e: BrowserSwitchException) {
                Log.e("ComposeActivity", "Failed to restore pending request", e)
                PendingRequestStore.clear(context)
            }
        }
    }

    Column(modifier = Modifier.safeGesturesPadding()) {
        BrowserSwitchButton {
            activity?.let { startBrowserSwitch(it, viewModel, browserSwitchClient) }
        }
        BrowserSwitchResult(viewModel = viewModel)
    }

    LifecycleResumeEffect(Unit) {
        val context = (context as ComponentActivity)
        PendingRequestStore.get(context)?.let { startedRequest ->
            val intent = context.intent
            val completeRequestResult = browserSwitchClient.completeRequest(intent, startedRequest)
            handleBrowserSwitchResult(viewModel, completeRequestResult)
            PendingRequestStore.clear(context)
            intent.data = null
            browserSwitchClient.cleanup()
        }

        onPauseOrDispose { lifecycle }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun handleBrowserSwitchResult(viewModel: BrowserSwitchViewModel, result: BrowserSwitchFinalResult) {
    when (result) {
        is BrowserSwitchFinalResult.Success ->
            viewModel.browserSwitchFinalResult = result

        is BrowserSwitchFinalResult.NoResult ->
            viewModel.browserSwitchError = Exception("User did not complete browser switch")

        is BrowserSwitchFinalResult.Failure ->
            viewModel.browserSwitchError = result.error
    }
}

private fun startBrowserSwitch(
    activity: Activity,
    viewModel: BrowserSwitchViewModel,
    browserSwitchClient: BrowserSwitchClient
) {
    val url = buildBrowserSwitchUrl()
    val browserSwitchOptions = BrowserSwitchOptions()
        .metadata(buildMetadataObject())
        .requestCode(1)
        .url(url)
        .launchAsNewTask(false)
        .returnUrlScheme(RETURN_URL_SCHEME)

    when (val startResult = browserSwitchClient.start(activity, browserSwitchOptions)) {
        is BrowserSwitchStartResult.Started -> {
            PendingRequestStore.put(activity.applicationContext, startResult.pendingRequest)
        }
        is BrowserSwitchStartResult.Failure ->
            viewModel.browserSwitchError = startResult.error
    }
}

private fun buildBrowserSwitchUrl(): Uri? {
    val url = "https://braintree.github.io/popup-bridge-example/" +
        "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=$RETURN_URL_SCHEME://"
    return url.toUri()
}

private fun buildMetadataObject(): JSONObject? {
    return JSONObject().put("test_key", "test_value")
}

@Composable
private fun BrowserSwitchResult(viewModel: BrowserSwitchViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    (uiState.browserSwitchFinalResult as? BrowserSwitchFinalResult.Success)?.let {
        BrowserSwitchSuccess(result = it)
    }
    uiState.browserSwitchError?.let { BrowserSwitchError(exception = it) }
}

@Composable
private fun BrowserSwitchButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(text = "Start Browser Switch")
    }
}

@Composable
private fun BrowserSwitchSuccess(result: BrowserSwitchFinalResult.Success) {
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
private fun BrowserSwitchError(exception: Exception) {
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
