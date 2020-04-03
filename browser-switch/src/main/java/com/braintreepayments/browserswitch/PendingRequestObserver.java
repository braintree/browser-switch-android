package com.braintreepayments.browserswitch;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Observer;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

import java.lang.ref.WeakReference;

public class PendingRequestObserver implements Observer<PendingRequest> {

    public static PendingRequestObserver newInstance(Application application, BrowserSwitchListener listener) {
        // avoid leaking context (e.g. Activity, Fragment) by taking a weak reference here
        return newInstance(application, new WeakReference<>(listener));
    }

    @VisibleForTesting
    static PendingRequestObserver newInstance(Application application, WeakReference<BrowserSwitchListener> listenerRef) {
        BrowserSwitchRepository repository = BrowserSwitchRepository.newInstance(application);
        return new PendingRequestObserver(repository, listenerRef);
    }

    private WeakReference<BrowserSwitchListener> listenerRef;
    private BrowserSwitchRepository browserSwitchRepository;

    private PendingRequestObserver(BrowserSwitchRepository repository, WeakReference<BrowserSwitchListener> listenerRef) {
        this.browserSwitchRepository = repository;
        this.listenerRef = listenerRef;
    }

    @Override
    public void onChanged(@Nullable PendingRequest pendingRequest) {
        if (pendingRequest != null) {
            BrowserSwitchListener listener = listenerRef.get();
            if (listener != null) {
                BrowserSwitchEvent event =
                        BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK);
                listener.onBrowserSwitchEvent(event);

                // stop observing
                browserSwitchRepository.getPendingRequest().removeObservers(listener);
            }

            // delete all pending events now that we've notified completion; this is backwards
            // compatible with previous versions of browser switch
            browserSwitchRepository.deleteAll();
        }
    }
}
