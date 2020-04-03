package com.braintreepayments.browserswitch;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.ref.WeakReference;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BrowserSwitchRepository.class, BrowserSwitchEvent.class })
public class PendingRequestObserverTest {

    private WeakReference<BrowserSwitchListener> listenerRef;
    private BrowserSwitchListener listener;

    private PendingRequest pendingRequest;
    private BrowserSwitchEvent browserSwitchEvent;

    private PendingRequestObserver sut;

    @Before
    public void beforeEach() {
        mockStatic(BrowserSwitchEvent.class);

        pendingRequest = mock(PendingRequest.class);
        browserSwitchEvent = mock(BrowserSwitchEvent.class);

        // Ref: https://stackoverflow.com/a/1652738
        listenerRef = (WeakReference<BrowserSwitchListener>) mock(WeakReference.class);
        listener = mock(BrowserSwitchListener.class);
    }

    @Test
    public void onChanged_whenPendingRequestIsNull_doesNothing() {
        sut = PendingRequestObserver.newInstance(listenerRef);
        sut.onChanged(null);

        verify(listener, never()).onBrowserSwitchEvent(any());
    }

    @Test
    public void onChange_whenPendingRequestIsNonNull_andListenerIsNull_doesNothing() {
        when(BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK)).thenReturn(browserSwitchEvent);
        sut = PendingRequestObserver.newInstance(listenerRef);

        Exception capturedException = null;
        try {
            sut.onChanged(pendingRequest);
        } catch (Exception e) {
            capturedException = e;
        } finally {
            assertNull(capturedException);
        }
    }

    @Test
    public void onChanged_whenPendingRequestIsNonNull_notifiesListenerOfEvent() {
        when(listenerRef.get()).thenReturn(listener);
        when(BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK)).thenReturn(browserSwitchEvent);

        sut = PendingRequestObserver.newInstance(listenerRef);
        sut.onChanged(pendingRequest);

        verify(listener).onBrowserSwitchEvent(browserSwitchEvent);
    }
}
