package com.braintreepayments.api;

import androidx.annotation.NonNull;

public interface BrowserSwitchLauncherCallback {

    void onResult(@NonNull BrowserSwitchResult browserSwitchResult);
}
