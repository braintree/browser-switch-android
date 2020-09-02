package com.braintreepayments.browserswitch.demo;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.uiautomator.Until.hasObject;
import static com.lukekorth.deviceautomator.AutomatorAction.click;
import static com.lukekorth.deviceautomator.DeviceAutomator.onDevice;
import static com.lukekorth.deviceautomator.UiObjectMatcher.withText;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4ClassRunner.class)
public class BrowserSwitchTest {

    @Before
    public void beforeEach() {
        // TODO: update device automator to use ApplicationProvider and new androidx testing libraries
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice device = UiDevice.getInstance(instrumentation);
        device.pressHome();

        String launcherPackage = device.getLauncherPackageName();
        device.wait(hasObject(By.pkg(launcherPackage).depth(0)), 5000);

        String packageName = InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName();

        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);

        device.wait(hasObject(By.pkg(intent.getPackage()).depth(0)), 5000);
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
