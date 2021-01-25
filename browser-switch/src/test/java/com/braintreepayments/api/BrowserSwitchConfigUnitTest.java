package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchConfigUnitTest {

    private Context context;
    private Context applicationContext;

    private BrowserSwitchConfig sut;

    @Before
    public void beforeEach() {
        context = mock(Context.class);
        applicationContext = mock(Context.class);

        sut = new BrowserSwitchConfig();
    }

    @Test
    public void createIntentToLaunchUriInBrowser_returnsIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        Uri uri = Uri.parse("https://www.example.com");
        Intent result = sut.createIntentToLaunchUriInBrowser(uri);

        assertEquals(result.getData().toString(), "https://www.example.com");
        assertEquals(result.getAction(), Intent.ACTION_VIEW);
        assertEquals(result.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void createIntentToLaunchUriInBrowser_optionallyConfiguresIntentForChromeCustomTabs() {
        when(context.getApplicationContext()).thenReturn(applicationContext);

        Uri uri = Uri.parse("https://www.example.com");
        Intent result = sut.createIntentToLaunchUriInBrowser(uri);

        assertEquals(result.getData().toString(), "https://www.example.com");
        assertEquals(result.getAction(), Intent.ACTION_VIEW);
        assertEquals(result.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Test
    public void createIntentForBrowserSwitchActivityQuery_configuresAndReturnsIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getPackageName()).thenReturn("sample.package.name");

        String returnUrlScheme = "sample.package.name.browserswitch";
        Intent result = sut.createIntentForBrowserSwitchActivityQuery(returnUrlScheme);

        assertEquals(result.getData().toString(), "sample.package.name.browserswitch://");
        assertEquals(result.getAction(), Intent.ACTION_VIEW);
        assertTrue(result.hasCategory(Intent.CATEGORY_DEFAULT));
        assertTrue(result.hasCategory(Intent.CATEGORY_BROWSABLE));
    }
}
