package com.braintreepayments.api;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

class BrowserSwitchListenerFinder {

    List<BrowserSwitchListener> findActiveListeners(FragmentActivity activity) {
        List<BrowserSwitchListener> result = new ArrayList<>();
        if (activity instanceof BrowserSwitchListener) {
            result.add((BrowserSwitchListener) activity);
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof BrowserSwitchListener) {
                result.add((BrowserSwitchListener) fragment);
            }
        }
        return result;
    }
}
