package com.example.locket.ui.photo;

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

                // Tải ảnh chia sẻ ban đầu
                sharedPhotoViewModel.getSharedPhotos(userId).observe(this, photos -> {
                    if (photos != null && !photos.isEmpty()) {
                        imageAdapter.updatePhotos(photos);
                    } else {
                        imageAdapter.updatePhotos(new ArrayList<>());
                        Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
                    }
                });
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
        });

        sharedPhotoViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
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
                        sharedPhotoViewModel.getSharedPhotos(userId).observe(this, photos -> {
                            imageAdapter.updatePhotos(photos != null ? photos : new ArrayList<>());
                            if (photos == null || photos.isEmpty()) {
                                Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        title.setText(selectedFriend.getFullName());
                        sharedPhotoViewModel
                                .getPhotosSharedWithMe(selectedFriend.getUid(), userId) // 👈 gọi hàm mới
                                .observe(this, photos -> {
                                    imageAdapter.updatePhotos(photos != null ? photos : new ArrayList<>());
                                    if (photos == null || photos.isEmpty()) {
                                        Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
                dialog.show(getSupportFragmentManager(), "friendPopup");
            } else {
                Toast.makeText(this, "Không có bạn bè nào", Toast.LENGTH_SHORT).show();
            }
        });
    }
}