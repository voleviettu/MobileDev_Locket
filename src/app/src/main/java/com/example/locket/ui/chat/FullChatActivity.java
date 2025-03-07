package com.example.locket.ui.chat;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.model.Chat;
import com.example.locket.ui.settings.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class FullChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        chatList.add(new Chat("Minh Ngoc", "😊", "2h", R.drawable.ic_profile));
        chatList.add(new Chat("Khue Gia", "Định cát mà không biết hơ...", "2d", R.drawable.ic_profile));
        chatList.add(new Chat("Nhật Nhật", "Hong, căng ơn 🥴", "3d", R.drawable.ic_profile));
        chatList.add(new Chat("Rắn", "😍", "3d", R.drawable.ic_profile));
        chatList.add(new Chat("wan min", "em đi vs lớp em", "4d", R.drawable.ic_profile));
        chatList.add(new Chat("Thien", "💛", "5d", R.drawable.ic_profile));

        chatAdapter = new ChatAdapter(chatList);
        recyclerView.setAdapter(chatAdapter);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
