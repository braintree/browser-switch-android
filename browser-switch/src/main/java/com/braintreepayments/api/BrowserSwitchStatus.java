package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The status of a {@link BrowserSwitchResult}.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({BrowserSwitchStatus.SUCCESS})
public @interface BrowserSwitchStatus {

    /**
     * Browser switch is considered a success when a user is deep linked back into the app.
     */
    int SUCCESS = 1;
}