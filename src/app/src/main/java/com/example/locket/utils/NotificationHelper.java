package com.example.locket.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.locket.R;
import com.example.locket.ui.photo.PhotoActivity;
import com.example.locket.model.PhotoReaction;

public class NotificationHelper {
    private static final String CHANNEL_ID = "reaction_channel";
    private static final String CHANNEL_NAME = "Reactions";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo về phản ứng mới");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void showReactionNotification(PhotoReaction reaction) {
        Log.d("NotificationHelper", "Hiển thị thông báo cho reaction: " + reaction.getReaction() + ", photoId: " + reaction.getPhotoId());
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra("photoId", reaction.getPhotoId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Lượt reaction mới!")
                .setContentText("Có một reaction mới: " + reaction.getReaction())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.notify(reaction.getId().hashCode(), builder.build());
        Log.d("NotificationHelper", "Thông báo đã được gửi với ID: " + reaction.getId().hashCode());
    }
}