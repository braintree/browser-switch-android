package com.braintreepayments.browserswitch;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.braintreepayments.browserswitch.db.BrowserSwitchRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BrowserSwitchRepository.class, ChromeCustomTabs.class })
public class BrowserSwitchTest {

    private Uri uri;
    private Intent intent;

    private Application application;
    private BrowserSwitchRepository repository;

    private AppCompatActivity activity;
    private Context applicationContext;

    @Before
    public void beforeEach() {
        PowerMockito.mockStatic(ChromeCustomTabs.class);
        PowerMockito.mockStatic(BrowserSwitchRepository.class);

        uri = mock(Uri.class);
        intent = mock(Intent.class);
        application = mock(Application.class);

        activity = mock(AppCompatActivity.class);
        applicationContext = mock(Context.class);
        repository = mock(BrowserSwitchRepository.class);

        // Ref: https://stackoverflow.com/a/11837973
    }

    @Test
    public void start_configuresIntentForBrowserSwitching() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, activity, intent);

        verify(intent).setData(uri);
        verify(intent).setAction(Intent.ACTION_VIEW);
        verify(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void start_startsActivityUsingIntent() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, activity, intent);

        verify(applicationContext).startActivity(intent);
    }

    @Test
    public void start_whenChromeCustomTabsNotAvailable_doesNothing() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, activity, intent);

        verifyStatic(ChromeCustomTabs.class, never());
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
    }

    @Test
    public void start_whenChromeCustomTabsAvailable_addsChromeCustomTabs() {
        when(BrowserSwitchRepository.newInstance(application)).thenReturn(repository);
        when(ChromeCustomTabs.isAvailable(applicationContext)).thenReturn(true);

        when(activity.getApplication()).thenReturn(application);
        when(activity.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, activity, intent);
        verifyStatic(ChromeCustomTabs.class);
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
    }
}
