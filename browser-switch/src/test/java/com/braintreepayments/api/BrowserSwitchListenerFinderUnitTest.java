package com.braintreepayments.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class BrowserSwitchListenerFinderUnitTest {

    private static class ListenerActivity extends FragmentActivity implements BrowserSwitchListener {
        @Override
        public void onBrowserSwitchResult(BrowserSwitchResult result) {
        }
    }

    private static class ListenerFragment extends Fragment implements BrowserSwitchListener {
        @Override
        public void onBrowserSwitchResult(BrowserSwitchResult result) {
        }
    }

    private ListenerActivity listenerActivity;
    private FragmentActivity nonListenerActivity;

    private FragmentManager fragmentManager;

    private Fragment fragment;
    private ListenerFragment listenerFragment;

    @Before
    public void beforeEach() {
        fragmentManager = mock(FragmentManager.class);

        nonListenerActivity = mock(FragmentActivity.class);
        listenerActivity = mock(ListenerActivity.class);

        fragment = mock(Fragment.class);
        listenerFragment = mock(ListenerFragment.class);

        when(nonListenerActivity.getSupportFragmentManager()).thenReturn(fragmentManager);
        when(listenerActivity.getSupportFragmentManager()).thenReturn(fragmentManager);
    }

    @Test
    public void findActiveListeners_whenActivityIsNotAListener_excludesActivityInTheResult() {
        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(nonListenerActivity);

        assertEquals(0, activeListeners.size());
    }

    @Test
    public void findActiveListeners_whenActivityIsAListener_includesActivityInTheResult() {
        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(listenerActivity);

        assertEquals(1, activeListeners.size());
        assertSame(activeListeners.get(0), listenerActivity);
    }

    @Test
    public void findActiveListeners_whenPlainActivityFragmentMangerHasListenerFragments_includesFragmentsInResult() {
        when(fragmentManager.getFragments()).thenReturn(Arrays.asList(fragment, listenerFragment));

        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(nonListenerActivity);

        assertEquals(1, activeListeners.size());
        assertSame(activeListeners.get(0), listenerFragment);
    }

    @Test
    public void findActiveListeners_whenListenerActivityFragmentMangerHasListenerFragments_includesFragmentsInResult() {
        when(fragmentManager.getFragments()).thenReturn(Arrays.asList(listenerFragment, fragment));

        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(listenerActivity);

        assertEquals(2, activeListeners.size());
        assertSame(activeListeners.get(0), listenerActivity);
        assertSame(activeListeners.get(1), listenerFragment);
    }
}