package com.example.locket.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import com.example.locket.ui.settings.SettingsActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các thành phần UI
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView avatar = findViewById(R.id.profile_avatar);
        TextView username = findViewById(R.id.profile_username);
        TextView email = findViewById(R.id.profile_email);
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);

        // Gán dữ liệu giả lập
        avatar.setImageResource(R.drawable.default_avatar); // Ảnh avatar
        username.setText("Luu Thanh Thuy");
        email.setText("ltthuy161@gmail.com");

        // Xử lý sự kiện nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện chỉnh sửa hồ sơ (mở màn hình chỉnh sửa)
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditNameActivity.class);
            startActivity(intent);
        });

        ImageView btnSettings = findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}