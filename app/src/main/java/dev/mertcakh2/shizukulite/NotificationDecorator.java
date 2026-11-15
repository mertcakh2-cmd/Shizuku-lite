package dev.mertcakh2.shizukulite;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public final class NotificationDecorator {

    private static final String CHANNEL_ID = "shizuku_lite_status";
    private static final int NOTIFICATION_ID = 100;

    private NotificationDecorator() {
    }

    public static boolean postStatus(
            Activity activity,
            boolean ready,
            boolean enabled
    ) {

        if (activity == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (activity.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        NotificationManager manager =
                (NotificationManager) activity.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Shizuku Lite Status",
                            NotificationManager.IMPORTANCE_DEFAULT
                    );

            channel.setDescription(
                    "Shizuku Lite connection notifications."
            );

            manager.createNotificationChannel(channel);
        }

        String title;
        String message;

        if (ready && enabled) {
            title = "Shizuku Lite connected";
            message = "Shizuku is ready and permission is granted.";
        } else if (ready) {
            title = "Shizuku permission required";
            message = "The Shizuku binder is connected, but permission is missing.";
        } else {
            title = "Shizuku disconnected";
            message = "Open Shizuku and start its service.";
        }

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(activity, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(activity);
        }

        Notification notification = builder
                .setSmallIcon(R.drawable.ic_shizuku_lite)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(
                        new Notification.BigTextStyle()
                                .bigText(message)
                )
                .setCategory(Notification.CATEGORY_STATUS)
                .setAutoCancel(true)
                .build();

        manager.notify(NOTIFICATION_ID, notification);

        return true;
    }
}
