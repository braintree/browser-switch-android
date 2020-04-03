package com.braintreepayments.browserswitch;

import android.net.Uri;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BrowserSwitchRepository.class, Uri.class })
public class PendingRequestObserverTest {

    WeakReference<BrowserSwitchListener> listenerRef;
    BrowserSwitchListener listener;

    PendingRequest pendingRequest;

    Uri uri;
    PendingRequestObserver sut;

    @Before
    public void beforeEach() {
        mockStatic(Uri.class);

        // Ref: https://stackoverflow.com/a/1652738
        listenerRef = (WeakReference<BrowserSwitchListener>) mock(WeakReference.class);
        listener = mock(BrowserSwitchListener.class);

        uri = mock(Uri.class);
        pendingRequest = mock(PendingRequest.class);
    }

    @Test
    public void onChanged_whenPendingRequestIsNonNull_notifiesListenerOfEvent() {
        when(listenerRef.get()).thenReturn(listener);
        when(pendingRequest.getRequestCode()).thenReturn(123);
        when(pendingRequest.getUrl()).thenReturn("https://example.com");
        when(Uri.parse("https://example.com")).thenReturn(uri);

        sut = PendingRequestObserver.newInstance(listenerRef);
        sut.onChanged(pendingRequest);

        ArgumentCaptor<BrowserSwitchEvent> captor = ArgumentCaptor.forClass(BrowserSwitchEvent.class);
        verify(listener).onBrowserSwitchEvent(captor.capture());

        BrowserSwitchEvent event = captor.getValue();
        assertEquals(event.result, BrowserSwitchResult.OK);
        assertEquals(event.requestCode, 123);
        assertEquals(event.returnUri, uri);
    }

    @Test
    public void onChanged_whenPendingRequestIsNull_doesNothing() {
        sut = PendingRequestObserver.newInstance(listenerRef);
        sut.onChanged(null);

        verify(listener, never()).onBrowserSwitchEvent(any());
    }
}
