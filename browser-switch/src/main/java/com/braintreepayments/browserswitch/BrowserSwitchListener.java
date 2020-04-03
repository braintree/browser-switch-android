package com.braintreepayments.browserswitch;

import androidx.lifecycle.LifecycleOwner;

public interface BrowserSwitchListener extends LifecycleOwner {
    void onBrowserSwitchEvent(BrowserSwitchEvent event);
}
