package com.example.locket.ui.settings;

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

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Chat> chatList;

    public ChatAdapter(List<Chat> chatList) {
        this.chatList = chatList;
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

        // Nếu không có tin nhắn (message là "Chưa có câu trả lời nào!"), ẩn tv_time
        if ("Chưa có câu trả lời nào!".equals(chat.getMessage())) {
            holder.tvTime.setVisibility(View.GONE);
        } else {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(chat.getTime());
        }

        // Load ảnh đại diện bằng Glide
        if (chat.getAvatarUrl() != null && !chat.getAvatarUrl().isEmpty()) {
            Glide.with(holder.ivProfile.getContext())
                    .load(chat.getAvatarUrl())
                    .circleCrop()
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile);
        }
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