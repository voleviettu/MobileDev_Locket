package com.example.locket.ui.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private final List<User> requestList;
    private final OnAcceptRequestListener acceptListener;
    private final OnDeclineRequestListener declineListener;

    // Interfaces for callbacks
    public interface OnAcceptRequestListener {
        void onAcceptClicked(User requester);
    }
    public interface OnDeclineRequestListener {
        void onDeclineClicked(User requester);
    }

    public FriendRequestAdapter(List<User> requestList, OnAcceptRequestListener acceptListener, OnDeclineRequestListener declineListener) {
        this.requestList = requestList;
        this.acceptListener = acceptListener;
        this.declineListener = declineListener;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_friend_request, parent, false);
        return new FriendRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        User requester = requestList.get(position);
        holder.bind(requester, acceptListener, declineListener);
    }

    @Override
    public int getItemCount() {
        return requestList == null ? 0 : requestList.size();
    }

    // --- ViewHolder Class ---
    static class FriendRequestViewHolder extends RecyclerView.ViewHolder {
        ImageView profileAvatar;
        TextView textViewUserName;
        Button buttonAccept;
        ImageButton buttonDecline;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            profileAvatar = itemView.findViewById(R.id.profile_avatar_request);
            textViewUserName = itemView.findViewById(R.id.textViewUserNameRequest);
            buttonAccept = itemView.findViewById(R.id.buttonAcceptRequest);
            buttonDecline = itemView.findViewById(R.id.buttonDeclineRequest);
        }

        public void bind(final User requester, final OnAcceptRequestListener acceptListener, final OnDeclineRequestListener declineListener) {
            if (requester == null) return; // Check null safety
            textViewUserName.setText(requester.getFullName());

            Glide.with(itemView.getContext())
                    .load(requester.getAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileAvatar);

            buttonAccept.setOnClickListener(v -> {
                if (acceptListener != null) {
                    acceptListener.onAcceptClicked(requester);
                }
            });

            buttonDecline.setOnClickListener(v -> {
                if (declineListener != null) {
                    declineListener.onDeclineClicked(requester);
                }
            });
        }
    }
}