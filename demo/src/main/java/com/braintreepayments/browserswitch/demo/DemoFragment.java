package com.braintreepayments.browserswitch.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.braintreepayments.browserswitch.BrowserSwitchEvent;
import com.braintreepayments.browserswitch.BrowserSwitchFragment;
import com.braintreepayments.browserswitch.BrowserSwitchListener;
import com.braintreepayments.browserswitch.BrowserSwitchResult;

import androidx.annotation.Nullable;

public class DemoFragment extends BrowserSwitchFragment implements View.OnClickListener {

    private Button mBrowserSwitchButton;
    private TextView mResult;
    private TextView mReturnUrl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment, null);

        mBrowserSwitchButton = (Button) view.findViewById(R.id.browser_switch);
        mBrowserSwitchButton.setOnClickListener(this);

        mResult = (TextView) view.findViewById(R.id.result);
        mReturnUrl = (TextView) view.findViewById(R.id.return_url);

        return view;
    }

    @Override
    public void onClick(View v) {
        browserSwitch(1, "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" + getReturnUrlScheme()
                + "://");
    }

    @Override
    public void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri) {
        // TODO: remove when fragment code de-coupled
        mResult.setText("Result: " + result.name());
        mReturnUrl.setText("Return url: " + returnUri);
    }

    @Override
    public void onBrowserSwitchEvent(BrowserSwitchEvent event) {
        mResult.setText("Result: " + event.result.name());
        mReturnUrl.setText("Return url: " + event.returnUri);
    }
}
