package com.braintreepayments.api;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The status of a {@link BrowserSwitchResult}.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({BrowserSwitchStatus.SUCCESS, BrowserSwitchStatus.INCOMPLETE})
public @interface BrowserSwitchStatus {

    /**
     * Browser switch is considered a success when a user is deep linked back into the app.
     */
    int SUCCESS = 1;

    /**
     * Browser switch is considered incomplete when a user re-enters the app without deep link. This
     * may happen if the user closes the Chrome Custom Tab or navigates away from the browser and
     * back into the launching app. If the user returns to the browser and completes the flow, this
     * result may become SUCCESS.
     */
    int INCOMPLETE = 2;
}