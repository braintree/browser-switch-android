package com.braintreepayments.api.browserswitch.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.braintreepayments.api.BrowserSwitchFinalResult;
import com.braintreepayments.api.BrowserSwitchException;
import com.braintreepayments.api.BrowserSwitchOptions;
import com.braintreepayments.api.demo.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DemoFragment extends Fragment implements View.OnClickListener {

    private static final String TEST_KEY = "testKey";
    private static final String TEST_VALUE = "testValue";

    private Spinner mLinkSpinner;

    private TextView mBrowserSwitchStatusTextView;
    private TextView mSelectedColorTextView;
    private TextView mMetadataTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.demo_fragment, container, false);

        mLinkSpinner = view.findViewById(R.id.link_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.navigation_links,
                R.layout.support_simple_spinner_dropdown_item
        );
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mLinkSpinner.setAdapter(adapter);

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
        boolean isAppLink = mLinkSpinner.getSelectedItem().equals("App Link");
        Uri url = buildBrowserSwitchUrl(isAppLink);
        BrowserSwitchOptions browserSwitchOptions = new BrowserSwitchOptions()
                .metadata(metadata)
                .requestCode(1)
                .url(url)
                .launchAsNewTask(false);

        if (isAppLink) {
            browserSwitchOptions.appLinkUri(
                    Uri.parse("https://mobile-sdk-demo-site-838cead5d3ab.herokuapp.com")
            );
        } else {
            browserSwitchOptions.returnUrlScheme(getReturnUrlScheme());
        }

        try {
            getDemoActivity().startBrowserSwitch(browserSwitchOptions);
            clearTextViews();
        } catch (BrowserSwitchException e) {
            onBrowserSwitchError(e);
            e.printStackTrace();
        }
    }

    private Uri buildBrowserSwitchUrl(boolean isAppLink) {
        String url = "https://braintree.github.io/popup-bridge-example/" +
                "this_launches_in_popup.html?";
        if (isAppLink) {
            url += "isAppLink=true";
        } else {
            url += "popupBridgeReturnUrlPrefix=" + getReturnUrlScheme() + "://";
        }
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

    public void onBrowserSwitchResult(BrowserSwitchFinalResult.Success result) {
        String selectedColorText = "";

        String resultText = "Browser Switch Successful";

        Uri returnUrl = result.getDeepLinkUrl();
        String color = returnUrl.getQueryParameter("color");
        selectedColorText = String.format("Selected color: %s", color);

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

    void onBrowserSwitchError(Exception error) {
        String statusText = "Browser Switch Error: " + error.getMessage();
        mBrowserSwitchStatusTextView.setText(statusText);
    }

    private DemoActivitySingleTop getDemoActivity() {
        return (DemoActivitySingleTop) getActivity();
    }

    private String getReturnUrlScheme() {
        return getDemoActivity().getReturnUrlScheme();
    }
}
