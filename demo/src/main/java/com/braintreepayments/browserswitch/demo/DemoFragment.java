package com.braintreepayments.browserswitch.demo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.braintreepayments.browserswitch.BrowserSwitchFragment;
import com.braintreepayments.browserswitch.ChromeCustomTabs;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class DemoFragment extends BrowserSwitchFragment implements View.OnClickListener {

    private Context mContext;
    private Button mBrowserSwitchButton;
    private Button mCustomStyleSwitchButton;
    private TextView mResult;
    private TextView mReturnUrl;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment, null);

        mBrowserSwitchButton = (Button) view.findViewById(R.id.browser_switch);
        mCustomStyleSwitchButton = (Button) view.findViewById(R.id.style_browser_switch);
        mBrowserSwitchButton.setOnClickListener(this);
        mCustomStyleSwitchButton.setOnClickListener(this);

        mResult = (TextView) view.findViewById(R.id.result);
        mReturnUrl = (TextView) view.findViewById(R.id.return_url);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBrowserSwitchButton.getId()) {
            browserSwitch(1, "https://braintree.github.io/popup-bridge-example/" +
                    "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" + getReturnUrlScheme()
                    + "://");
        } else if (v.getId() == mCustomStyleSwitchButton.getId()) {
            Uri returnUri = Uri.parse("https://braintree.github.io/popup-bridge-example/" +
                    "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" + getReturnUrlScheme()
                    + "://");
            Intent browserIntent =
                    new Intent(Intent.ACTION_VIEW, returnUri)
                            .addFlags(FLAG_ACTIVITY_NEW_TASK);
            ChromeCustomTabs.addChromeCustomTabsExtras(mContext, browserIntent);
            Bundle startAnimationBundle = ActivityOptionsCompat.makeCustomAnimation(
                    mContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle();
            browserSwitch(1, browserIntent, startAnimationBundle);
        }


    }

    @Override
    public void onBrowserSwitchResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri) {
        mResult.setText("Result: " + result.name());
        mReturnUrl.setText("Return url: " + returnUri);
    }
}
