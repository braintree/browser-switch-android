package com.braintreepayments.api.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.braintreepayments.api.BrowserSwitchException;
import com.braintreepayments.api.BrowserSwitchListener;
import com.braintreepayments.api.BrowserSwitchOptions;
import com.braintreepayments.api.BrowserSwitchResult;
import com.braintreepayments.api.BrowserSwitchStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoFragment extends Fragment implements View.OnClickListener, BrowserSwitchListener {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    private TextView mBrowserSwitchStatusTextView;
    private TextView mSelectedColorTextView;
    private TextView mMetadataTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment, container, false);

        Button startBrowserSwitchButton = view.findViewById(R.id.browser_switch);
        startBrowserSwitchButton.setOnClickListener(this);

        Button startBrowserSwitchWithMetadataButton =
            view.findViewById(R.id.browser_switch_with_metadata);
        startBrowserSwitchWithMetadataButton.setOnClickListener(this);

        mBrowserSwitchStatusTextView = view.findViewById(R.id.result);
        mSelectedColorTextView = view.findViewById(R.id.return_url);
        mMetadataTextView = view.findViewById(R.id.metadata);

        return view;
    }

    @Override
    public void onClick(View v) {
        @IdRes int viewId = v.getId();
        if (viewId == R.id.browser_switch) {
            startBrowserSwitch(null);
        } else if (viewId == R.id.browser_switch_with_metadata) {
            JSONObject metadata = buildMetadataObject();
            startBrowserSwitch(metadata);
        }
    }

    private void startBrowserSwitch(@Nullable JSONObject metadata) {
        Uri url = buildBrowserSwitchUrl();
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .metadata(metadata)
                .requestCode(1)
                .url(url)
                .returnUrlScheme(getReturnUrlScheme());

        try {
            getDemoActivity().startBrowserSwitch(browserSwitchOptions);
            clearTextViews();
        } catch (BrowserSwitchException e) {
            String statusText = "Browser Switch Error: " + e.getMessage();
            mBrowserSwitchStatusTextView.setText(statusText);
            e.printStackTrace();
        }
    }

    private Uri buildBrowserSwitchUrl() {
        String url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" +
                getReturnUrlScheme() + "://";
        return Uri.parse(url);
    }

    private JSONObject buildMetadataObject() {
        try {
            return new JSONObject().put(DemoFragment.TEST_KEY, DemoFragment.TEST_VALUE);
        } catch (JSONException ignore) {
            // do nothing
        }
        return null;
    }

    private void clearTextViews() {
        mBrowserSwitchStatusTextView.setText("");
        mSelectedColorTextView.setText("");
        mMetadataTextView.setText("");
    }

    public void onBrowserSwitchResult(BrowserSwitchResult result) {
        String resultText = null;
        String selectedColorText = "";

        int statusCode = result.getStatus();
        switch (statusCode) {
            case BrowserSwitchStatus.SUCCESS:
                resultText = "Browser Switch Successful";

                Uri returnUrl = result.getDeepLinkUrl();
                if (returnUrl != null) {
                    String color = returnUrl.getQueryParameter("color");
                    selectedColorText = String.format("Selected color: %s", color);
                }
                break;
            case BrowserSwitchStatus.CANCELED:
                resultText = "Browser Switch Cancelled by User";
                break;
        }
        mBrowserSwitchStatusTextView.setText(resultText);
        mSelectedColorTextView.setText(selectedColorText);

        String metadataOutput = null;
        JSONObject requestMetadata = result.getRequestMetadata();
        if (requestMetadata != null) {
            try {
                String metadataValue = result.getRequestMetadata().getString(TEST_KEY);
                metadataOutput = String.format("%s=%s", TEST_KEY, metadataValue);
            } catch (JSONException ignore) {
                // do nothing
            }
        }
        mMetadataTextView.setText(String.format("Metadata: %s", metadataOutput));
    }

    private DemoActivity getDemoActivity() {
        return (DemoActivity) getActivity();
    }

    private String getReturnUrlScheme() {
        return getDemoActivity().getReturnUrlScheme();
    }
}
