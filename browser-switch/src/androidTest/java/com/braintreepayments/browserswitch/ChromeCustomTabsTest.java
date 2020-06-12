package com.braintreepayments.browserswitch;

import android.content.Intent;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.braintreepayments.browserswitch.test.TestBrowserSwitchFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(AndroidJUnit4.class)
public class ChromeCustomTabsTest {

    private FragmentScenario<TestBrowserSwitchFragment> scenario;

    @Before
    public void beforeEach() {
        scenario = FragmentScenario.launch(TestBrowserSwitchFragment.class);
    }

    @Test
    public void isAvailable_returnsFalseIfChromeCustomTabsAreNotSupported() {
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            assertFalse(ChromeCustomTabs.isAvailable(activity.getApplication()));
        });
    }

    @Test
    public void addChromeCustomTabsExtras_doesNotModifyTheIntentIfChromeCustomTabsAreNotSupported() {
        Intent intent = mock(Intent.class);
        scenario.onFragment(fragment -> {
            FragmentActivity activity = fragment.getActivity();
            ChromeCustomTabs.addChromeCustomTabsExtras(activity.getApplication(), intent);
            verifyZeroInteractions(intent);
        });
    }
}
