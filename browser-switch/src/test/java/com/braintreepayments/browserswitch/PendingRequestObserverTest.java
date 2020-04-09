package com.braintreepayments.browserswitch;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

import org.junit.Before;
import org.junit.Ignore;
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

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({ BrowserSwitchRepository.class, BrowserSwitchEvent.class })
public class PendingRequestObserverTest {

    private WeakReference<BrowserSwitchListener> listenerRef;
    private BrowserSwitchListener listener;

    private Application application;
    private BrowserSwitchRepository repository;

    private PendingRequest pendingRequest;
    private LiveData<PendingRequest> pendingRequestLiveData;

    private BrowserSwitchEvent browserSwitchEvent;
    private PendingRequestObserver sut;

    @Before
    public void beforeEach() {
        mockStatic(BrowserSwitchEvent.class);
        mockStatic(BrowserSwitchRepository.class);

        application = mock(Application.class);
        repository = mock(BrowserSwitchRepository.class);

        pendingRequest = mock(PendingRequest.class);
        pendingRequestLiveData = (LiveData<PendingRequest>) mock(LiveData.class);

        browserSwitchEvent = mock(BrowserSwitchEvent.class);

        // Ref: https://stackoverflow.com/a/1652738
        listenerRef = (WeakReference<BrowserSwitchListener>) mock(WeakReference.class);
        listener = mock(BrowserSwitchListener.class);
    }

    @Test
    public void onChanged_whenPendingRequestIsNull_doesNothing() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(repository.getPendingRequest()).thenReturn(pendingRequestLiveData);

        sut = PendingRequestObserver.newInstance(application, listenerRef);
        sut.onChanged(null);

        verify(listener, never()).onBrowserSwitchEvent(any());
        verify(repository, never()).deleteAll();
        verify(pendingRequestLiveData, never()).removeObservers(any());
    }

    @Test
    public void onChange_whenPendingRequestIsPresent_deletesAllPendingRequestsFromRepository() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(BrowserSwitchEvent.from(any(), any())).thenReturn(browserSwitchEvent);

        sut = PendingRequestObserver.newInstance(application, listenerRef);
        sut.onChanged(pendingRequest);

        verify(repository).deleteAll();
    }

    @Test
    public void onChange_whenPendingRequestIsPresentAndListenerIsNull_doesNothing() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK)).thenReturn(browserSwitchEvent);

        sut = PendingRequestObserver.newInstance(application, listenerRef);

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
    public void onChanged_whenPendingRequestAndListenerArePresent_notifiesListenerOfEvent() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(repository.getPendingRequest()).thenReturn(pendingRequestLiveData);

        when(BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK)).thenReturn(browserSwitchEvent);
        when(listenerRef.get()).thenReturn(listener);

        sut = PendingRequestObserver.newInstance(application, listenerRef);
        sut.onChanged(pendingRequest);

        verify(listener).onBrowserSwitchEvent(browserSwitchEvent);
    }

    @Test
    public void onChanged_whenPendingRequestAndListenerArePresent_deletesAllPendingRequestsAndStopsObserving() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(repository.getPendingRequest()).thenReturn(pendingRequestLiveData);

        when(BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK)).thenReturn(browserSwitchEvent);
        when(listenerRef.get()).thenReturn(listener);

        sut = PendingRequestObserver.newInstance(application, listenerRef);
        sut.onChanged(pendingRequest);

        verify(repository).deleteAll();
        verify(pendingRequestLiveData).removeObservers(listener);
    }
}
