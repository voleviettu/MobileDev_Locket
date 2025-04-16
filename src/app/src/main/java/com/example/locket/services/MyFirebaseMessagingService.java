package com.example.locket.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.locket.R;
import com.example.locket.ui.main.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "reaction_channel";
    private static final String CHANNEL_NAME = "Reactions";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Kiểm tra nếu thông báo có dữ liệu
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }

        // Xử lý dữ liệu tùy chỉnh nếu có (dành cho thông báo reaction)
        if (remoteMessage.getData().containsKey("photoId")) {
            String photoId = remoteMessage.getData().get("photoId");
            String reaction = remoteMessage.getData().get("reaction");
            showReactionNotification(photoId, reaction);
        }
    }

    private void showNotification(String title, String message) {
        // Tạo intent để mở MainActivity khi nhấn thông báo
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo notification channel
        createNotificationChannel();

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Thay bằng icon của bạn
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Hiển thị thông báo
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void showReactionNotification(String photoId, String reaction) {
        // Tạo intent để mở chi tiết ảnh
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("photoId", photoId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo notification channel
        createNotificationChannel();

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Phản ứng mới!")
                .setContentText("Có một phản ứng mới: " + reaction)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Hiển thị thông báo
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(photoId.hashCode(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo về phản ứng mới");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Lưu token vào Firestore nếu cần (xem phần sau)
    }
}