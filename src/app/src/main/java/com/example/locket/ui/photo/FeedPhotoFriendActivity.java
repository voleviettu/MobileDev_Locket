package com.example.locket.ui.photo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.locket.BaseActivity;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Message;
import com.example.locket.model.Photo;
import com.example.locket.model.PhotoReaction;
import com.example.locket.model.User;
import com.example.locket.ui.profile.ProfileActivity;
import com.example.locket.ui.settings.ReactionAdapter;
import com.example.locket.utils.NavigationUtils;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.MessageViewModel;
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

public class FeedPhotoFriendActivity extends BaseActivity {

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

    private ImageView emojiHeart, emojiFire, emojiSmile, emojiAnotherReact;
    private ImageView btnProfile, btnChat, btnShowAll, btnCapture, btnOption;

    private LinearLayout messageInputContainer, reactionInfoContainer, reactionAvatarsContainer;
    private TextView reactionInfoText, inputMessage;

    private List<PhotoReaction> currentReactions = new ArrayList<>();
    private boolean adapterInitialized = false;

    private static final String AD_IMAGE_URL = String.valueOf(R.drawable.ad);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_photo_friend);

        String targetPhotoId = getIntent().getStringExtra("photoId");
        String selectedFriendId = getIntent().getStringExtra("selectedFriendId");
        String selectedFriendName = getIntent().getStringExtra("selectedFriendName");
        String selectedFriendLastname = getIntent().getStringExtra("selectedFriendLastname");

        // Khởi tạo views
        viewPager2 = findViewById(R.id.photo_feed_pager);
        btnChat = findViewById(R.id.btn_chat);
        btnShowAll = findViewById(R.id.btn_showall);
        btnOption = findViewById(R.id.btn_option);
        btnCapture = findViewById(R.id.btn_capture);
        btnProfile = findViewById(R.id.btn_profile);
        inputMessage = findViewById(R.id.input_message);
        title = findViewById(R.id.tv_title);

        emojiHeart = findViewById(R.id.emoji_heart);
        emojiFire = findViewById(R.id.emoji_fire);
        emojiSmile = findViewById(R.id.emoji_smile);
        emojiAnotherReact = findViewById(R.id.emoji_another_react);

        messageInputContainer = findViewById(R.id.message_input_container);
        reactionInfoContainer = findViewById(R.id.reaction_info_container);
        reactionInfoText = findViewById(R.id.tv_reaction_info);
        reactionAvatarsContainer = findViewById(R.id.reaction_avatars_container);

        // Khởi tạo ViewModel
        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        photoReactionViewModel = new ViewModelProvider(this).get(PhotoReactionViewModel.class);

        // Kiểm tra inputMessage
        if (inputMessage == null) {
            Log.e("FeedPhotoFriendActivity", "input_message is null. Please check activity_feed_photo_friend.xml layout.");
        } else {
            inputMessage.setOnClickListener(v -> showMessageInputDialog());
        }

        // Quan sát user hiện tại
        userViewModel.getCurrentUser().observe(this, currentUser -> {
            if (currentUser == null) return;

            currentUserId = currentUser.getUid();
            boolean isPremium = currentUser.isPremium();

            // Tải avatar một lần
            if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                Glide.with(this).load(currentUser.getAvatar()).circleCrop().into(btnProfile);
            } else {
                btnProfile.setImageResource(R.drawable.ic_profile);
            }

            // Load danh sách bạn bè
            friendViewModel.loadFriends(currentUserId);
            friendViewModel.getFriends().observe(this, friends -> {
                friendList.clear();
                if (friends != null) {
                    friendList.addAll(friends);
                }
                // Thêm chính user vào danh sách bạn bè
                User self = new User(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        "",
                        getString(R.string.self),
                        currentUser.getUsername(),
                        currentUser.getAvatar(),
                        currentUser.isPremium()
                );
                friendList.add(self);
            });

            // Quan sát tất cả users
            userViewModel.getAllUsers().observe(this, users -> {
                if (users == null) return;

                allUsers.clear();
                allUsers.addAll(users);

                if (adapterInitialized) return;

                // Load danh sách ảnh dựa trên selectedFriendId
                if (selectedFriendId == null) {
                    title.setText(R.string.all_friends);
                    sharedPhotoViewModel.getSharedPhotos(currentUserId).observe(this, sharedPhotos -> {
                        updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId, isPremium);
                        adapterInitialized = true;
                    });
                } else if (getString(R.string.self).equals(selectedFriendLastname)) {
                    title.setText(R.string.self);
                    sharedPhotoViewModel.getMyPhotos(currentUserId).observe(this, sharedPhotos -> {
                        updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId, isPremium);
                        adapterInitialized = true;
                    });
                } else {
                    title.setText(selectedFriendName);
                    sharedPhotoViewModel.getPhotosSharedWithMe(selectedFriendId, currentUserId).observe(this, sharedPhotos -> {
                        updatePhotoFeedAndScroll(users, sharedPhotos, targetPhotoId, isPremium);
                        adapterInitialized = true;
                    });
                }
            });
        });

        // Quan sát reactionsLiveData
        photoReactionViewModel.getReactionsLiveData().observe(this, reactions -> {
            if (reactions != null && !reactions.isEmpty()) {
                reactionInfoText.setText(R.string.activity);
                Log.d("FeedPhotoFriendActivity", "Reactions: " + reactions.size());

                currentReactions.clear();
                currentReactions.addAll(reactions);

                Set<String> userIds = new HashSet<>();
                for (PhotoReaction reaction : reactions) {
                    userIds.add(reaction.getUserId());
                }

                reactionAvatarsContainer.removeAllViews();

                int avatarCount = 0;
                for (String userId : userIds) {
                    if (avatarCount >= 3) break;

                    for (User user : allUsers) {
                        if (user.getUid().equals(userId)) {
                            ImageView avatarView = new ImageView(this);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(90, 90);
                            params.setMargins(0, 0, 6, 0);
                            avatarView.setLayoutParams(params);
                            avatarView.setBackgroundResource(R.drawable.circle_background);
                            avatarView.setPadding(3, 3, 3, 3);

                            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                                Glide.with(this)
                                        .load(user.getAvatar())
                                        .circleCrop()
                                        .into(avatarView);
                            } else {
                                avatarView.setImageResource(R.drawable.ic_profile);
                            }

                            reactionAvatarsContainer.addView(avatarView);
                            avatarCount++;
                            break;
                        }
                    }
                }
            } else {
                reactionInfoText.setText(R.string.no_activity);
                reactionAvatarsContainer.removeAllViews();
                currentReactions.clear();
                Log.d("FeedPhotoFriendActivity", "No reactions for this photo");
            }
        });

        // Sự kiện nhấn vào reaction_info_container
        reactionInfoContainer.setOnClickListener(v -> {
            if (!currentReactions.isEmpty()) {
                showReactionDetailsDialog();
            }
        });

        // Các sự kiện nút
        btnShowAll.setOnClickListener(v -> {
            Intent intent = new Intent(FeedPhotoFriendActivity.this, FullPhotoActivity.class);
            if (selectedFriend != null) {
                intent.putExtra("selectedFriendId", selectedFriend.getUid());
                intent.putExtra("selectedFriendName", selectedFriend.getFullName());
                intent.putExtra("selectedFriendLastname", selectedFriend.getLastname());
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
            if (currentPhoto == null || currentPhoto.isAd()) {
                return;
            }
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
        // Sự kiện nhấn vào title để chọn bạn bè
        title.setOnClickListener(v -> {
            if (!friendList.isEmpty()) {
                adapterInitialized = false; // Đặt lại để adapter có thể được khởi tạo lại
                FriendDialog dialog = new FriendDialog(friendList, friend -> {
                    selectedFriend = friend;

                    if (friend == null) {
                        title.setText(R.string.all_friends);
                        sharedPhotoViewModel.getSharedPhotos(currentUserId).observe(this, this::updatePhotoFeedBySender);
                    } else if (getString(R.string.self).equals(friend.getLastname())) {
                        title.setText(R.string.self);
                        sharedPhotoViewModel.getMyPhotos(currentUserId).observe(this, this::updatePhotoFeedBySender);
                    } else {
                        title.setText(friend.getFullName());
                        sharedPhotoViewModel.getPhotosSharedWithMe(friend.getUid(), currentUserId).observe(this, this::updatePhotoFeedBySender);
                    }
                });
                dialog.show(getSupportFragmentManager(), "friendDialog");
            } else {
                Toast.makeText(this, R.string.no_friends, Toast.LENGTH_SHORT).show();
            }
        });

        emojiHeart.setOnClickListener(v -> {
            if (currentPhoto == null || currentPhoto.isAd()) return;
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "love"));
            updateUIBasedOnPhotoOwner();
        });

        emojiFire.setOnClickListener(v -> {
            if (currentPhoto == null || currentPhoto.isAd()) return;
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "fire"));
            updateUIBasedOnPhotoOwner();
        });

        emojiSmile.setOnClickListener(v -> {
            if (currentPhoto == null || currentPhoto.isAd()) return;
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "smile"));
            updateUIBasedOnPhotoOwner();
        });

        emojiAnotherReact.setOnClickListener(v -> {
            if (currentPhoto == null || currentPhoto.isAd()) return;
            animateEmojiClick(v);
            photoReactionViewModel.addReaction(new PhotoReaction(currentUserId, currentPhoto.getPhotoId(), "another_react"));
            updateUIBasedOnPhotoOwner();
        });
    }

    private void updatePhotoFeedAndScroll(List<User> users, List<Photo> photos, String targetPhotoId, boolean isPremium) {
        if (photos == null || photos.isEmpty()) {
            photoList.clear();
            viewPager2.setAdapter(null);
            Toast.makeText(this, "Không có ảnh nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        photoList.clear();
        if (!isPremium) {
            // Chèn quảng cáo sau mỗi 3 ảnh
            int photoCount = 0;
            for (int i = 0; i < photos.size(); i++) {
                photoList.add(photos.get(i));
                photoCount++;
                if (photoCount == 3 && i < photos.size() - 1) {
                    photoList.add(new Photo(AD_IMAGE_URL));
                    photoCount = 0;
                }
            }
        } else {
            photoList.addAll(photos);
        }

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
    }

    private void updatePhotoFeedBySender(List<Photo> photos) {
        if (photos == null || allUsers.isEmpty()) return;

        boolean isPremium = userViewModel.getCurrentUser().getValue() != null && userViewModel.getCurrentUser().getValue().isPremium();
        photoList.clear();
        if (!isPremium) {
            int photoCount = 0;
            for (int i = 0; i < photos.size(); i++) {
                photoList.add(photos.get(i));
                photoCount++;
                if (photoCount == 3 && i < photos.size() - 1) {
                    photoList.add(new Photo(AD_IMAGE_URL));
                    photoCount = 0;
                }
            }
        } else {
            photoList.addAll(photos);
        }

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

        if (currentPhoto.isAd()) {
            messageInputContainer.setVisibility(View.GONE);
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

    private void showMessageInputDialog() {
        if (currentPhoto == null || currentPhoto.isAd()) return;

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_message_input);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(params);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        EditText etMessageInput = dialog.findViewById(R.id.et_message_input);
        ImageView ivSendMessage = dialog.findViewById(R.id.iv_send_message);

        String photoOwnerLastname = "Người dùng";
        String receiverId = currentPhoto.getUserId();
        for (User user : allUsers) {
            if (user.getUid().equals(currentPhoto.getUserId())) {
                photoOwnerLastname = user.getLastname() != null && !user.getLastname().isEmpty()
                        ? user.getLastname()
                        : user.getUsername() != null && !user.getUsername().isEmpty()
                        ? user.getUsername()
                        : "Người dùng";
                break;
            }
        }

        etMessageInput.setHint(getString(R.string.reply_to, photoOwnerLastname));
        etMessageInput.requestFocus();

        etMessageInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    ivSendMessage.setBackgroundResource(R.drawable.circle_background);
                } else {
                    ivSendMessage.setBackgroundResource(R.drawable.circle_background_yellow);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        MessageViewModel messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        messageViewModel.getSendMessageSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Tin nhắn đã gửi!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        messageViewModel.getSendMessageError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi khi gửi tin nhắn: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        ivSendMessage.setOnClickListener(v -> {
            String messageContent = etMessageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                Message message = new Message(
                        currentUserId,
                        receiverId,
                        messageContent,
                        currentPhoto.getPhotoId()
                );
                messageViewModel.sendMessage(message);
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showReactionDetailsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reaction_details);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCanceledOnTouchOutside(true);

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(params);

        RecyclerView rvReactionList = dialog.findViewById(R.id.rv_reaction_list);
        rvReactionList.setLayoutManager(new LinearLayoutManager(this));
        ReactionAdapter adapter = new ReactionAdapter(this, currentReactions, allUsers);
        rvReactionList.setAdapter(adapter);

        dialog.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewPager2.setAdapter(null); // Dọn dẹp ViewPager2
    }
}