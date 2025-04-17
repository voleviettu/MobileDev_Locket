package com.example.locket.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.bumptech.glide.Glide;
import com.example.locket.ui.photo.FeedPhotoFriendActivity;
import com.example.locket.R;
import com.example.locket.data.PhotoRepository;
import com.example.locket.data.SharedPhotoRepository;
import com.example.locket.data.UserRepository;
import com.example.locket.model.Photo;
import com.example.locket.model.User;
import com.google.firebase.auth.FirebaseAuth; // Để lấy UID

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WidgetUpdateWorker extends Worker {

    private static final String TAG = "WidgetUpdateWorker";
    public static final String WIDGET_ID_KEY = "WIDGET_ID";
    private final Context context;
    private final int appWidgetId;
    private final AppWidgetManager appWidgetManager;
    private final UserRepository userRepository;
    private final SharedPhotoRepository sharedPhotoRepository;
    private final PhotoRepository photoRepository;
    private List<User> cachedAllUsers = null; // Cache để tránh load lại user list

    public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
        this.appWidgetId = workerParams.getInputData().getInt(WIDGET_ID_KEY, AppWidgetManager.INVALID_APPWIDGET_ID);
        this.appWidgetManager = AppWidgetManager.getInstance(this.context);
        // Khởi tạo Repositories
        this.userRepository = new UserRepository();
        this.sharedPhotoRepository = new SharedPhotoRepository();
        this.photoRepository = new PhotoRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting work for widget ID: " + appWidgetId);
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID.");
            return Result.failure();
        }

        String config = WidgetConfigureActivity.loadConfiguration(context, appWidgetId);
        Log.d(TAG, "Config for widget " + appWidgetId + ": " + config);

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId == null) {
            Log.e(TAG, "User not logged in.");
            updateWithError("Chưa đăng nhập");
            return Result.failure();
        }

        try {
            Photo latestPhoto = null;
            List<String> photoIds;

            if (WidgetConfigureActivity.VALUE_ALL_FRIENDS.equals(config)) {
                Log.d(TAG, "Fetching LATEST shared photo ID for ALL friends...");
                // Lấy danh sách ID ảnh được chia sẻ VỚI user hiện tại, sắp xếp mới nhất trước
                photoIds = sharedPhotoRepository.getSharedPhotoIdsBlocking(currentUserId);
                if (!photoIds.isEmpty()) {
                    String latestPhotoId = photoIds.get(0); // Lấy ID mới nhất
                    Log.d(TAG, "Latest shared photo ID: " + latestPhotoId);
                    // Lấy chi tiết ảnh bằng ID đó
                    List<Photo> result = photoRepository.getPhotosByIdsBlocking(Collections.singletonList(latestPhotoId));
                    if (!result.isEmpty()) {
                        latestPhoto = result.get(0);
                    }
                } else {
                    Log.d(TAG, "No photos shared with user " + currentUserId);
                }

            } else if (WidgetConfigureActivity.VALUE_SELF.equals(config)) {
                Log.d(TAG, "Fetching LATEST SELF photo...");
                // Lấy danh sách ảnh CỦA user hiện tại, sắp xếp mới nhất trước
                List<Photo> selfPhotos = photoRepository.getPhotosByUserBlocking(currentUserId);
                if (!selfPhotos.isEmpty()) {
                    latestPhoto = selfPhotos.get(0); // Lấy ảnh mới nhất
                    Log.d(TAG, "Latest self photo ID: " + latestPhoto.getPhotoId());
                } else {
                    Log.d(TAG, "No photos found for user " + currentUserId);
                }

            } else { // Config là friendId
                String friendId = config;
                Log.d(TAG, "Fetching LATEST shared photo ID from friend: " + friendId);
                // Lấy danh sách ID ảnh được chia sẻ BỞI friendId CHO user hiện tại, sắp xếp mới nhất trước
                photoIds = sharedPhotoRepository.getPhotosSharedByFriendBlocking(friendId, currentUserId);
                if (!photoIds.isEmpty()) {
                    String latestPhotoId = photoIds.get(0); // Lấy ID mới nhất
                    Log.d(TAG, "Latest shared photo ID from friend " + friendId + ": " + latestPhotoId);
                    // Lấy chi tiết ảnh bằng ID đó
                    List<Photo> result = photoRepository.getPhotosByIdsBlocking(Collections.singletonList(latestPhotoId));
                    if (!result.isEmpty()) {
                        latestPhoto = result.get(0);
                    }
                } else {
                    Log.d(TAG, "No photos shared by " + friendId + " to " + currentUserId);
                }
            }

            // Kiểm tra kết quả và cập nhật UI
            if (latestPhoto == null) {
                Log.d(TAG, "No suitable photo found to display.");
                updateWithNoPhotos();
                return Result.success();
            }

            // Lấy thông tin người gửi (người tạo ra ảnh)
            User sender = findUserByIdBlocking(latestPhoto.getUserId()); // userId là người tạo ảnh
            String senderName = "Không rõ";
            String senderAvatarUrl = null;
            if (sender != null) {
                senderName = sender.getFullName() != null && !sender.getFullName().isEmpty()
                        ? sender.getFullName()
                        : (sender.getUsername() != null ? sender.getUsername() : "Người dùng");
                senderAvatarUrl = sender.getAvatar();
                Log.d(TAG, "Found sender info: " + senderName);
            } else {
                Log.w(TAG,"Sender user not found for ID: " + latestPhoto.getUserId());
            }

            // Cập nhật UI widget
            updateWidgetUI(latestPhoto, senderName, senderAvatarUrl, config); // Truyền config vào
            Log.d(TAG, "Work finished successfully for widget ID: " + appWidgetId);
            return Result.success();

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Error during blocking fetch or thread interrupted: " + e.getMessage(), e);
            updateWithError("Lỗi tải dữ liệu");
            return Result.retry();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error during widget update: " + e.getMessage(), e);
            updateWithError("Lỗi không xác định");
            return Result.failure();
        }
    }

    private User findUserByIdBlocking(String userId) throws ExecutionException, InterruptedException {
        if (cachedAllUsers != null) {
            for (User user : cachedAllUsers) {
                if (user.getUid().equals(userId)) {
                    return user;
                }
            }
            Log.w(TAG, "User " + userId + " not found in cache. Fetching all users again.");
        }
        Log.d(TAG,"Fetching all users blocking to find sender: " + userId);
        cachedAllUsers = userRepository.getAllUsersBlocking();
        if (cachedAllUsers != null) {
            for (User user : cachedAllUsers) {
                if (user.getUid().equals(userId)) {
                    return user;
                }
            }
            Log.w(TAG,"User " + userId + " not found after fetching all users.");
        } else {
            Log.e(TAG,"getAllUsersBlocking returned null.");
            cachedAllUsers = new ArrayList<>();
        }
        return null;
    }

    private void updateWithNoPhotos() {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
        views.setViewVisibility(R.id.widget_progress, View.GONE);
        views.setImageViewResource(R.id.widget_photo, R.drawable.widget_placeholder);
        views.setTextViewText(R.id.widget_sender_name, "");
        views.setTextViewText(R.id.widget_caption, "Chưa có ảnh nào");
        views.setImageViewResource(R.id.widget_avatar, R.drawable.ic_profile);
        views.setOnClickPendingIntent(R.id.widget_container, null);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG,"Updated widget " + appWidgetId + " with 'no photos' message.");
    }
    private void updateWithError(String errorMessage) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);
        views.setViewVisibility(R.id.widget_progress, View.GONE);
        views.setImageViewResource(R.id.widget_photo, R.drawable.widget_placeholder);
        views.setTextViewText(R.id.widget_sender_name, "Lỗi");
        views.setTextViewText(R.id.widget_caption, errorMessage);
        views.setImageViewResource(R.id.widget_avatar, R.drawable.ic_profile);
        views.setOnClickPendingIntent(R.id.widget_container, null);
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG,"Updated widget " + appWidgetId + " with error message: " + errorMessage);
    }


    // Cập nhật UI với ảnh và thông tin - **CẬP NHẬT PENDING INTENT**
    private void updateWidgetUI(Photo photo, String senderName, @Nullable String senderAvatarUrl, String config) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.locket_widget_layout);

        // Đặt Text
        views.setTextViewText(R.id.widget_sender_name, senderName);
        views.setTextViewText(R.id.widget_caption, photo.getCaption() != null ? photo.getCaption() : "");
        views.setViewVisibility(R.id.widget_progress, View.GONE);

        // Tạo PendingIntent để mở ảnh trong app - ĐIỀU CHỈNH EXTRA DỰA TRÊN CONFIG
        Intent intent = new Intent(context, FeedPhotoFriendActivity.class);
        intent.putExtra("photoId", photo.getPhotoId()); // Luôn gửi photoId để focus vào ảnh đó

        // Gửi thông tin cấu hình gốc của widget để FeedPhotoFriendActivity biết nên hiển thị feed nào ban đầu
        if (WidgetConfigureActivity.VALUE_ALL_FRIENDS.equals(config)) {
            // Không cần gửi selectedFriendId, FeedPhotoFriendActivity sẽ hiểu là xem tất cả
            Log.d(TAG,"PendingIntent for ALL friends feed, photo: " + photo.getPhotoId());
        } else if (WidgetConfigureActivity.VALUE_SELF.equals(config)) {
            intent.putExtra("selectedFriendId", photo.getUserId()); // Gửi ID của chính mình
            intent.putExtra("selectedFriendLastname", "Bạn"); // Đánh dấu để FeedPhotoFriendActivity biết là feed "Bạn"
            Log.d(TAG,"PendingIntent for SELF feed, photo: " + photo.getPhotoId());
        } else {
            intent.putExtra("selectedFriendId", config); // Gửi friendId đã lưu trong config
            Log.d(TAG,"PendingIntent for friend feed ("+config+"), photo: " + photo.getPhotoId());
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Tải ảnh bằng Glide (đồng bộ trong worker) - Giữ nguyên
        try {
            Bitmap photoBitmap = Glide.with(context).asBitmap().load(photo.getImageUrl())
                    .placeholder(R.drawable.widget_placeholder).error(R.drawable.widget_placeholder)
                    .submit().get();
            views.setImageViewBitmap(R.id.widget_photo, photoBitmap);
            Log.d(TAG,"Photo bitmap loaded for widget " + appWidgetId);

            Bitmap avatarBitmap = Glide.with(context).asBitmap().load(senderAvatarUrl)
                    .placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).circleCrop()
                    .submit().get();
            views.setImageViewBitmap(R.id.widget_avatar, avatarBitmap);
            Log.d(TAG,"Avatar bitmap loaded for widget " + appWidgetId);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d(TAG,"Widget UI updated successfully with images for "+appWidgetId);

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Error loading image with Glide sync: " + e.getMessage(), e);
            views.setImageViewResource(R.id.widget_photo, R.drawable.widget_placeholder);
            views.setImageViewResource(R.id.widget_avatar, R.drawable.ic_profile);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.w(TAG,"Updated widget " + appWidgetId + " with placeholder images due to Glide error.");
        } catch (Exception e){
            Log.e(TAG, "Unexpected error during image loading/setting: " + e.getMessage(), e);
        }
    }
}