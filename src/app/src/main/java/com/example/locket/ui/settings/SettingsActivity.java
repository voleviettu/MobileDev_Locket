package com.example.locket.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private ListView settingsList;
    private SettingsAdapter adapter;
    private ArrayList<SettingsItem> settingsItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsList = findViewById(R.id.settings_list);
        settingsItems = new ArrayList<>();

        // Thêm các mục cài đặt
        settingsItems.add(new SettingsItem(R.drawable.ic_logo, "Đổi icon ứng dụng"));
        settingsItems.add(new SettingsItem(R.drawable.ic_notification, "Thông báo"));
        settingsItems.add(new SettingsItem(R.drawable.ic_privacy, "Quyền riêng tư"));
        settingsItems.add(new SettingsItem(R.drawable.ic_about, "Giới thiệu"));

        adapter = new SettingsAdapter(this, settingsItems);
        settingsList.setAdapter(adapter);


    }
}