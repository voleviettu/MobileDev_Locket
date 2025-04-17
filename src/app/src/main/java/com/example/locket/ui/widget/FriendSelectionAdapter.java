package com.example.locket.ui.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;
import java.util.List;

public class FriendSelectionAdapter extends RecyclerView.Adapter<FriendSelectionAdapter.FriendViewHolder> {

    private final Context context;
    private final List<User> friendList; // Bao gồm cả "Tất cả bạn bè" và "Bạn"
    private final OnFriendSelectedListener listener;

    public interface OnFriendSelectedListener {
        void onFriendSelected(User friend); // friend == null nghĩa là "Tất cả bạn bè"
    }

    public FriendSelectionAdapter(Context context, List<User> friendList, OnFriendSelectedListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_selection, parent, false); // Tạo layout item_friend_selection
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bind(friend);
        holder.itemView.setOnClickListener(v -> listener.onFriendSelected(friend));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName;

        FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_friend_avatar); // ID trong item_friend_selection.xml
            tvName = itemView.findViewById(R.id.tv_friend_name);     // ID trong item_friend_selection.xml
        }

        void bind(User friend) {
            // ID "ALL" và "SELF" là quy ước
            if ("ALL".equals(friend.getUid())) {
                tvName.setText("Tất cả bạn bè");
                ivAvatar.setImageResource(R.drawable.ic_logo); // Icon phù hợp
            } else if ("SELF".equals(friend.getUid())) {
                tvName.setText("Chỉ mình bạn"); // Hoặc "Bạn" như cũ
                Glide.with(itemView.getContext())
                        .load(friend.getAvatar())
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                tvName.setText(friend.getFullName());
                Glide.with(itemView.getContext())
                        .load(friend.getAvatar())
                        .placeholder(R.drawable.ic_profile)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }
    }
}