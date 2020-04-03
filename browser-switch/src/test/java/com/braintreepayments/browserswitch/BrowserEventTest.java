package com.braintreepayments.browserswitch;

import android.net.Uri;

import com.braintreepayments.browserswitch.db.PendingRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Uri.class })
public class BrowserEventTest {

    private Uri uri;
    private PendingRequest pendingRequest;

    @Before
    public void beforeEach() {
        mockStatic(Uri.class);

        uri = mock(Uri.class);
        pendingRequest = mock(PendingRequest.class);
    }

    @Test
    public void fromPendingRequest() {
        when(pendingRequest.getRequestCode()).thenReturn(123);
        when(pendingRequest.getUrl()).thenReturn("https://example.com");
        when(Uri.parse("https://example.com")).thenReturn(uri);

        BrowserSwitchEvent sut = BrowserSwitchEvent.from(pendingRequest, BrowserSwitchResult.OK);
        assertEquals(sut.result, BrowserSwitchResult.OK);
        assertEquals(sut.requestCode, 123);
        assertEquals(sut.returnUri, uri);
    }
}
