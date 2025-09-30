package com.braintreepayments.api.browserswitch.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.api.BrowserSwitchClient;
import com.braintreepayments.api.demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button standardButton = findViewById(R.id.standard_button);
        standardButton.setOnClickListener(this::launchStandardBrowserSwitch);

        Button singleTopButton = findViewById(R.id.single_top_button);
        singleTopButton.setOnClickListener(this::launchSingleTopBrowserSwitch);

        // Show Auth Tab support status via Toast
        BrowserSwitchClient client = new BrowserSwitchClient();
        if (client.isAuthTabSupported(this)) {
            Toast.makeText(this, "Auth Tab is supported", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Using Custom Tabs fallback", Toast.LENGTH_LONG).show();
        }

        // Support Edge-to-Edge layout in Android 15
        // Ref: https://developer.android.com/develop/ui/views/layout/edge-to-edge#cutout-insets
        View navHostView = findViewById(R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(navHostView, (v, insets) -> {
            @WindowInsetsCompat.Type.InsetsType int insetTypeMask =
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.systemGestures();
            Insets bars = insets.getInsets(insetTypeMask);
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public void launchStandardBrowserSwitch(View view) {
        Intent intent = new Intent(this, ComposeActivity.class);
        startActivity(intent);
    }

    public void launchSingleTopBrowserSwitch(View view) {
        Intent intent = new Intent(this, DemoActivitySingleTop.class);
        startActivity(intent);
    }
}