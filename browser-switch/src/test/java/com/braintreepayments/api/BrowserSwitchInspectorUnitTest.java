package com.braintreepayments.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.Intent;
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
}