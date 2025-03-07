package com.example.locket.ui.photo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.R;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class UploadImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image); // Tạo file layout activity_upload_image.xml

        TextView noThanks = findViewById(R.id.text_no_tks);
        noThanks.setOnClickListener(v -> finish()); // Đóng Activity hiện tại

    }


}