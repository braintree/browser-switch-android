package com.braintreepayments.browserswitch;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowBinder;
import org.robolectric.shadows.ShadowContextImpl;

import static junit.framework.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChromeCustomTabsTest {
    @Test
    public void isAvailable_returnsFalseIfChromeCustomTabsAreNotSupported() {
        Context context = mockContextWithoutChromeCustomTabs();

        assertFalse(ChromeCustomTabs.isAvailable(context));
    }

    @Test
    public void addChromeCustomTabsExtras_doesNotModifyTheIntentIfChromeCustomTabsAreNotSupported() {
        Intent intent = mock(Intent.class);
        Context context = mockContextWithoutChromeCustomTabs();

        ChromeCustomTabs.addChromeCustomTabsExtras(context, intent);

        verifyZeroInteractions(intent);
    }

    public Context mockContextWithoutChromeCustomTabs() {
        Context context = mock(Context.class);

        doReturn(false).when(context).bindService(
                argThat((intent) -> "android.support.customtabs.action.CustomTabsService".equals(intent.getAction())),
                any(ServiceConnection.class),
                anyInt());

        return context;
    }
}
