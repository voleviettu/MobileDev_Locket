package com.example.locket.ui.chat;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Chat;
import com.example.locket.model.Message;
import com.example.locket.model.User;
import com.example.locket.ui.settings.ChatAdapter;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.MessageViewModel;
import com.example.locket.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FullChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private UserViewModel userViewModel;
    private FriendViewModel friendViewModel;
    private MessageViewModel messageViewModel;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList, currentUserId); // Truyền currentUserId
        recyclerView.setAdapter(chatAdapter);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // Lấy currentUserId
        userViewModel.getCurrentUser().observe(this, currentUser -> {
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
                // Cập nhật adapter với currentUserId
                chatAdapter = new ChatAdapter(chatList, currentUserId);
                recyclerView.setAdapter(chatAdapter);
                // Load danh sách bạn bè
                friendViewModel.loadFriends(currentUserId);
            }
        });

        // Quan sát danh sách bạn bè
        friendViewModel.getFriends().observe(this, friends -> {
            if (friends != null && !friends.isEmpty()) {
                chatList.clear();
                for (User friend : friends) {
                    Chat chat = new Chat(
                            friend.getUid(),
                            friend.getFullName(),
                            "Chưa có câu trả lời nào!",
                            "",
                            friend.getAvatar()
                    );
                    chatList.add(chat);
                }
                chatAdapter.notifyDataSetChanged();

                for (User friend : friends) {
                    messageViewModel.getLatestMessage(currentUserId, friend.getUid(), friend.getUid());
                }
            }
        });

        // Quan sát tin nhắn gần nhất
        messageViewModel.getLatestMessages().observe(this, messagesMap -> {
            if (messagesMap == null) return;

            updateChatList(messagesMap);
        });

        // Quan sát sự kiện gửi tin nhắn thành công
        messageViewModel.getSendMessageSuccess().observe(this, success -> {
            if (success != null && success) {
                // Lắng nghe friendId của cuộc trò chuyện vừa gửi
                messageViewModel.getLatestFriendId().observe(this, friendId -> {
                    if (friendId != null) {
                        messageViewModel.getLatestMessage(currentUserId, friendId, friendId);
                    }
                });
            }
        });

        messageViewModel.getSendMessageError().observe(this, error -> {
            if (error != null) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void updateChatList(Map<String, Message> messagesMap) {
        for (int i = 0; i < chatList.size(); i++) {
            Chat chat = chatList.get(i);
            Message message = messagesMap.get(chat.getFriendId());
            if (message != null) {
                String displayMessage = message.getContent();
                String displayTime = calculateTimeAgo(message.getCreatedAt());

                Chat updatedChat = new Chat(
                        chat.getFriendId(),
                        chat.getName(),
                        displayMessage,
                        displayTime,
                        chat.getAvatarUrl()
                );
                updatedChat.setCreatedAt(message.getCreatedAt()); // Lưu createdAt
                chatList.set(i, updatedChat);
            }
        }

        // Sắp xếp dựa trên createdAt
        Collections.sort(chatList, (chat1, chat2) -> {
            if ("Chưa có câu trả lời nào!".equals(chat1.getMessage())) return 1;
            if ("Chưa có câu trả lời nào!".equals(chat2.getMessage())) return -1;
            if (chat1.getCreatedAt() == null) return 1;
            if (chat2.getCreatedAt() == null) return -1;
            return chat2.getCreatedAt().compareTo(chat1.getCreatedAt()); // Sắp xếp theo thời gian thực
        });

        chatAdapter.notifyDataSetChanged();
    }

    private String calculateTimeAgo(Date createdAt) {
        if (createdAt == null) return "";
        long now = System.currentTimeMillis();
        long messageTime = createdAt.getTime();
        long diffInMillis = now - messageTime;

        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
        if (diffInSeconds < 60) {
            return "Vừa xong"; // Hiển thị "Vừa xong" nếu dưới 1 phút
        }

        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        if (diffInMinutes < 60) {
            return diffInMinutes + "m";
        }

        long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
        if (diffInHours < 24) {
            return diffInHours + "h";
        }

        long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);
        return diffInDays + "d";
    }
}