package com.braintreepayments.browserswitch;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;
import com.braintreepayments.browserswitch.db.PendingRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BrowserSwitchRepository.class, ChromeCustomTabs.class, PendingRequestObserver.class })
public class BrowserSwitchTest {

    private static abstract class ListenerFragment extends Fragment implements BrowserSwitchListener {}
    private static abstract class ListenerFragmentActivity extends FragmentActivity implements BrowserSwitchListener {}

    private Uri uri;
    private Intent intent;

    private Application application;
    private BrowserSwitchRepository repository;

    private LiveData<PendingRequest> pendingRequest;
    private PendingRequestObserver pendingRequestObserver;

    private Context applicationContext;

    private ListenerFragment fragment;
    private ListenerFragmentActivity activity;

    @Before
    public void beforeEach() {
        mockStatic(ChromeCustomTabs.class);
        mockStatic(BrowserSwitchRepository.class);
        mockStatic(PendingRequestObserver.class);

        uri = mock(Uri.class);
        intent = mock(Intent.class);

        application = mock(Application.class);
        repository = mock(BrowserSwitchRepository.class);

        fragment = mock(ListenerFragment.class);
        activity = mock(ListenerFragmentActivity.class);
        applicationContext = mock(Context.class);

        pendingRequest = (LiveData<PendingRequest>) mock(LiveData.class);
        pendingRequestObserver = mock(PendingRequestObserver.class);

        // Ref: https://stackoverflow.com/a/11837973
    }

    @Test
    public void start_configuresIntentForBrowserSwitching() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, activity)).thenReturn(pendingRequestObserver);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, activity, intent);

        verify(intent).setData(uri);
        verify(intent).setAction(Intent.ACTION_VIEW);
        verify(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void start_startsActivityUsingIntent() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, activity)).thenReturn(pendingRequestObserver);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, activity, intent);

        verify(applicationContext).startActivity(intent);
    }

    @Test
    public void start_withFragmentActivity_addsFragmentActivityAsListener() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, activity)).thenReturn(pendingRequestObserver);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, activity, intent);
        verify(pendingRequest).observe(activity, pendingRequestObserver);
    }

    @Test
    public void start_withFragment_addsFragmentAsListener() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, fragment)).thenReturn(pendingRequestObserver);

        // Ref: https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2#mock-the-unmockable-opt-in-mocking-of-final-classesmethods
        // Ref: https://github.com/powermock/powermock/wiki/mockito#mockito-mock-maker-inline
        when(fragment.getActivity()).thenReturn(activity);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, fragment, intent);
        verify(pendingRequest).observe(fragment, pendingRequestObserver);
    }

    @Test
    public void start_whenChromeCustomTabsNotAvailable_doesNothing() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, activity)).thenReturn(pendingRequestObserver);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, activity, intent);

        verifyStatic(ChromeCustomTabs.class, never());
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
    }

    @Test
    public void start_whenChromeCustomTabsAvailable_addsChromeCustomTabs() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(PendingRequestObserver.newInstance(application, activity)).thenReturn(pendingRequestObserver);
        when(ChromeCustomTabs.isAvailable(applicationContext)).thenReturn(true);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);
        when(repository.getPendingRequest()).thenReturn(pendingRequest);

        BrowserSwitch.start(123, uri, activity, intent);

        verifyStatic(ChromeCustomTabs.class);
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
    }
}
