package com.braintreepayments.api;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityFinderTest {

    private Intent intent;
    private Context context;

    private PackageManager packageManager;

    @Before
    public void setUp() {
        intent = mock(Intent.class);
        context = mock(Context.class);

        packageManager = mock(PackageManager.class);
    }

    @Test
    public void canResolveActivityForIntent_whenNoActivityFound_returnsFalse() {
        when(context.getPackageManager()).thenReturn(packageManager);
        when(packageManager.queryIntentActivities(intent, 0)).thenReturn(Collections.emptyList());

        ActivityFinder sut = ActivityFinder.newInstance();
        assertFalse(sut.canResolveActivityForIntent(context, intent));
    }

    @Test
    public void canResolveActivityForIntent_whenActivityFound_returnsTrue() {
        when(context.getPackageManager()).thenReturn(packageManager);

        ResolveInfo resolveInfo = mock(ResolveInfo.class);
        when(packageManager.queryIntentActivities(intent, 0)).thenReturn(Collections.singletonList(resolveInfo));

        ActivityFinder sut = ActivityFinder.newInstance();
        assertTrue(sut.canResolveActivityForIntent(context, intent));
    }
}
