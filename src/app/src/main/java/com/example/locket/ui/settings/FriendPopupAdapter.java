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
import com.example.locket.model.User;

import java.util.List;

public class FriendPopupAdapter extends RecyclerView.Adapter<FriendPopupAdapter.FriendViewHolder> {

    private final List<User> friends;
    private final OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(User friend);
    }

    public FriendPopupAdapter(List<User> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_popup, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.txtName.setText(friend.getFullName());

        String avatar = friend.getAvatar();

        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.equals("ic_friends")) {
                holder.imgAvatar.setImageResource(R.drawable.ic_friends);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(avatar)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .circleCrop()
                        .into(holder.imgAvatar);
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        }
        holder.itemView.setOnClickListener(v -> listener.onFriendClick(friend));
    }


    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView txtName;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
