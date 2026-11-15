package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import rikka.shizuku.Shizuku;

/** Lightweight dashboard and permission-aware client for the official Shizuku service. */
public final class MainActivity extends Activity {
    private static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";
    private static final String SETUP_URL = "https://shizuku.rikka.app/guide/setup/";
    private static final String DOWNLOAD_URL = "https://shizuku.rikka.app/download/";
    private static final int REQUEST_CODE = 1001;

    private TextView connectionValue;
    private TextView permissionValue;
    private TextView detailsValue;
    private Button permissionButton;

    private final Shizuku.OnBinderReceivedListener binderReceivedListener = this::refreshStatus;
    private final Shizuku.OnBinderDeadListener binderDeadListener = this::refreshStatus;
    private final Shizuku.OnRequestPermissionResultListener permissionResultListener = (requestCode, result) -> {
        if (requestCode == REQUEST_CODE) {
            refreshStatus();
            Toast.makeText(this, result == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ? "Shizuku access granted" : "Shizuku access was not granted", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(246, 247, 251));

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(28));
        scroll.addView(page);

        TextView eyebrow = text("SHIZUKU CLIENT", 12, 0xFF5266D8, true);
        page.addView(eyebrow);
        TextView title = text("Shizuku Lite", 32, 0xFF17213A, true);
        LinearLayout.LayoutParams titleParams = params(-1, -2);
        titleParams.topMargin = dp(5);
        page.addView(title, titleParams);
        TextView intro = text("A clean, lightweight dashboard for connecting to Shizuku.", 15, 0xFF69748A, false);
        LinearLayout.LayoutParams introParams = params(-1, -2);
        introParams.topMargin = dp(7);
        page.addView(intro, introParams);

        LinearLayout statusCard = card();
        LinearLayout.LayoutParams cardParams = params(-1, -2);
        cardParams.topMargin = dp(24);
        page.addView(statusCard, cardParams);
        statusCard.addView(text("CONNECTION", 11, 0xFF758098, true));
        connectionValue = text("Checking…", 22, 0xFF17213A, true);
        addTop(statusCard, connectionValue, 8);
        detailsValue = text("Looking for the Shizuku service on this device.", 14, 0xFF69748A, false);
        addTop(statusCard, detailsValue, 6);

        LinearLayout accessCard = card();
        LinearLayout.LayoutParams accessParams = params(-1, -2);
        accessParams.topMargin = dp(14);
        page.addView(accessCard, accessParams);
        accessCard.addView(text("APP ACCESS", 11, 0xFF758098, true));
        permissionValue = text("Checking permission…", 18, 0xFF17213A, true);
        addTop(accessCard, permissionValue, 8);
        TextView permissionHint = text("Permission is requested from Shizuku and remains under your control.", 14, 0xFF69748A, false);
        addTop(accessCard, permissionHint, 6);
        permissionButton = button("Grant Shizuku access", true);
        permissionButton.setOnClickListener(v -> requestShizukuPermission());
        addTop(accessCard, permissionButton, 16);

        TextView setupTitle = text("Get connected", 20, 0xFF17213A, true);
        LinearLayout.LayoutParams setupTitleParams = params(-1, -2);
        setupTitleParams.topMargin = dp(25);
        page.addView(setupTitle, setupTitleParams);
        TextView setupText = text("Install Shizuku, then start its service using Wireless debugging (Android 11+) or the documented ADB setup. The service may need to be started again after a reboot.", 14, 0xFF69748A, false);
        LinearLayout.LayoutParams setupTextParams = params(-1, -2);
        setupTextParams.topMargin = dp(7);
        page.addView(setupText, setupTextParams);

        Button launchButton = button("Open Shizuku", true);
        LinearLayout.LayoutParams launchParams = params(-1, -2);
        launchParams.topMargin = dp(15);
        page.addView(launchButton, launchParams);
        launchButton.setOnClickListener(v -> openShizuku());

        Button guideButton = button("Setup guide", false);
        LinearLayout.LayoutParams guideParams = params(-1, -2);
        guideParams.topMargin = dp(9);
        page.addView(guideButton, guideParams);
        guideButton.setOnClickListener(v -> openUrl(SETUP_URL));

        TextView note = text("Shizuku Lite is a client, not a replacement service. It does not create shell access or run arbitrary commands. Only continue when you understand and trust the access you grant.", 12, 0xFF80899A, false);
        LinearLayout.LayoutParams noteParams = params(-1, -2);
        noteParams.topMargin = dp(22);
        page.addView(note, noteParams);

        setContentView(scroll);
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener);
        Shizuku.addBinderDeadListener(binderDeadListener);
        Shizuku.addRequestPermissionResultListener(permissionResultListener);
        refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    protected void onDestroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener);
        Shizuku.removeBinderDeadListener(binderDeadListener);
        Shizuku.removeRequestPermissionResultListener(permissionResultListener);
        super.onDestroy();
    }

    private void refreshStatus() {
        if (connectionValue == null) return;
        boolean connected;
        boolean granted = false;
        try {
            connected = Shizuku.pingBinder();
            if (connected && !Shizuku.isPreV11()) {
                granted = Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED;
            }
        } catch (RuntimeException e) {
            connected = false;
        }

        connectionValue.setText(connected ? "Connected" : "Not connected");
        connectionValue.setTextColor(connected ? 0xFF16845B : 0xFFB05B32);
        detailsValue.setText(connected
                ? "Shizuku service is available. Service privilege depends on how Shizuku was started."
                : "Start Shizuku from its app, then return here to refresh the connection.");
        permissionValue.setText(granted ? "Access granted" : connected ? "Access not granted" : "Waiting for service");
        permissionValue.setTextColor(granted ? 0xFF16845B : 0xFF17213A);
        permissionButton.setEnabled(connected && !granted);
        permissionButton.setText(granted ? "Access granted" : connected ? "Grant Shizuku access" : "Start Shizuku first");
    }

    private void requestShizukuPermission() {
        try {
            if (!Shizuku.pingBinder()) {
                Toast.makeText(this, "Start Shizuku first, then try again.", Toast.LENGTH_LONG).show();
                refreshStatus();
                return;
            }
            if (Shizuku.isPreV11()) {
                Toast.makeText(this, "This Shizuku service version is not supported. Update Shizuku.", Toast.LENGTH_LONG).show();
                return;
            }
            if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                refreshStatus();
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Toast.makeText(this, "Access was previously denied. Manage this app's permission in Shizuku.", Toast.LENGTH_LONG).show();
            } else {
                Shizuku.requestPermission(REQUEST_CODE);
            }
        } catch (RuntimeException e) {
            Toast.makeText(this, "Could not request Shizuku access. Check that the service is running.", Toast.LENGTH_LONG).show();
            refreshStatus();
        }
    }

    private void openShizuku() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(SHIZUKU_PACKAGE);
        if (launch == null) {
            openUrl(DOWNLOAD_URL);
            Toast.makeText(this, "Shizuku is not installed. Opening its download page.", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            startActivity(launch);
        } catch (ActivityNotFoundException e) {
            openUrl(DOWNLOAD_URL);
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No browser is available.", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout card() {
        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(dp(18), dp(17), dp(18), dp(18));
        view.setBackground(background(Color.WHITE, 18, 0xFFE9ECF3));
        return view;
    }

    private Button button(String label, boolean primary) {
        Button view = new Button(this);
        view.setText(label);
        view.setAllCaps(false);
        view.setTextSize(15);
        view.setTypeface(null, Typeface.BOLD);
        view.setTextColor(primary ? Color.WHITE : 0xFF34415D);
        view.setBackground(background(primary ? 0xFF5266D8 : 0xFFEEF0F8, 14, primary ? 0xFF5266D8 : 0xFFEEF0F8));
        view.setPadding(dp(12), dp(7), dp(12), dp(7));
        return view;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.START);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private void addTop(LinearLayout parent, View child, int margin) {
        LinearLayout.LayoutParams p = params(-1, -2);
        p.topMargin = dp(margin);
        parent.addView(child, p);
    }

    private GradientDrawable background(int color, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
