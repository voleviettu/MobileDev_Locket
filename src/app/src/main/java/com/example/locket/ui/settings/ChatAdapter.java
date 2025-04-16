package com.example.locket.ui.settings;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.Chat;
import com.example.locket.ui.chat.ChatDetailActivity;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;
    private String currentUserId; // Thêm để truyền vào ChatDetailActivity

    public ChatAdapter(List<Chat> chatList, String currentUserId) {
        this.chatList = chatList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.tvName.setText(chat.getName());
        holder.tvMessage.setText(chat.getMessage());

        if ("Chưa có câu trả lời nào!".equals(chat.getMessage())) {
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(chat.getTime());
        }

        if (chat.getAvatarUrl() != null && !chat.getAvatarUrl().isEmpty()) {
            Glide.with(holder.ivProfile.getContext())
                    .load(chat.getAvatarUrl())
                    .circleCrop()
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile);
        }

        // Sự kiện nhấn để mở ChatDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ChatDetailActivity.class);
            intent.putExtra("currentUserId", currentUserId);
            intent.putExtra("friendId", chat.getFriendId());
            intent.putExtra("friendName", chat.getName());
            intent.putExtra("friendAvatar", chat.getAvatarUrl());
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile, ivForward;
        TextView tvName, tvMessage, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            ivForward = itemView.findViewById(R.id.iv_forward);
            tvName = itemView.findViewById(R.id.tv_name);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}