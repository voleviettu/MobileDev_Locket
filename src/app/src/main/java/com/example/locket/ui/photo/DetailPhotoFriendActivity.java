package com.example.locket.ui.photo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.R;
import com.example.locket.utils.NavigationUtils;

public class DetailPhotoFriendActivity extends AppCompatActivity {

    private ImageView btnProfile, btnChat, photo, userAvatar, btnShowAll, btnCapture, btnOption;
    private TextView tvTitle, userName, postTime;
    private EditText inputMessage, caption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photofriend_detail);

        btnProfile = findViewById(R.id.btn_profile);
        btnChat = findViewById(R.id.btn_chat);
        photo = findViewById(R.id.photo);
        userAvatar = findViewById(R.id.user_avatar);
        btnShowAll = findViewById(R.id.btn_showall);
        btnCapture = findViewById(R.id.btn_capture);
        btnOption = findViewById(R.id.btn_option);

        tvTitle = findViewById(R.id.tv_title);
        caption = findViewById(R.id.caption);
        userName = findViewById(R.id.user_name);
        postTime = findViewById(R.id.post_time);

        inputMessage = findViewById(R.id.input_message);

        tvTitle.setText("Tất cả bạn bè");
        userName.setText("Rần");
        postTime.setText("2m");
        caption.setText("Caption");

        btnShowAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailPhotoFriendActivity.this, FullPhotoActivity.class);
                startActivity(intent);
            }
        });
        NavigationUtils.setChatButtonClickListener(btnChat, this);
    }
}