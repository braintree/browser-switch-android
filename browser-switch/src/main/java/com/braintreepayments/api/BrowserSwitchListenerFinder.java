package com.braintreepayments.api;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Locate all {@link BrowserSwitchListener} references associated with an activity. A listener can
 * be the root activity, along with any fragments attached to that activity, and any child,
 * grand-child, great-grand-child etc. fragments that can be traced back to the root activity.
 */
class BrowserSwitchListenerFinder {

    List<BrowserSwitchListener> findActiveListeners(FragmentActivity activity) {
        List<BrowserSwitchListener> listeners = new ArrayList<>();
        if (activity instanceof BrowserSwitchListener) {
            listeners.add((BrowserSwitchListener) activity);
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof BrowserSwitchListener) {
                listeners.add((BrowserSwitchListener) fragment);
            }

            listeners.addAll(findChildFragmentActiveListeners(fragment));
        }
        return listeners;
    }

    private List<BrowserSwitchListener> findChildFragmentActiveListeners(Fragment fragment) {
        List<BrowserSwitchListener> listeners = new ArrayList<>();

        FragmentManager childFragmentManager = fragment.getChildFragmentManager();
        List<Fragment> childFragments = childFragmentManager.getFragments();
        for (Fragment childFragment : childFragments) {
            if (childFragment instanceof BrowserSwitchListener) {
                listeners.add((BrowserSwitchListener) childFragment);
            }

            // recursively find additional child fragments until no descendant listener
            // fragments exist
            listeners.addAll(findChildFragmentActiveListeners(childFragment));
        }
        return listeners;
    }
}
