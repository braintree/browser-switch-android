package com.braintreepayments.browserswitch;

import android.net.Uri;

import com.braintreepayments.browserswitch.db.PendingRequest;

public class BrowserSwitchEvent {

    public static BrowserSwitchEvent from(PendingRequest pendingRequest, BrowserSwitchResult result) {
        Uri uri = Uri.parse(pendingRequest.getUrl());
        int requestCode = pendingRequest.getRequestCode();
        return new BrowserSwitchEvent(result, requestCode, uri);
    }

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
