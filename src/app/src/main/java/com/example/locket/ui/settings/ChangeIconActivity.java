package com.example.locket.ui.settings;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.R;

public class ChangeIconActivity extends AppCompatActivity {
    private GridView iconGrid;
    private IconAdapter iconAdapter;

    private static class IconData {
        int iconResId;
        String aliasName;

        IconData(int iconResId, String aliasName) {
            this.iconResId = iconResId;
            this.aliasName = aliasName;
        }
    }

    private final IconData[] iconDataList = {
            new IconData(R.drawable.ic_app_11, "com.example.locket.MainActivityIcon1"),
            new IconData(R.drawable.ic_app_12, "com.example.locket.MainActivityIcon2"),
            new IconData(R.drawable.ic_app_13, "com.example.locket.MainActivityIcon3"),
            new IconData(R.drawable.ic_app_14, "com.example.locket.MainActivityIcon4"),
            new IconData(R.drawable.ic_app_21, "com.example.locket.MainActivityIcon5"),
            new IconData(R.drawable.ic_app_22, "com.example.locket.MainActivityIcon6"),
            new IconData(R.drawable.ic_app_23, "com.example.locket.MainActivityIcon7"),
            new IconData(R.drawable.ic_app_24, "com.example.locket.MainActivityIcon8"),
            new IconData(R.drawable.ic_app_31, "com.example.locket.MainActivityIcon9"),
            new IconData(R.drawable.ic_app_32, "com.example.locket.MainActivityIcon10"),
            new IconData(R.drawable.ic_app_33, "com.example.locket.MainActivityIcon11"),
            new IconData(R.drawable.ic_app_34, "com.example.locket.MainActivityIcon12")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_icon);

        iconGrid = findViewById(R.id.icon_grid);
        ImageView btnBack = findViewById(R.id.btn_back);

        // Tạo danh sách icon từ iconDataList
        int[] iconList = new int[iconDataList.length];
        for (int i = 0; i < iconDataList.length; i++) {
            iconList[i] = iconDataList[i].iconResId;
        }

        // Gán Adapter cho GridView
        iconAdapter = new IconAdapter(this, iconList);
        iconGrid.setAdapter(iconAdapter);
        Log.d("ChangeIcon", "GridView adapter set with " + iconList.length + " icons");

        // Xử lý nút Back
        btnBack.setOnClickListener(view -> {
            Log.d("ChangeIcon", "Back button clicked");
            finish();
        });

        // Xử lý khi người dùng chọn icon
        iconGrid.setOnItemClickListener((parent, view, position, id) -> {
            Log.d("ChangeIcon", "Icon clicked at position: " + position);
            try {
                iconAdapter.setSelectedPosition(position);
                String alias = iconDataList[position].aliasName;
                Log.d("ChangeIcon", "Selected alias: " + alias);
                changeAppIcon(alias);
                Toast.makeText(this, "Icon đã thay đổi. Có thể cần khởi động lại launcher.", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Log.e("ChangeIcon", "Error on icon click: " + e.getMessage());
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changeAppIcon(String aliasName) {
        PackageManager pm = getPackageManager();
        try {
            // Disable tất cả alias
            for (IconData iconData : iconDataList) {
                ComponentName component = new ComponentName(getPackageName(), iconData.aliasName);
                pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
                Log.d("ChangeIcon", "Disabled alias: " + iconData.aliasName);
                int state = pm.getComponentEnabledSetting(component);
                Log.d("ChangeIcon", "State of " + iconData.aliasName + " after disable: " + state);
            }

            // Enable alias được chọn
            ComponentName selectedComponent = new ComponentName(getPackageName(), aliasName);
            pm.setComponentEnabledSetting(
                    selectedComponent,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );
            Log.d("ChangeIcon", "Enabled alias: " + aliasName);
            int state = pm.getComponentEnabledSetting(selectedComponent);
            Log.d("ChangeIcon", "State of " + aliasName + " after enable: " + state);
        } catch (Exception e) {
            Log.e("ChangeIcon", "Error changing icon: " + e.getMessage());
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}