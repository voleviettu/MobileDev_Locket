package com.example.locket.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private ListView settingsList;
    private SettingsAdapter adapter;
    private ArrayList<SettingsItem> settingsItems;
    private ImageView iconPreview; // Nếu có hiển thị icon sau khi chọn

    // Dùng ActivityResultLauncher thay vì startActivityForResult
    private final ActivityResultLauncher<Intent> changeIconLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {

            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                if (position == 0) { // "Đổi icon ứng dụng"
                    Intent intent = new Intent(SettingsActivity.this, ChangeIconActivity.class);
                    changeIconLauncher.launch(intent); // 🔥 Dùng cách mới để mở Activity
                }
            }
        });
    }
}