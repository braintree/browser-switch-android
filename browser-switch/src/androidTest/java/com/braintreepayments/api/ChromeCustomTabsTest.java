package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;

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

    private FragmentScenario<Fragment> scenario;
    private Context applicationContext;

    @Before
    public void beforeEach() {
        scenario = FragmentScenario.launch(Fragment.class);
        applicationContext = ApplicationProvider.getApplicationContext();
    }

    @Test
    @SdkSuppress(maxSdkVersion = 23)
    public void isAvailable_whenCustomTabsAreNotSupported_returnsFalse() {
        scenario.onFragment(fragment -> assertFalse(ChromeCustomTabs.isAvailable(applicationContext)));
    }

    @Test
    @SdkSuppress(minSdkVersion = 24)
    public void isAvailable_whenCustomTabsAreSupported_returnsTrue() {
        scenario.onFragment(fragment -> assertTrue(ChromeCustomTabs.isAvailable(applicationContext)));
    }

    @Test
    @SdkSuppress(maxSdkVersion = 23)
    public void addChromeCustomTabsExtras_whenCustomTabsAreNotSupported_doesNotModifyTheIntent() {
        Intent intent = mock(Intent.class);
        scenario.onFragment(fragment -> {
            ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);
            verifyZeroInteractions(intent);
        });
    }

    @Test
    @SdkSuppress(minSdkVersion = 24)
    public void addChromeCustomTabsExtras_whenCustomTabsAreSupported_modifiesTheIntent() {
        Intent intent = mock(Intent.class);
        scenario.onFragment(fragment -> {
            ChromeCustomTabs.addChromeCustomTabsExtras(applicationContext, intent);

            verify(intent).putExtras(any(Bundle.class));
            verify(intent).addFlags(anyInt());
        });
    }
}
