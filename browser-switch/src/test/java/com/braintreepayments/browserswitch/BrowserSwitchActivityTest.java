package com.braintreepayments.browserswitch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class BrowserSwitchActivityTest {

    private BrowserSwitchClient browserSwitchClient;
    private ActivityController<BrowserSwitchActivity> controller;

    @Before
    public void beforeEach() {
        browserSwitchClient = mock(BrowserSwitchClient.class);
        controller = Robolectric.buildActivity(BrowserSwitchActivity.class);
    }

    @Test
    public void onCreate_capturesBrowserSwitchResult() {
        BrowserSwitchActivity sut = controller.get();
        sut.browserSwitchClient = browserSwitchClient;

        controller.setup();
        verify(browserSwitchClient).captureResult(sut.getIntent(), sut);
    }

    @Test
    public void onCreate_callsFinish() {
        BrowserSwitchActivity sut = controller.get();
        sut.browserSwitchClient = browserSwitchClient;

        controller.setup();
        assertTrue(sut.isFinishing());
    }
}
