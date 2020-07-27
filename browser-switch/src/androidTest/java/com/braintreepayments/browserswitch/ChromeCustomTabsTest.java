package com.braintreepayments.browserswitch;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

import com.braintreepayments.browserswitch.test.TestBrowserSwitchFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(AndroidJUnit4.class)
public class ChromeCustomTabsTest {

    private FragmentScenario<TestBrowserSwitchFragment> scenario;

    @Before
    public void beforeEach() {
        scenario = FragmentScenario.launch(TestBrowserSwitchFragment.class);
    }

    @Test
    @SdkSuppress(maxSdkVersion = 23)
    public void isAvailable_whenCustomTabsAreNotSupported_returnsFalse() {
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            assertFalse(ChromeCustomTabs.isAvailable(activity.getApplication()));
        });
    }

    @Test
    @SdkSuppress(minSdkVersion = 24)
    public void isAvailable_whenCustomTabsAreSupported_returnsTrue() {
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            assertTrue(ChromeCustomTabs.isAvailable(activity.getApplication()));
        });
    }

    @Test
    @SdkSuppress(maxSdkVersion = 23)
    public void addChromeCustomTabsExtras_whenCustomTabsAreNotSupported_doesNotModifyTheIntent() {
        Intent intent = mock(Intent.class);
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            ChromeCustomTabs.addChromeCustomTabsExtras(activity.getApplication(), intent);
            verifyZeroInteractions(intent);
        });
    }

    @Test
    @SdkSuppress(minSdkVersion = 24)
    public void addChromeCustomTabsExtras_whenCustomTabsAreSupported_modifiesTheIntent() {
        Intent intent = mock(Intent.class);
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            ChromeCustomTabs.addChromeCustomTabsExtras(activity.getApplication(), intent);

            verify(intent).putExtras(any(Bundle.class));
            verify(intent).addFlags(anyInt());
        });
    }
}
