package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import org.json.JSONObject;

public class BrowserSwitchActivityResultContract extends
        ActivityResultContract<BrowserSwitchOptions, BrowserSwitchResult> {

    private BrowserSwitchRequest request;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, BrowserSwitchOptions browserSwitchOptions) {
        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();
        JSONObject metadata = browserSwitchOptions.getMetadata();
        request =
                new BrowserSwitchRequest(requestCode, browserSwitchUrl, metadata, returnUrlScheme, true);
        BrowserSwitchPersistentStore.getInstance().putActiveRequest(request, context);
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        Intent customTabsIntent = builder.build().intent;
        customTabsIntent.setData(browserSwitchUrl);
        return customTabsIntent;
    }

    @Override
    public BrowserSwitchResult parseResult(int resultCode, @Nullable Intent intent) {
        // A successful result will be returned via deep link back to the app, so the only result
        // that will be delivered via activity result is a cancel (ex: the user closed the browser)
        return new BrowserSwitchResult(BrowserSwitchStatus.INCOMPLETE, request);
    }
}
