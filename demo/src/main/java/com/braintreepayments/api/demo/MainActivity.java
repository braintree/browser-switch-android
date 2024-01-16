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