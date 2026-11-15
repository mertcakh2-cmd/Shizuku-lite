package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/** Independent notification helper for the standalone app dashboard. */
public final class NotificationDecorator {
    private static final String CHANNEL_ID = "lite_dashboard_status";
    private static final int NOTIFICATION_ID = 100;

    private NotificationDecorator() { }

    public static boolean postStatus(Activity activity, boolean ready, boolean enabled) {
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
                    CHANNEL_ID,
                    "Lite Dashboard status",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Status updates from the standalone Lite Dashboard app.");
            manager.createNotificationChannel(channel);
        }

        String title = ready ? "App ready" : "App not ready";
        String message = ready
                ? (enabled ? "Everything is running smoothly." : "The app is ready but notification access is not enabled.")
                : "Open the app to check your current status.";

        Notification.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(activity, CHANNEL_ID)
                : new Notification.Builder(activity);

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_shizuku_lite)
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
