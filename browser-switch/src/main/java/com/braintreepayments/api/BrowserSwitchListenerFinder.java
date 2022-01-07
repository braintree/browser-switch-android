package com.braintreepayments.api;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

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

            FragmentManager childFragmentManager = fragment.getChildFragmentManager();
            List<Fragment> childFragments = childFragmentManager.getFragments();
            for (Fragment childFragment : childFragments) {
                if (childFragment instanceof BrowserSwitchListener) {
                    listeners.add((BrowserSwitchListener) childFragment);
                }
            }
        }
        return listeners;
    }
}
