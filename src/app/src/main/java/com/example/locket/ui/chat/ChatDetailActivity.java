package com.example.locket.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.Message;
import com.example.locket.ui.settings.MessageAdapter;
import com.example.locket.viewmodel.MessageViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageView ivSendMessage, ivFriendAvatar;
    private TextView tvFriendName;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private MessageViewModel messageViewModel;
    private String currentUserId, friendId, friendName, friendAvatar;
    private ListenerRegistration messagesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("currentUserId");
        friendId = intent.getStringExtra("friendId");
        friendName = intent.getStringExtra("friendName");
        friendAvatar = intent.getStringExtra("friendAvatar");

        // Khởi tạo views
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        ivSendMessage = findViewById(R.id.iv_send_message);
        ivFriendAvatar = findViewById(R.id.iv_friend_avatar);
        tvFriendName = findViewById(R.id.tv_friend_name);

        tvFriendName.setText(friendName);
        if (friendAvatar != null && !friendAvatar.isEmpty()) {
            Glide.with(this)
                    .load(friendAvatar)
                    .circleCrop()
                    .into(ivFriendAvatar);
        } else {
            ivFriendAvatar.setImageResource(R.drawable.ic_profile);
        }

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // Thiết lập RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUserId, friendAvatar); // Truyền friendAvatar
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(messageAdapter);

        // Khởi tạo ViewModel
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        // Lắng nghe tin nhắn từ Firestore
        listenForMessages();

        // Quan sát kết quả gửi tin nhắn
        messageViewModel.getSendMessageSuccess().observe(this, success -> {
            if (success != null && success) {
                etMessageInput.setText("");
            }
        });

        messageViewModel.getSendMessageError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, "Lỗi khi gửi tin nhắn: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Sự kiện gửi tin nhắn
        ivSendMessage.setOnClickListener(v -> {
            String messageContent = etMessageInput.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                Message message = new Message(
                        currentUserId,
                        friendId,
                        messageContent,
                        null // photoId để null vì đây là tin nhắn văn bản
                );
                messageViewModel.sendMessage(message);
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        messagesListener = db.collection("messages")
                .whereIn("senderId", java.util.Arrays.asList(currentUserId, friendId))
                .whereIn("receiverId", java.util.Arrays.asList(currentUserId, friendId))
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi khi tải tin nhắn: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null) {
                        messageList.clear();
                        for (var doc : snapshot.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            messageList.add(message);
                        }
                        messageAdapter.notifyDataSetChanged();
                        // Cuộn xuống tin nhắn mới nhất
                        if (!messageList.isEmpty()) {
                            rvChatMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messagesListener != null) {
            messagesListener.remove();
        }
    }
}