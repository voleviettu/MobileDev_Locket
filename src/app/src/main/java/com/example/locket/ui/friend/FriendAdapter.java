package com.example.locket.ui.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<User> friendList;
    private final OnRemoveFriendListener removeListener;

    // Interface for callback
    public interface OnRemoveFriendListener {
        void onRemoveClicked(User friend);
    }

    public FriendAdapter(List<User> friendList, OnRemoveFriendListener removeListener) {
        this.friendList = friendList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.bind(friend, removeListener);
    }

    @Override
    public int getItemCount() {
        return friendList == null ? 0 : friendList.size();
    }

    // --- ViewHolder Class ---
    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profileAvatar;
        TextView textViewFriendName;
        ImageButton buttonRemoveFriend;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profileAvatar = itemView.findViewById(R.id.profile_avatar);
            textViewFriendName = itemView.findViewById(R.id.textViewFriendName);
            buttonRemoveFriend = itemView.findViewById(R.id.buttonRemoveFriend);
        }

        public void bind(final User friend, final FriendAdapter.OnRemoveFriendListener listener) {
            if (friend == null) return; // Check null safety
            textViewFriendName.setText(friend.getFullName());

            Glide.with(itemView.getContext())
                    .load(friend.getAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileAvatar);

            buttonRemoveFriend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClicked(friend);
                }
            });
        }
    }
}