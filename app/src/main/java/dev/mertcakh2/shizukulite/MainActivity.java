package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** A small, transparent setup companion; it does not create an elevated shell. */
public final class MainActivity extends Activity {
    private static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";
    private static final String SETUP_URL = "https://shizuku.rikka.app/guide/setup/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(24);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(pad, dp(36), pad, pad);
        page.setBackgroundColor(0xFFF5F6FA);

        TextView title = text("Shizuku Lite", 30, 0xFF17233C, true);
        page.addView(title);
        TextView subtitle = text("A lightweight starting point for user-authorized Android tools.", 16, 0xFF526079, false);
        LinearLayout.LayoutParams subtitleParams = params(-1, -2);
        subtitleParams.topMargin = dp(8);
        page.addView(subtitle, subtitleParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cardParams = params(-1, -2);
        cardParams.topMargin = dp(28);
        page.addView(card, cardParams);

        card.addView(text("Service status", 19, 0xFF17233C, true));
        TextView status = text("Not connected\nThis starter app does not run commands or start a privileged service.", 15, 0xFF526079, false);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(10);
        card.addView(status, statusParams);

        TextView explanation = text(
                "Android apps cannot turn their own process into the ADB shell. For shell-level access, the device owner must explicitly start a trusted service using Android's supported debugging workflow. This app currently provides setup links only.",
                15, 0xFF26334B, false);
        LinearLayout.LayoutParams explanationParams = params(-1, -2);
        explanationParams.topMargin = dp(24);
        page.addView(explanation, explanationParams);

        Button openShizuku = new Button(this);
        openShizuku.setText("Open Shizuku");
        openShizuku.setOnClickListener(v -> openShizuku());
        LinearLayout.LayoutParams buttonParams = params(-1, -2);
        buttonParams.topMargin = dp(24);
        page.addView(openShizuku, buttonParams);

        Button setupGuide = new Button(this);
        setupGuide.setText("Read the setup guide");
        setupGuide.setOnClickListener(v -> openUrl(SETUP_URL));
        page.addView(setupGuide, params(-1, -2));

        TextView footer = text("No root access, shell commands, or background service are requested by this build.", 13, 0xFF68758A, false);
        LinearLayout.LayoutParams footerParams = params(-1, -2);
        footerParams.topMargin = dp(20);
        page.addView(footer, footerParams);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        setContentView(scroll);
    }

    private void openShizuku() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(SHIZUKU_PACKAGE);
        if (launch == null) {
            Toast.makeText(this, "Shizuku is not installed. Open the setup guide to learn more.", Toast.LENGTH_LONG).show();
            openUrl(SETUP_URL);
            return;
        }
        try {
            startActivity(launch);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Could not open Shizuku.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No browser is available.", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setGravity(Gravity.START);
        return view;
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
