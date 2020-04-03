package com.braintreepayments.browserswitch;

import android.net.Uri;

public class BrowserSwitchEvent {

    public final BrowserSwitchResult result;
    public final int requestCode;
    public final Uri returnUri;

    // TODO: move Browser switch result into its own class
    public BrowserSwitchEvent(BrowserSwitchResult result, int requestCode, Uri returnUri) {
        this.result = result;
        this.requestCode = requestCode;
        this.returnUri = returnUri;
    }
}
