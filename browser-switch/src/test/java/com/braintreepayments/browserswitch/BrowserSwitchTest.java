package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ ChromeCustomTabs.class })
public class BrowserSwitchTest {

    private Uri uri;
    private Intent intent;

    private Context context;
    private Context applicationContext;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ChromeCustomTabs.class);

        uri = mock(Uri.class);
        intent = mock(Intent.class);

        context = mock(Context.class);
        applicationContext = mock(Context.class);
    }

    @Test
    public void start_setsUriOnIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, context, intent);
        verify(intent).setData(uri);
    }

    @Test
    public void start_setsActionViewOnIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, context, intent);
        verify(intent).setAction(Intent.ACTION_VIEW);
    }

    @Test
    public void start_setsIntentFlags() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, context, intent);
        verify(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void start_startsActivityUsingIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        BrowserSwitch.start(123, uri, context, intent);
        verify(applicationContext).startActivity(intent);
    }

    @Test
    public void start_addsChromeCustomTabsExtras_whenAvailable() {
        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(ChromeCustomTabs.isAvailable(applicationContext)).thenReturn(true);

        BrowserSwitch.start(123, uri, context, intent);
        verifyStatic(ChromeCustomTabs.class);
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
    }
}
