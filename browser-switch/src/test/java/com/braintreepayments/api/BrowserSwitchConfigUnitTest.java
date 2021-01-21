package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({ "org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({ ChromeCustomTabs.class })
public class BrowserSwitchConfigUnitTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    private Context context;
    private Context applicationContext;

    private BrowserSwitchConfig sut;

    @Before
    public void beforeEach() {
        mockStatic(ChromeCustomTabs.class);

        context = mock(Context.class);
        applicationContext = mock(Context.class);

        sut = new BrowserSwitchConfig();
    }

    @Test
    public void createIntentToLaunchUriInBrowser_returnsIntent() {
        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(ChromeCustomTabs.isAvailable(applicationContext)).thenReturn(false);

        Uri uri = Uri.parse("https://www.example.com");
        Intent result = sut.createIntentToLaunchUriInBrowser(context, uri);

        assertEquals(result.getData().toString(), "https://www.example.com");
        assertEquals(result.getAction(), Intent.ACTION_VIEW);
        assertEquals(result.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK);

        verifyStatic(ChromeCustomTabs.class, never());
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, result);
    }

    @Test
    public void createIntentToLaunchUriInBrowser_optionallyConfiguresIntentForChromeCustomTabs() {
        when(context.getApplicationContext()).thenReturn(applicationContext);
        when(ChromeCustomTabs.isAvailable(applicationContext)).thenReturn(true);

        Uri uri = Uri.parse("https://www.example.com");
        Intent result = sut.createIntentToLaunchUriInBrowser(context, uri);

        assertEquals(result.getData().toString(), "https://www.example.com");
        assertEquals(result.getAction(), Intent.ACTION_VIEW);
        assertEquals(result.getFlags(), Intent.FLAG_ACTIVITY_NEW_TASK);

        verifyStatic(ChromeCustomTabs.class);
        ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, result);
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
