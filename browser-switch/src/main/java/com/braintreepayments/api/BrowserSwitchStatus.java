package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({BrowserSwitchStatus.SUCCESS, BrowserSwitchStatus.CANCELED})
public @interface BrowserSwitchStatus {
    /**
     * Browser switch is considered a success when a user is deep linked back into the app.
     */
    int SUCCESS = 1;

    /**
     * Browser switch is considered canceled when a user re-enters the app without deep link.
     */
    int CANCELED = 2;
}