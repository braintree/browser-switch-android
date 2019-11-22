package com.braintreepayments.browserswitch.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class DemoActivityTest {
    private static final long APP_LAUNCH_TIMEOUT_MS = 5000;

    @Test
    public void testDemo() throws UiObjectNotFoundException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        Context context = ApplicationProvider.getApplicationContext();

        // Launch our app using UIAutomator instead of Espresso.
        // If we use Espresso, our activity is killed when the browser opens.
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        context.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(BuildConfig.APPLICATION_ID).depth(0)), APP_LAUNCH_TIMEOUT_MS);

        performBrowserSwitch(device, "Red", "red");
        performBrowserSwitch(device, "Green", "green");
        performBrowserSwitch(device, "Blue", "blue");
        performBrowserSwitch(device, "I don't like any of these colors", "undefined");
    }

    private void performBrowserSwitch(UiDevice device,
                                      String colorActionLabel,
                                      String expectedSelectedColor) throws UiObjectNotFoundException {
        Application application = ApplicationProvider.getApplicationContext();
        device.findObject(By.res(BuildConfig.APPLICATION_ID, "browser_switch")).click();
        UiSelector selector = new UiSelector();
        device.findObject(selector.text(colorActionLabel)).click();
        assertTrue(device.findObject(selector.text(application.getString(R.string.browser_switch_success_text))).exists());
        assertTrue(device.findObject(selector.text(application.getString(R.string.browser_switch_selected_color_text, expectedSelectedColor))).exists());
    }

}
