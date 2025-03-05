package com.example.locket.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;

public class ChangeIconActivity extends AppCompatActivity {
    private GridView iconGrid;
    private IconAdapter iconAdapter;

    // Danh sách icon (thay bằng icon thực tế)
    private int[] iconList = {
            R.drawable.ic_app_11,
            R.drawable.ic_app_12,
            R.drawable.ic_app_13,
            R.drawable.ic_app_14,
            R.drawable.ic_app_21,
            R.drawable.ic_app_22,
            R.drawable.ic_app_23,
            R.drawable.ic_app_24,
            R.drawable.ic_app_31,
            R.drawable.ic_app_32,
            R.drawable.ic_app_33,
            R.drawable.ic_app_34
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_icon);

        iconGrid = findViewById(R.id.icon_grid);
        ImageView btnBack = findViewById(R.id.btn_back);

        // Gán Adapter cho GridView
        iconAdapter = new IconAdapter(this, iconList);
        iconGrid.setAdapter(iconAdapter);

        // Xử lý khi nhấn nút back
        btnBack.setOnClickListener(view -> finish());

        // Xử lý khi chọn icon
        iconGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                iconAdapter.setSelectedPosition(position); // Cập nhật UI
                returnSelectedIcon(position);
            }
        });
    }

    private void returnSelectedIcon(int position) {
        Intent intent = new Intent();
        intent.putExtra("selected_icon", iconList[position]);
        setResult(RESULT_OK, intent);
        finish(); // Đóng màn hình chọn icon
    }
}