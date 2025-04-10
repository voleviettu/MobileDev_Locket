package com.example.locket.ui.photo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.model.User;
import com.example.locket.ui.settings.ImageAdapter;
import com.example.locket.utils.NavigationUtils;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.SharedPhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

public class FullPhotoActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private SharedPhotoViewModel sharedPhotoViewModel;
    private FriendViewModel friendViewModel;
    private User currentUser;
    private String userId;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Photo> photoList;
    private ImageView btnChat, btnCapture;
    private TextView title;
    private List<User> friendList = new ArrayList<>(); // Khởi tạo mặc định để tránh null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullphoto);

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            Log.e("FullPhotoActivity", "Google Play Services không khả dụng");
            Toast.makeText(this, "Google Play Services không khả dụng", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);

        recyclerView = findViewById(R.id.recyclerView);
        btnChat = findViewById(R.id.btn_chat);
        btnCapture = findViewById(R.id.btn_capture);
        title = findViewById(R.id.tv_title);
        photoList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, photoList);

        imageAdapter.setOnPhotoClickListener(photo -> {
            Intent intent = new Intent(FullPhotoActivity.this, FeedPhotoFriendActivity.class);
            intent.putExtra("photoId", photo.getPhotoId());

            String selectedFriendId = null;
            String selectedFriendName = null;
            String selectedFriendLastname = null;

            if (!title.getText().toString().equals("Tất cả bạn bè")) {
                for (User u : friendList) {
                    if (u.getFullName().equals(title.getText().toString())) {
                        selectedFriendId = u.getUid();
                        selectedFriendName = u.getFullName();
                        selectedFriendLastname = u.getLastname();
                        break;
                    }
                }

                intent.putExtra("selectedFriendId", selectedFriendId);
                intent.putExtra("selectedFriendName", selectedFriendName);
                intent.putExtra("selectedFriendLastname", selectedFriendLastname);
            }

            startActivity(intent);
        });


        recyclerView.setAdapter(imageAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Quan sát currentUser để lấy userId
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                userId = user.getUid();
                // Tải danh sách bạn bè
                friendViewModel.loadFriends(userId);

                String selectedFriendId = getIntent().getStringExtra("selectedFriendId");
                String selectedFriendName = getIntent().getStringExtra("selectedFriendName");
                String selectedFriendLastname = getIntent().getStringExtra("selectedFriendLastname");

                // Tải ảnh chia sẻ ban đầu
                if (selectedFriendId != null) {
                    if ("Bạn".equals(selectedFriendLastname)) {
                        title.setText("Bạn");
                        sharedPhotoViewModel.getMyPhotos(userId).observe(this, photos -> {
                            if (photos != null) {
                                imageAdapter.updatePhotos(photos);
                            }
                        });
                    } else {
                        title.setText(selectedFriendName);
                        sharedPhotoViewModel.getPhotosSharedWithMe(selectedFriendId, userId).observe(this, photos -> {
                            if (photos != null) {
                                imageAdapter.updatePhotos(photos);
                            }
                        });
                    }
                } else {
                    title.setText("Tất cả bạn bè");
                    sharedPhotoViewModel.getSharedPhotos(userId).observe(this, photos -> {
                        if (photos != null && !photos.isEmpty()) {
                            imageAdapter.updatePhotos(photos);
                        } else {
                            imageAdapter.updatePhotos(new ArrayList<>());
                            Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                ImageView btnProfile = findViewById(R.id.btn_profile);
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatar())
                            .circleCrop()
                            .into(btnProfile);
                } else {
                    btnProfile.setImageResource(R.drawable.ic_profile);
                }
            } else {
                Log.e("FullPhotoActivity", "Không tìm thấy user!");
            }
        });

        // Quan sát danh sách bạn bè
        friendViewModel.getFriends().observe(this, friends -> {
            if (friends != null && !friends.isEmpty()) {
                friendList.clear();
                friendList.addAll(friends);
                Log.d("FullPhotoActivity", "Đã tải " + friendList.size() + " bạn bè");
            } else {
                friendList.clear();
                Log.d("FullPhotoActivity", "Không có bạn bè nào được tải");
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

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int width = recyclerView.getWidth() / 3;
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                ViewGroup.LayoutParams params = child.getLayoutParams();
                params.height = width; // Đảm bảo ảnh vuông
                child.setLayoutParams(params);
            }
        });

        NavigationUtils.setChatButtonClickListener(btnChat, this);
        NavigationUtils.setCaptureButtonClickListener(btnCapture, this);

        // Xử lý sự kiện nhấp vào title
        title.setOnClickListener(v -> {
            if (!friendList.isEmpty()) {
                FriendDialog dialog = new FriendDialog(friendList, selectedFriend -> {
                    if (selectedFriend == null) {
                        title.setText("Tất cả bạn bè");
                        sharedPhotoViewModel.getSharedPhotos(userId); // Tải tất cả ảnh gửi tới người dùng
                    } else if (selectedFriend.getLastname().equals("Bạn")) {
                        title.setText("Bạn");
                        sharedPhotoViewModel.getMyPhotos(userId); // Tải ảnh người dùng đã đăng
                    } else {
                        title.setText(selectedFriend.getFullName());
                        sharedPhotoViewModel.getPhotosSharedWithMe(selectedFriend.getUid(), userId); // Tải ảnh từ bạn bè gửi tới
                    }
                });
                dialog.show(getSupportFragmentManager(), "friendPopup");
            } else {
                Toast.makeText(this, "Không có bạn bè nào", Toast.LENGTH_SHORT).show();
            }
        });
    }
}