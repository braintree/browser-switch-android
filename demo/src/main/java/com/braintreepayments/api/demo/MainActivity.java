package com.braintreepayments.api.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button standardButton = findViewById(R.id.standard_button);
        standardButton.setOnClickListener(this::launchStandardBrowserSwitch);

        Button singleTopButton = findViewById(R.id.single_top_button);
        singleTopButton.setOnClickListener(this::launchSingleTopBrowserSwitch);

        Button launcherButton = findViewById(R.id.launcher_button);
        launcherButton.setOnClickListener(this::launchBrowserSwitchLauncher);

        Button launcherWithoutButton = findViewById(R.id.launcher_without_button);
        launcherWithoutButton.setOnClickListener(this::launchBrowserSwitchLauncherWithoutButton);
    }

    public void launchStandardBrowserSwitch(View view) {
        Intent intent = new Intent(this, DemoActivity.class);
        startActivity(intent);
    }

    public void launchSingleTopBrowserSwitch(View view) {
        Intent intent = new Intent(this, DemoActivitySingleTop.class);
        startActivity(intent);
    }

    public void launchBrowserSwitchLauncher(View view) {
        Intent intent = new Intent(this, LauncherActivity.class);
        startActivity(intent);
    }

    public void launchBrowserSwitchLauncherWithoutButton(View view) {
        Intent intent = new Intent(this, LauncherActivityNoButton.class);
        startActivity(intent);
    }
}