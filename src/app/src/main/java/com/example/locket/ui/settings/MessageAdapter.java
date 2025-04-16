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
import com.example.locket.data.PhotoRepository;
import com.example.locket.model.Message;
import com.example.locket.model.Photo;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private List<Message> messageList;
    private String currentUserId;
    private String friendAvatar;
    private PhotoRepository photoRepository;

    public MessageAdapter(List<Message> messageList, String currentUserId, String friendAvatar) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.friendAvatar = friendAvatar;
        this.photoRepository = new PhotoRepository(); // Khởi tạo PhotoRepository
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = viewType == VIEW_TYPE_SENT ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Hiển thị avatar cho tin nhắn nhận
        if (holder.viewType == VIEW_TYPE_RECEIVED) {
            if (friendAvatar != null && !friendAvatar.isEmpty()) {
                Glide.with(holder.ivAvatar.getContext())
                        .load(friendAvatar)
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_profile);
            }
        }

        // Kiểm tra nếu có photoId (phản hồi ảnh từ Feed)
        if (message.getPhotoId() != null && !message.getPhotoId().isEmpty()) {
            holder.ivPhoto.setVisibility(View.VISIBLE);
            // Sử dụng PhotoRepository để lấy URL ảnh
            photoRepository.getPhotoById(message.getPhotoId(), new PhotoRepository.FirestoreCallback<Photo>() {
                @Override
                public void onSuccess(Photo photo) {
                    if (photo != null && photo.getImageUrl() != null && !photo.getImageUrl().isEmpty()) {
                        Glide.with(holder.ivPhoto.getContext())
                                .load(photo.getImageUrl())
                                .centerCrop()
                                .into(holder.ivPhoto);
                    } else {
                        holder.ivPhoto.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    holder.ivPhoto.setVisibility(View.GONE);
                }
            });

            holder.tvMessageContent.setVisibility(View.VISIBLE);
            holder.tvMessageContent.setText(message.getContent());
        } else {
            // Nếu không có photoId, chỉ hiển thị nội dung tin nhắn
            holder.ivPhoto.setVisibility(View.GONE);
            holder.tvMessageContent.setVisibility(View.VISIBLE);
            holder.tvMessageContent.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent;
        ImageView ivAvatar, ivPhoto;
        int viewType;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            if (viewType == VIEW_TYPE_RECEIVED) {
                ivAvatar = itemView.findViewById(R.id.iv_avatar);
            }
        }
    }
}