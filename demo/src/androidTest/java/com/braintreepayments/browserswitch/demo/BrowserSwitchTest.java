package com.braintreepayments.browserswitch.demo;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class BrowserSwitchTest {

    @Before
    public void beforeEach() {
        onDevice().onHomeScreen().launchApp("com.braintreepayments.browserswitch.demo");
    }

    @Test(timeout = 60000)
    public void startWithoutMetadata() {
        onDevice(withText("Start Browser Switch")).perform(click());
        onDevice(withText("Red")).waitForExists().perform(click());

        onDevice(withText("Browser Switch Successful")).waitForExists();

        assertTrue(onDevice(withText("Browser Switch Successful")).exists());
        assertTrue(onDevice(withText("Selected Color: red")).exists());
        assertTrue(onDevice(withText("Metadata: null")).exists());
    }

    @Test(timeout = 60000)
    public void startWithMetadata() {
        onDevice(withText("Start Browser Switch With Metadata")).perform(click());
        onDevice(withText("Red")).waitForExists().perform(click());

        onDevice(withText("Browser Switch Successful")).waitForExists();

        assertTrue(onDevice(withText("Browser Switch Successful")).exists());
        assertTrue(onDevice(withText("Selected Color: red")).exists());
        assertTrue(onDevice(withText("Metadata: testKey=testValue")).exists());
    }
}
