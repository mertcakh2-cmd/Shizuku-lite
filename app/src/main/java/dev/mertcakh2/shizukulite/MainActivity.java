package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import rikka.shizuku.Shizuku;

/** Lightweight, polished dashboard backed by the official Shizuku client API. */
public final class MainActivity extends Activity {
    private static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";
    private static final String SETUP_URL = "https://shizuku.rikka.app/guide/setup/";
    private static final String DOWNLOAD_URL = "https://shizuku.rikka.app/download/";
    private static final int SHIZUKU_PERMISSION_REQUEST = 1001;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 1002;

    private TextView connectionValue;
    private TextView permissionValue;
    private TextView detailsValue;
    private Button permissionButton;
    private Button notificationButton;
    private boolean connected;
    private boolean accessGranted;

    private final Shizuku.OnBinderReceivedListener binderReceivedListener = this::refreshStatus;
    private final Shizuku.OnBinderDeadListener binderDeadListener = this::refreshStatus;
    private final Shizuku.OnRequestPermissionResultListener permissionResultListener = (requestCode, result) -> {
        if (requestCode == SHIZUKU_PERMISSION_REQUEST) {
            refreshStatus();
            Toast.makeText(this, result == PackageManager.PERMISSION_GRANTED
                    ? "Shizuku access granted" : "Shizuku access was not granted", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(0xFFF5F6FB);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(28));
        scroll.addView(page);

        LinearLayout brand = new LinearLayout(this);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_shizuku_lite);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(54), dp(54));
        brand.addView(logo, logoParams);
        LinearLayout brandText = new LinearLayout(this);
        brandText.setOrientation(LinearLayout.VERTICAL);
        brandText.setPadding(dp(12), 0, 0, 0);
        brandText.addView(text("SHIZUKU CLIENT", 11, 0xFF5266D8, true));
        brandText.addView(text("Shizuku Lite", 27, 0xFF17213A, true));
        brand.addView(brandText, params(-2, -2));
        page.addView(brand);

        TextView intro = text("A lightweight dashboard for your Shizuku connection.", 15, 0xFF69748A, false);
        addTop(page, intro, 12);

        LinearLayout statusCard = card();
        addTop(page, statusCard, 23);
        statusCard.addView(text("SERVICE", 11, 0xFF758098, true));
        connectionValue = text("Checking…", 23, 0xFF17213A, true);
        addTop(statusCard, connectionValue, 8);
        detailsValue = text("Looking for the Shizuku service on this device.", 14, 0xFF69748A, false);
        addTop(statusCard, detailsValue, 6);

        LinearLayout accessCard = card();
        addTop(page, accessCard, 13);
        accessCard.addView(text("APP PERMISSION", 11, 0xFF758098, true));
        permissionValue = text("Checking permission…", 18, 0xFF17213A, true);
        addTop(accessCard, permissionValue, 8);
        addTop(accessCard, text("Permission is requested from Shizuku and remains under your control.", 14, 0xFF69748A, false), 6);
        permissionButton = button("Grant Shizuku access", true);
        permissionButton.setOnClickListener(v -> requestShizukuPermission());
        addTop(accessCard, permissionButton, 15);

        TextView setupTitle = text("Get connected", 20, 0xFF17213A, true);
        addTop(page, setupTitle, 24);
        TextView setupText = text("Install Shizuku, then start its service using Wireless debugging (Android 11+) or the documented ADB setup. The service may need to be restarted after a reboot.", 14, 0xFF69748A, false);
        addTop(page, setupText, 6);

        Button launchButton = button("Open Shizuku", true);
        launchButton.setOnClickListener(v -> openShizuku());
        addTop(page, launchButton, 14);
        Button guideButton = button("Read setup guide", false);
        guideButton.setOnClickListener(v -> openUrl(SETUP_URL));
        addTop(page, guideButton, 8);

        TextView notificationTitle = text("Notifications", 20, 0xFF17213A, true);
        addTop(page, notificationTitle, 24);
        TextView notificationInfo = text("Send a branded notification with the current connection and permission state.", 14, 0xFF69748A, false);
        addTop(page, notificationInfo, 6);
        notificationButton = button("Send status notification", false);
        notificationButton.setOnClickListener(v -> requestNotificationOrPost());
        addTop(page, notificationButton, 10);

        TextView note = text("Shizuku Lite is a client, not a replacement service. It does not create shell access or run arbitrary commands.", 12, 0xFF80899A, false);
        addTop(page, note, 21);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                postStatusNotification();
            } else {
                Toast.makeText(this, "Notification permission was not granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshStatus() {
        if (connectionValue == null) return;
        connected = false;
        accessGranted = false;
        try {
            connected = Shizuku.pingBinder();
            if (connected && !Shizuku.isPreV11()) {
                accessGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            }
        } catch (RuntimeException ignored) {
            connected = false;
        }

        connectionValue.setText(connected ? "Connected" : "Not connected");
        connectionValue.setTextColor(connected ? 0xFF16845B : 0xFFB05B32);
        detailsValue.setText(connected
                ? "The service is available. Its privilege depends on how Shizuku was started."
                : "Start Shizuku from its app, then return here to refresh the connection.");
        permissionValue.setText(accessGranted ? "Access granted" : connected ? "Access not granted" : "Waiting for service");
        permissionValue.setTextColor(accessGranted ? 0xFF16845B : 0xFF17213A);
        permissionButton.setEnabled(connected && !accessGranted);
        permissionButton.setText(accessGranted ? "Access granted" : connected ? "Grant Shizuku access" : "Start Shizuku first");
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
            } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                refreshStatus();
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                Toast.makeText(this, "Access was previously denied. Manage this app's permission in Shizuku.", Toast.LENGTH_LONG).show();
            } else {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST);
            }
        } catch (RuntimeException e) {
            Toast.makeText(this, "Could not request Shizuku access. Check that the service is running.", Toast.LENGTH_LONG).show();
            refreshStatus();
        }
    }

    private void requestNotificationOrPost() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
            return;
        }
        postStatusNotification();
    }

    private void postStatusNotification() {
        if (NotificationDecorator.postStatus(this, connected, accessGranted)) {
            Toast.makeText(this, "Status notification sent.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Could not send the notification.", Toast.LENGTH_SHORT).show();
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
        LinearLayout.LayoutParams layoutParams = params(-1, -2);
        layoutParams.topMargin = dp(margin);
        parent.addView(child, layoutParams);
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
