package com.example.locket.ui.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.model.PhotoReaction;

import com.example.locket.model.User;
import com.example.locket.ui.profile.ProfileActivity;
import com.example.locket.utils.NavigationUtils;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.PhotoReactionViewModel;
import com.example.locket.viewmodel.SharedPhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;
import com.example.locket.viewmodel.PhotoViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedPhotoFriendActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private SharedPhotoViewModel sharedPhotoViewModel;
    private UserViewModel userViewModel;
    private PhotoViewModel photoViewModel;
    private FriendViewModel friendViewModel;
    private PhotoReactionViewModel photoReactionViewModel;
    private String currentUserId;
    private List<User> allUsers = new ArrayList<>();
    private List<Photo> photoList = new ArrayList<>();
    private Photo currentPhoto;
    private List<User> friendList = new ArrayList<>();
    private TextView title;
    private User selectedFriend = null;

    private ImageView emojiHeart, emojiFire, emojiSmile;

    private ImageView btnProfile, btnChat, btnShowAll, btnCapture, btnOption;

    private LinearLayout messageInputContainer, reactionInfoContainer, reactionAvatarsContainer;
    private TextView reactionInfoText;
    private boolean adapterInitialized = false;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_photo_friend);

        String targetPhotoId = getIntent().getStringExtra("photoId");
        String selectedFriendId = getIntent().getStringExtra("selectedFriendId");
        String selectedFriendName = getIntent().getStringExtra("selectedFriendName");
        String selectedFriendLastname = getIntent().getStringExtra("selectedFriendLastname");

        viewPager2 = findViewById(R.id.photo_feed_pager);
        btnChat = findViewById(R.id.btn_chat);
        btnShowAll = findViewById(R.id.btn_showall);
        btnOption = findViewById(R.id.btn_option);
        btnCapture = findViewById(R.id.btn_capture);
        btnProfile = findViewById(R.id.btn_profile);

        title = findViewById(R.id.tv_title);

        emojiHeart = findViewById(R.id.emoji_heart);
        emojiFire = findViewById(R.id.emoji_fire);
        emojiSmile = findViewById(R.id.emoji_smile);

        messageInputContainer = findViewById(R.id.message_input_container); // Khởi tạo container
        reactionInfoContainer = findViewById(R.id.reaction_info_container); // Khởi tạo container
        reactionInfoText = findViewById(R.id.tv_reaction_info);
        reactionAvatarsContainer = findViewById(R.id.reaction_avatars_container);


        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        photoReactionViewModel = new ViewModelProvider(this).get(PhotoReactionViewModel.class);

        // Quan sát reactionsLiveData một lần duy nhất
        photoReactionViewModel.getReactionsLiveData().observe(this, reactions -> {
            if (reactions != null && !reactions.isEmpty()) {
                reactionInfoText.setText("Hoạt động");
                Log.d("FeedPhotoFriendActivity", "Reactions: " + reactions.size());

                Set<String> userIds = new HashSet<>();
                for (PhotoReaction reaction : reactions) {
                    userIds.add(reaction.getUserId());
                }

                reactionAvatarsContainer.removeAllViews();

                int avatarCount = 0;
                for (String userId : userIds) {
                    if (avatarCount >= 3) break; // Giới hạn 3 avatar

                    for (User user : allUsers) {
                        if (user.getUid().equals(userId)) {
                            ImageView avatarView = new ImageView(this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(90, 90);
                            params.setMargins(0, 0, 8, 0); // Khoảng cách giữa các avatar
                            avatarView.setLayoutParams(params);
                            avatarView.setBackgroundResource(R.drawable.circle_background); // Background tròn
                            avatarView.setPadding(2, 2, 2, 2);

                            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                                Glide.with(this)
                                        .load(user.getAvatar())
                                        .circleCrop()
                                        .into(avatarView);
                            } else {
                                avatarView.setImageResource(R.drawable.ic_profile); // Ảnh mặc định
                            }

                            reactionAvatarsContainer.addView(avatarView);
                            avatarCount++;
                            break;
                        }
                    }
                }
            } else {
                reactionInfoText.setText("Chưa có hoạt động nào!");
                reactionAvatarsContainer.removeAllViews();
                Log.d("FeedPhotoFriendActivity", "No reactions for this photo");
            }
        });

        userViewModel.getCurrentUser().observe(this, currentUser -> {
            if (currentUser != null) {
                currentUserId = currentUser.getUid();

                if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                    Glide.with(this).load(currentUser.getAvatar()).circleCrop().into(btnProfile);
                }

                friendViewModel.loadFriends(currentUserId);
                friendViewModel.getFriends().observe(this, friends -> {
                    friendList.clear();
                    if (friends != null) {
                        friendList.addAll(friends);
                    }
                    if (currentUser != null) {
                        User self = new User(
                                currentUser.getUid(),
                                currentUser.getEmail(),
                                "",
                                "Bạn",
                                currentUser.getUsername(),
                                currentUser.getAvatar(),
                                currentUser.isPremium()
                        );
                        friendList.add(self);
                    }
                });

                userViewModel.getAllUsers().observe(this, users -> {
                    if (users != null) {
                        allUsers.clear();
                        allUsers.addAll(users);
                    }

                    if (adapterInitialized) return;

                    if (selectedFriendId == null) {
                        title.setText("Tất cả bạn bè");
                        sharedPhotoViewModel.getSharedPhotos(currentUserId).observe(this, sharedPhotos -> {
                            updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId);
                            adapterInitialized = true;
                        });
                    } else if ("Bạn".equals(selectedFriendLastname)) {
                        title.setText("Bạn");
                        sharedPhotoViewModel.getMyPhotos(currentUserId).observe(this, sharedPhotos -> {
                            updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId);
                            adapterInitialized = true;
                        });
                    } else {
                        title.setText(selectedFriendName);
                        sharedPhotoViewModel.getPhotosSharedWithMe(selectedFriendId, currentUserId).observe(this, sharedPhotos -> {
                            updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId);
                            adapterInitialized = true;
                        });
                    }
                });
            }
        });

        title.setOnClickListener(v -> {
            if (!friendList.isEmpty()) {
                FriendDialog dialog = new FriendDialog(friendList, friend -> {
                    selectedFriend = friend;

                    if (friend == null) {
                        title.setText("Tất cả bạn bè");
                        sharedPhotoViewModel.getSharedPhotos(currentUserId).observe(this, this::updatePhotoFeedBySender);
                    } else if ("Bạn".equals(friend.getLastname())) {
                        title.setText("Bạn");
                        sharedPhotoViewModel.getMyPhotos(currentUserId).observe(this, this::updatePhotoFeedBySender);
                    } else {
                        title.setText(friend.getFullName());
                        sharedPhotoViewModel.getPhotosSharedWithMe(friend.getUid(), currentUserId).observe(this, this::updatePhotoFeedBySender);
                    }
                });
                dialog.show(getSupportFragmentManager(), "friendDialog");
            } else {
                Toast.makeText(this, "Không có bạn bè nào", Toast.LENGTH_SHORT).show();
            }
        });

        btnShowAll.setOnClickListener(v -> {
            Intent intent = new Intent(FeedPhotoFriendActivity.this, FullPhotoActivity.class);
            if (selectedFriend != null) {
                intent.putExtra("selectedFriendId", selectedFriend.getUid());
                intent.putExtra("selectedFriendName", selectedFriend.getFullName());
                intent.putExtra("selectedFriendLastname", selectedFriend.getLastname()); // để kiểm tra "Bạn"
            }
            startActivity(intent);
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(FeedPhotoFriendActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
        NavigationUtils.setChatButtonClickListener(btnChat, this);
        NavigationUtils.setCaptureButtonClickListener(btnCapture, this);

        btnOption.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(view);

            bottomSheetDialog.setCanceledOnTouchOutside(true);

            Button btnSave = view.findViewById(R.id.btn_save);
            Button btnDelete = view.findViewById(R.id.btn_delete);

            btnSave.setOnClickListener(view1 -> {
                String imageUrl = currentPhoto.getImageUrl();
                Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                saveBitmapToGallery(resource);
                                Toast.makeText(FeedPhotoFriendActivity.this, "Ảnh đã được lưu!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                Toast.makeText(FeedPhotoFriendActivity.this, "Lỗi khi tải ảnh!", Toast.LENGTH_SHORT).show();
                            }
                        });

                bottomSheetDialog.dismiss();
            });

            btnDelete.setOnClickListener(view2 -> {
                if (currentPhoto == null || currentUserId == null) {
                    Toast.makeText(this, "Không thể xoá ảnh (thiếu thông tin)", Toast.LENGTH_SHORT).show();
                    return;
                }
                photoViewModel.deletePhoto(currentUserId, currentPhoto,
                        () -> {
                            Toast.makeText(this, "Đã xoá ảnh!", Toast.LENGTH_SHORT).show();
                            photoList.remove(currentPhoto);
                            viewPager2.getAdapter().notifyDataSetChanged();
                            if (photoList.isEmpty()) {
                                finish();
                            } else {
                                currentPhoto = photoList.get(Math.min(viewPager2.getCurrentItem(), photoList.size() - 1));
                            }

                        },
                        e -> {
                            Toast.makeText(this, "Lỗi khi xoá ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });

        emojiHeart.setOnClickListener(v -> {
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "love"));
            updateUIBasedOnPhotoOwner();
        });

        emojiFire.setOnClickListener(v -> {
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "fire"));
            updateUIBasedOnPhotoOwner();
        });

        emojiSmile.setOnClickListener(v -> {
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "smile"));
            updateUIBasedOnPhotoOwner();
        });
    }

    private void updatePhotoFeedAndScroll(List<User> users, List<Photo> photos, String targetPhotoId) {
        if (photos != null && !photos.isEmpty()) {
            photoList.clear();
            photoList.addAll(photos);

            DetailPhotoFriendAdapter adapter = new DetailPhotoFriendAdapter(
                    FeedPhotoFriendActivity.this,
                    photoList,
                    users,
                    currentUserId,
                    FeedPhotoFriendActivity.this
            );
            viewPager2.setAdapter(adapter);

            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position < photoList.size()) {
                        currentPhoto = photoList.get(position);
                        updateUIBasedOnPhotoOwner();
                    }
                }
            });

            if (targetPhotoId != null) {
                for (int i = 0; i < photoList.size(); i++) {
                    if (photoList.get(i).getPhotoId().equals(targetPhotoId)) {
                        viewPager2.setCurrentItem(i, false);
                        break;
                    }
                }
            }
        } else {
            photoList.clear();
            viewPager2.setAdapter(null);
            Toast.makeText(this, "Không có ảnh nào!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePhotoFeedBySender(List<Photo> photos) {
        if (photos == null || allUsers.isEmpty()) return;
        photoList.clear();
        photoList.addAll(photos);
        DetailPhotoFriendAdapter adapter = new DetailPhotoFriendAdapter(
                FeedPhotoFriendActivity.this,
                photoList,
                allUsers,
                currentUserId,
                FeedPhotoFriendActivity.this
        );
        viewPager2.setAdapter(adapter);
        if (!photoList.isEmpty()) {
            currentPhoto = photoList.get(0);
            updateUIBasedOnPhotoOwner();
        }
    }
    private void updateUIBasedOnPhotoOwner() {
        if (currentPhoto == null || currentUserId == null) {
            Log.w("FeedPhotoFriendActivity", "currentPhoto or currentUserId is null");
            messageInputContainer.setVisibility(View.VISIBLE);
            reactionInfoContainer.setVisibility(View.GONE);
            return;
        }

        boolean isOwnPhoto = currentPhoto.getUserId().equals(currentUserId);
        Log.d("FeedPhotoFriendActivity", "Is own photo: " + isOwnPhoto);

        if (isOwnPhoto) {
            messageInputContainer.setVisibility(View.GONE);
            reactionInfoContainer.setVisibility(View.VISIBLE);
            photoReactionViewModel.fetchReactionsForPhoto(currentPhoto.getPhotoId());
        } else {
            messageInputContainer.setVisibility(View.VISIBLE);
            reactionInfoContainer.setVisibility(View.GONE);
        }
    }
    private void saveBitmapToGallery(Bitmap bitmap) {
        String filename = "Locket_" + System.currentTimeMillis() + ".jpg";
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(picturesDir, filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateEmojiClick(View view) {
        view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }
}
