package com.example.campusexpensemanagement.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.campusexpensemanagement.R;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationHelper {
    private static final String CHANNEL_ID = "BUDGET_ALERT_CHANNEL";
    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    // Tạo kênh thông báo một lần khi lớp được tải
    static {
        createNotificationChannel(null); // Context sẽ được cập nhật khi gửi thông báo
    }

    public static void sendBudgetExceededNotification(Context context, String category, float spent, float budgetLimit) {
        String title = "Budget Exceeded!";
        String message = "You have spent " + spent + "đ on \"" + category + "\", exceeding your budget of " + budgetLimit + "đ.";
        sendNotification(context, title, message);
    }

    public static void sendBudgetWarningNotification(Context context, String category, float spent, float budget) {
        String title = "Budget Warning";
        String message = "You have used over 80% of your budget for \"" + category + "\" (" + spent + "/" + budget + "đ).";
        sendNotification(context, title, message);
    }

    private static void sendNotification(Context context, String title, String message) {
        // Cập nhật context cho kênh thông báo nếu cần
        if (context != null) {
            createNotificationChannel(context);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Tự động xóa khi người dùng nhấn vào

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID.incrementAndGet(), builder.build());
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context != null) {
            CharSequence name = "Budget Alerts";
            String description = "Notifications for budget limits";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}