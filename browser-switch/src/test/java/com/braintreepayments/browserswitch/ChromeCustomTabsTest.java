package com.braintreepayments.browserswitch;

import android.content.Intent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class ChromeCustomTabsTest {

    @Test
    public void isAvailable_returnsFalseIfChromeCustomTabsAreNotSupported() {
        assertFalse(ChromeCustomTabs.isAvailable(RuntimeEnvironment.application));
    }

    @Test
    public void addChromeCustomTabsExtras_doesNotModifyTheIntentIfChromeCustomTabsAreNotSupported() {
        Intent intent = mock(Intent.class);

        ChromeCustomTabs.addChromeCustomTabsExtras(RuntimeEnvironment.application, intent);

        verifyZeroInteractions(intent);
    }
}
