package com.braintreepayments.api;

import android.content.Context;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChromeCustomTabsInternalClientUnitTest {

    CustomTabsIntent customTabsIntent;
    CustomTabsIntent.Builder customTabsIntentBuilder;

    @Before
    public void beforeEach() {
        customTabsIntent = mock(CustomTabsIntent.class);
        customTabsIntentBuilder = mock(CustomTabsIntent.Builder.class);
    }

    @Test
    public void launchUrl_launchesChromeCustomTab() {
        when(customTabsIntentBuilder.build()).thenReturn(customTabsIntent);

        ChromeCustomTabsInternalClient sut = new ChromeCustomTabsInternalClient(customTabsIntentBuilder);

        Uri url = mock(Uri.class);
        Context context = mock(Context.class);
        sut.launchUrl(context, url, false);

        verify(customTabsIntent).launchUrl(context, url);
    }
}