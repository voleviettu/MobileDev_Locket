package com.example.locket.ui.friend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.locket.R;
import com.example.locket.model.User;
import java.net.URL;
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
            if (friend == null) return;

            textViewFriendName.setText(friend.getFullName());

            // Avatar hiển thị từ URL hoặc ảnh mặc định
            String avatarUrl = friend.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                new Thread(() -> {
                    try {
                        URL url = new URL(avatarUrl);
                        Bitmap bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        ((View) profileAvatar).post(() -> profileAvatar.setImageBitmap(bitmap));
                    } catch (Exception e) {
                        e.printStackTrace();
                        ((View) profileAvatar).post(() -> profileAvatar.setImageResource(R.drawable.default_avatar));
                    }
                }).start();
            } else {
                profileAvatar.setImageResource(R.drawable.default_avatar);
            }

            buttonRemoveFriend.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClicked(friend);
                }
            });
        }

    }
}