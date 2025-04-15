package com.example.locket.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.User;
import com.example.locket.ui.photo.UploadImageActivity;
import com.example.locket.viewmodel.UserViewModel;


import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private ListView settingsList;
    private SettingsAdapter adapter;
    private ArrayList<SettingsItem> settingsItems;
    private ImageView iconPreview; // Nếu có hiển thị icon sau khi chọn
    private User currentUser;
    private UserViewModel userViewModel;

    // Dùng ActivityResultLauncher thay vì startActivityForResult
    private final ActivityResultLauncher<Intent> changeIconLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                // Có thể cập nhật giao diện nếu cần, ví dụ avatar, username...
                Log.d("SettingsActivity", "Đã load user: " + currentUser.getUsername());
            } else {
                Toast.makeText(this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        settingsList = findViewById(R.id.settings_list);

        settingsItems = new ArrayList<>();
        settingsItems.add(new SettingsItem(R.drawable.ic_logo, "Đổi icon ứng dụng"));
        settingsItems.add(new SettingsItem(R.drawable.ic_notification, "Thông báo"));
        settingsItems.add(new SettingsItem(R.drawable.ic_privacy, "Quyền riêng tư"));
        settingsItems.add(new SettingsItem(R.drawable.ic_about, "Giới thiệu"));

        adapter = new SettingsAdapter(this, settingsItems);
        settingsList.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        settingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "Đổi icon ứng dụng
                    if (currentUser != null) {
                        if (currentUser.isPremium()) {
                            // Nếu là gold user thì mở thư viện ảnh
                            Intent intent = new Intent(SettingsActivity.this, ChangeIconActivity.class);
                            changeIconLauncher.launch(intent); // 🔥 Dùng cách mới để mở Activity
                        } else {
                            // Nếu là user thường thì chuyển sang UploadImageActivity
                            Intent intent = new Intent(SettingsActivity.this, UploadImageActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, "Chưa load thông tin user, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}