package com.example.locket.ui.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.locket.BaseActivity;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.auth.WelcomeActivity;
import com.example.locket.model.User;
import com.example.locket.ui.photo.UploadImageActivity;
import com.example.locket.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity {

    private ListView settingsList;
    private SettingsAdapter adapter;
    private ArrayList<SettingsItem> settingsItems;
    private ImageView iconPreview;
    private User currentUser;
    private UserViewModel userViewModel;

    private final ActivityResultLauncher<Intent> changeIconLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Xử lý kết quả từ ChangeIconActivity nếu cần
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                Log.d("SettingsActivity", "Đã load user: " + currentUser.getUsername());
            } else {
                Toast.makeText(this, R.string.user_load_error, Toast.LENGTH_SHORT).show();
            }
        });

        settingsList = findViewById(R.id.settings_list);

        settingsItems = new ArrayList<>();
        settingsItems.add(new SettingsItem(R.drawable.ic_logo, getString(R.string.change_icon)));
        settingsItems.add(new SettingsItem(R.drawable.ic_notification, getString(R.string.notifications)));
        settingsItems.add(new SettingsItem(R.drawable.ic_privacy, getString(R.string.privacy)));
        settingsItems.add(new SettingsItem(R.drawable.ic_about, getString(R.string.about)));
        settingsItems.add(new SettingsItem(R.drawable.ic_language, getString(R.string.change_language)));
        settingsItems.add(new SettingsItem(R.drawable.ic_logout, getString(R.string.logout)));

        adapter = new SettingsAdapter(this, settingsItems);
        settingsList.setAdapter(adapter);

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        settingsList.setOnItemClickListener((parent, view, position, id) -> {
            switch (position) {
                case 0: // Đổi icon ứng dụng
                    if (currentUser != null) {
                        if (currentUser.isPremium()) {
                            Intent intent = new Intent(SettingsActivity.this, ChangeIconActivity.class);
                            changeIconLauncher.launch(intent);
                        } else {
                            Intent intent = new Intent(SettingsActivity.this, UploadImageActivity.class);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.user_not_loaded, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 4: // Đổi ngôn ngữ
                    showLanguageDialog();
                    break;
                case 5:
                    // Đăng xuất
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(SettingsActivity.this, WelcomeActivity.class));
                    finish();
                    break;
                default:
                    break;
            }
        });
    }

    private void showLanguageDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_language)
                .setItems(new String[]{
                        getString(R.string.vietnamese),
                        getString(R.string.english)
                }, (dialog, which) -> {
                    if (which == 0) {
                        setLanguage("vi"); // Tiếng Việt
                    } else {
                        setLanguage("en"); // Tiếng Anh
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}