package com.example.locket.ui.photo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.model.User;
import com.example.locket.utils.NavigationUtils;
import com.example.locket.viewmodel.SharedPhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DetailPhotoFriendActivity extends AppCompatActivity {
    private ImageView btnChat, photo, userAvatar, btnShowAll;
    private TextView userName, postTime, caption;
    private SharedPhotoViewModel sharedPhotoViewModel;
    private UserViewModel userViewModel;
    private List<User> allUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photofriend_detail);

        btnChat = findViewById(R.id.btn_chat);
        photo = findViewById(R.id.photo);
        userAvatar = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        btnShowAll = findViewById(R.id.btn_showall);
        postTime = findViewById(R.id.post_time);
        caption = findViewById(R.id.caption);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);

        userViewModel.getAllUsers().observe(this, users -> {
            if (users != null) {
                allUsers = users;
            }
        });

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                loadLatestPhoto(user.getUid());
            } else {
                Log.e("DetailPhotoFriend", "Không tìm thấy user!");
            }
        });

        btnShowAll.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPhotoFriendActivity.this, FullPhotoActivity.class);
            startActivity(intent);
        });

        NavigationUtils.setChatButtonClickListener(btnChat, this);
    }

    private void loadLatestPhoto(String userId) {
        sharedPhotoViewModel.getSharedPhotos(userId).observe(this, photos -> {
            if (photos != null && !photos.isEmpty()) {
                Photo latestPhoto = photos.get(0);

                String username = findUsernameByUid(latestPhoto.getUserId());
                userName.setText(username);

                caption.setText(latestPhoto.getCaption());

                postTime.setText(formatTimeDifference(latestPhoto.getCreatedAt()));

                Glide.with(this).load(latestPhoto.getImageUrl()).into(photo);
            } else {
                Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String findUsernameByUid(String uid) {
        if (allUsers != null) {
            for (User user : allUsers) {
                if (user.getUid().equals(uid)) {
                    return user.getUsername();
                }
            }
        }
        return "Không xác định";
    }

    private String formatTimeDifference(Date createdAt) {
        if (createdAt == null) return "Vừa xong";

        long diffMillis = new Date().getTime() - createdAt.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
        long weeks = days / 7;

        if (minutes < 60) {
            return minutes + "m";
        } else if (hours < 24) {
            return hours + "h";
        } else if (days < 7) {
            return days + "d";
        } else {
            return weeks + "w";
        }
    }
}
