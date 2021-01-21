package com.braintreepayments.api.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.braintreepayments.api.BrowserSwitchClient;
import com.braintreepayments.api.BrowserSwitchException;
import com.braintreepayments.api.BrowserSwitchCallback;
import com.braintreepayments.api.BrowserSwitchOptions;
import com.braintreepayments.api.BrowserSwitchResult;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoFragment extends Fragment implements View.OnClickListener, BrowserSwitchCallback {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    @VisibleForTesting
    BrowserSwitchClient browserSwitchClient = null;

    private TextView mBrowserSwitchStatusTextView;
    private TextView mSelectedColorTextView;
    private TextView mMetadataTextView;

    String returnUrlScheme;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String packageName = getActivity().getApplicationContext().getPackageName();
        returnUrlScheme =
            packageName.toLowerCase().replace("_", "") + ".browserswitch";

        browserSwitchClient = new BrowserSwitchClient();
    }

    @Override
    public void onResume() {
        super.onResume();
        browserSwitchClient.captureResult(getActivity(), getActivity().getIntent());
        browserSwitchClient.deliverResult(getActivity(), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.browser_switch:
                startBrowserSwitch();
                break;
            case R.id.browser_switch_with_metadata:
                JSONObject metadata = buildMetadataObject(TEST_KEY, TEST_VALUE);
                startBrowserSwitchWithMetadata(metadata);
                break;
        }
    }

    private void startBrowserSwitch() {
        Uri url = buildBrowserSwitchUrl();
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .requestCode(1)
                .url(url)
                .returnUrlScheme(returnUrlScheme);
        try {
            browserSwitchClient.start(getActivity(), browserSwitchOptions);
        } catch (BrowserSwitchException e) {
            String statusText = "Browser Switch Error: " + e.getMessage();
            mBrowserSwitchStatusTextView.setText(statusText);
            e.printStackTrace();
        }
    }

    private void startBrowserSwitchWithMetadata(JSONObject metadata) {
        Uri url = buildBrowserSwitchUrl();
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .metadata(metadata)
                .requestCode(1)
                .url(url)
                .returnUrlScheme(returnUrlScheme);
        try {
            browserSwitchClient.start(getActivity(), browserSwitchOptions);
        } catch (BrowserSwitchException e) {
            e.printStackTrace();
        }
    }

    private Uri buildBrowserSwitchUrl() {
        String url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?popupBridgeReturnUrlPrefix=" +
                returnUrlScheme +
                "://";
        return Uri.parse(url);
    }

    private JSONObject buildMetadataObject(String key, String value) {
        try {
            return new JSONObject().put(key, value);
        } catch (JSONException ignore) {
            // do nothing
        }
        return null;
    }

    @Override
    public void onResult(int requestCode, BrowserSwitchResult result, @Nullable Uri returnUri) {
        String resultText = null;
        String selectedColorText = "";

        int statusCode = result.getStatus();
        switch (statusCode) {
            case BrowserSwitchResult.STATUS_OK:
                resultText = "Browser Switch Successful";

                if (returnUri != null) {
                    String color = returnUri.getQueryParameter("color");
                    selectedColorText = String.format("Selected color: %s", color);
                }
                break;
            case BrowserSwitchResult.STATUS_CANCELED:
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
}
