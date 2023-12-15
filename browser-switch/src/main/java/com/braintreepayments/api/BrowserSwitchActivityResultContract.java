package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Browser;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;

import org.json.JSONObject;

public class BrowserSwitchActivityResultContract extends
        ActivityResultContract<BrowserSwitchOptions, BrowserSwitchResult> {

    private BrowserSwitchRequest request;
    private Context context;

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, BrowserSwitchOptions browserSwitchOptions) {
        Uri browserSwitchUrl = browserSwitchOptions.getUrl();
        int requestCode = browserSwitchOptions.getRequestCode();
        String returnUrlScheme = browserSwitchOptions.getReturnUrlScheme();
        this.context = context;
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
        return new BrowserSwitchResult(BrowserSwitchStatus.CANCELED, request);
    }
}
