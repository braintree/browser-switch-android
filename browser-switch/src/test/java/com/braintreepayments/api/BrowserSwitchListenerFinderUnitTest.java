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
import java.util.Collections;
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
    private FragmentActivity activity;

    private FragmentManager fragmentManager;
    private FragmentManager childFragmentManager;

    private Fragment fragment;
    private ListenerFragment listenerFragment;

    @Before
    public void beforeEach() {
        fragmentManager = mock(FragmentManager.class);
        childFragmentManager = mock(FragmentManager.class);

        activity = mock(FragmentActivity.class);
        listenerActivity = mock(ListenerActivity.class);

        fragment = mock(Fragment.class);
        listenerFragment = mock(ListenerFragment.class);

        when(activity.getSupportFragmentManager()).thenReturn(fragmentManager);
        when(listenerActivity.getSupportFragmentManager()).thenReturn(fragmentManager);

        when(fragment.getChildFragmentManager()).thenReturn(childFragmentManager);
        when(listenerFragment.getChildFragmentManager()).thenReturn(childFragmentManager);
    }

    @Test
    public void findActiveListeners_whenActivityIsNotAListener_excludesActivityInTheResult() {
        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(activity);

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
    public void findActiveListeners_whenNonListenerActivityFragmentMangerHasListenerFragments_includesFragmentsInResult() {
        when(fragmentManager.getFragments()).thenReturn(Arrays.asList(fragment, listenerFragment));

        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(activity);

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

    @Test
    public void findActiveListeners_whenNonListenerActivityHasGrandchildListenerFragments_includesFragmentsInResult() {
        when(fragmentManager.getFragments()).thenReturn(Collections.singletonList(fragment));
        when(childFragmentManager.getFragments())
                .thenReturn(Collections.singletonList(listenerFragment))
                .thenReturn(Collections.emptyList());

        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(activity);

        assertEquals(1, activeListeners.size());
        assertSame(activeListeners.get(0), listenerFragment);
    }

    @Test
    public void findActiveListeners_whenListenerActivityHasGrandchildListenerFragments_includesFragmentsInResult() {
        when(fragmentManager.getFragments()).thenReturn(Collections.singletonList(fragment));
        when(childFragmentManager.getFragments())
                .thenReturn(Collections.singletonList(listenerFragment))
                .thenReturn(Collections.emptyList());

        BrowserSwitchListenerFinder sut = new BrowserSwitchListenerFinder();
        List<BrowserSwitchListener> activeListeners = sut.findActiveListeners(listenerActivity);

        assertEquals(2, activeListeners.size());
        assertSame(activeListeners.get(0), listenerActivity);
        assertSame(activeListeners.get(1), listenerFragment);
    }
}
