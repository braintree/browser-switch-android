package com.braintreepayments.api;

import static androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchInspectorUnitTest extends TestCase {

    private Context context;

    private PackageManager packageManager;

    @Before
    public void setUp() {
        context = mock(Context.class);
        packageManager = mock(PackageManager.class);
    }

    @Test
    public void isDeviceConfiguredForDeepLinking_queriesPackageManagerForDeepIntent() {
        when(context.getPackageName()).thenReturn("sample.package.name");
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.emptyList());

        String returnUrlScheme = "sample.package.name.browserswitch";

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        sut.isDeviceConfiguredForDeepLinking(context, returnUrlScheme);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(packageManager).queryIntentActivities(captor.capture(), eq(0));

        Intent intent = captor.getValue();
        assertEquals(Uri.parse("sample.package.name.browserswitch://"), intent.getData());
        assertEquals(Intent.ACTION_VIEW, intent.getAction());
        assertTrue(intent.hasCategory(Intent.CATEGORY_DEFAULT));
        assertTrue(intent.hasCategory(Intent.CATEGORY_BROWSABLE));
    }

    @Test
    public void isDeviceConfiguredForDeepLinking_whenNoActivityFound_returnsFalse() {
        when(context.getPackageName()).thenReturn("sample.package.name");
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.emptyList());

        String returnUrlScheme = "sample.package.name.browserswitch";

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertFalse(sut.isDeviceConfiguredForDeepLinking(context, returnUrlScheme));
    }

    @Test
    public void isDeviceConfiguredForDeepLinking_whenActivityFound_returnsTrue() {
        when(context.getPackageName()).thenReturn("sample.package.name");
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo resolveInfo = new ResolveInfo();
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.singletonList(resolveInfo));

        String returnUrlScheme = "sample.package.name.browserswitch";

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertTrue(sut.isDeviceConfiguredForDeepLinking(context, returnUrlScheme));
    }

    @Test
    public void deviceHasBrowser_queriesPackageManagerForIntentForBrowserSwitching() {
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.emptyList());

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        sut.deviceHasBrowser(context);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
        verify(packageManager).queryIntentActivities(captor.capture(), eq(0));

        Intent intent = captor.getValue();
        assertEquals(Intent.ACTION_VIEW, intent.getAction());
        assertEquals(Uri.parse("https://"), intent.getData());
    }

    @Test
    public void deviceHasBrowser_whenNoActivityFound_returnsFalse() {
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.emptyList());

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertFalse(sut.deviceHasBrowser(context));
    }

    @Test
    public void deviceHasBrowser_whenActivityFound_returnsTrue() {
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo resolveInfo = new ResolveInfo();
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.singletonList(resolveInfo));

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertTrue(sut.deviceHasBrowser(context));
    }

    @Test
    public void deviceHasChromeCustomTabs_queriesForBrowserCapableActivity() {
        when(context.getPackageManager()).thenReturn(packageManager);

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        sut.deviceHasChromeCustomTabs(context);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(packageManager).queryIntentActivities(intentCaptor.capture(), eq(0));

        Intent activityIntent = intentCaptor.getValue();
        assertEquals(Intent.ACTION_VIEW, activityIntent.getAction());
        assertTrue(activityIntent.hasCategory(Intent.CATEGORY_BROWSABLE));
        assertEquals(Uri.parse("https://"), activityIntent.getData());
    }

    @Test
    public void deviceHasChromeCustomTabs_checksIfBrowserCapableActivityCanResolveChromeCustomTabsService() {
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo browserInfo = new ResolveInfo();
        browserInfo.activityInfo = new ActivityInfo();
        browserInfo.activityInfo.packageName = "sample.package.name";
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.singletonList(browserInfo));

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        sut.deviceHasChromeCustomTabs(context);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(packageManager).resolveService(intentCaptor.capture(), eq(0));

        Intent serviceIntent = intentCaptor.getValue();
        assertEquals(ACTION_CUSTOM_TABS_CONNECTION, serviceIntent.getAction());
        assertEquals("sample.package.name", serviceIntent.getPackage());
    }

    @Test
    public void deviceHasChromeCustomTabs_whenChromeCustomTabsServiceIsResolvable_returnsTrue() {
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo browserInfo = new ResolveInfo();
        browserInfo.activityInfo = new ActivityInfo();
        browserInfo.activityInfo.packageName = "sample.package.name";
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.singletonList(browserInfo));

        Intent serviceIntent = new Intent();
        serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
        serviceIntent.setPackage("sample.package.name");

        ResolveInfo serviceInfo = new ResolveInfo();
        when(packageManager.resolveService(any(Intent.class), eq(0))).thenReturn(serviceInfo);

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertTrue(sut.deviceHasChromeCustomTabs(context));
    }

    @Test
    public void deviceHasChromeCustomTabs_whenChromeCustomTabsServiceIsNotResolvable_returnsFalse() {
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo browserInfo = new ResolveInfo();
        browserInfo.activityInfo = new ActivityInfo();
        browserInfo.activityInfo.packageName = "sample.package.name";
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.singletonList(browserInfo));
        when(packageManager.resolveService(any(Intent.class), eq(0))).thenReturn(null);

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertFalse(sut.deviceHasChromeCustomTabs(context));
    }

    @Test
    public void deviceHasChromeCustomTabs_whenNoBrowserFound_returnsFalse() {
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(Collections.emptyList());

        BrowserSwitchInspector sut = new BrowserSwitchInspector();
        assertFalse(sut.deviceHasChromeCustomTabs(context));
    }
}