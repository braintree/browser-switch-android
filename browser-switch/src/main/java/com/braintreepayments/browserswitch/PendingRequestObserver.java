package com.braintreepayments.browserswitch;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Observer;

import com.braintreepayments.browserswitch.db.PendingRequest;

import java.lang.ref.WeakReference;

public class PendingRequestObserver implements Observer<PendingRequest> {

    public static PendingRequestObserver newInstance(BrowserSwitchListener listener) {
        // avoid leaking context (i.e. Activity, Fragment) by taking a weak reference here
        return new PendingRequestObserver(new WeakReference<>(listener));
    }

    @VisibleForTesting
    static PendingRequestObserver newInstance(WeakReference<BrowserSwitchListener> listenerRef) {
        return new PendingRequestObserver(listenerRef);
    }

    private WeakReference<BrowserSwitchListener> listenerRef;

    private PendingRequestObserver(WeakReference<BrowserSwitchListener> listenerRef) {
        this.listenerRef = listenerRef;
    }

    @Override
    public void onChanged(@Nullable PendingRequest pendingRequest) {
        if (pendingRequest != null) {
            Uri uri = Uri.parse(pendingRequest.getUrl());
            int requestCode = pendingRequest.getRequestCode();
            BrowserSwitchEvent event =
                new BrowserSwitchEvent(BrowserSwitchResult.OK, requestCode, uri);

            BrowserSwitchListener listener = listenerRef.get();
            listener.onBrowserSwitchEvent(event);
        }
    }
}
