package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** Creates a user-requested, branded service-status notification. */
public final class NotificationDecorator {
    private static final String CHANNEL_ID = "service_status";
    private static final int NOTIFICATION_ID = 100;

    private NotificationDecorator() { }

    public static boolean postStatus(Activity activity, boolean connected, boolean accessGranted) {
        if (Build.VERSION.SDK_INT >= 33
                && activity.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        NotificationManager manager = (NotificationManager)
                activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Shizuku Lite status", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Status notifications requested from Shizuku Lite.");
            manager.createNotificationChannel(channel);
        }

        String title = connected ? "Shizuku is connected" : "Shizuku is not connected";
        String message = connected
                ? (accessGranted ? "This app has Shizuku permission." : "This app does not have Shizuku permission yet.")
                : "Start the Shizuku service, then return to Shizuku Lite.";

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(activity, CHANNEL_ID)
                : new Notification.Builder(activity);
        Notification notification = builder
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new Notification.BigTextStyle().bigText(message))
                .setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .build();
        manager.notify(NOTIFICATION_ID, notification);
        return true;
    }
}
