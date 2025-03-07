package com.example.locket.ui.photo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    private Context context;
    private List<User> friendsList;
    private OnFriendClickListener listener;
    private boolean isAllSelected = true;
    private Set<Integer> selectedPositions = new HashSet<>();
    public interface OnFriendClickListener {
        void onFriendSelected(User user);
    }

    public FriendsAdapter(Context context, List<User> friends, OnFriendClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.friendsList = friends;
        selectedPositions.add(0);
    }


    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);
        holder.tvName.setText(friend.getFirstname());

        if (position == 0) {
            holder.imgProfile.setVisibility(View.VISIBLE);
            holder.imgProfile.setImageResource(R.drawable.ic_friends);
            holder.imgProfile.getLayoutParams().width = 36;
            holder.imgProfile.getLayoutParams().height = 36;
            holder.tvInitials.setVisibility(View.GONE);
        } else {
            if (friend.getAvatar() != null && !friend.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(friend.getAvatar().replace("http://", "https://"))
                        .circleCrop()
                        .into(holder.imgProfile);
                holder.imgProfile.setVisibility(View.VISIBLE);
                holder.tvInitials.setVisibility(View.GONE);
            } else {
                String initials = getInitials(friend.getFirstname(), friend.getLastname());
                holder.tvInitials.setText(initials);
                holder.tvInitials.setVisibility(View.VISIBLE);
                holder.imgProfile.setVisibility(View.GONE);
            }
        }


        if (isAllSelected) {
            if (position == 0) {
                holder.profileFrame.setBackgroundResource(R.drawable.border_selected);
            } else {
                holder.profileFrame.setBackgroundResource(R.drawable.border_default);
            }
        } else {
            if (selectedPositions.contains(position)) {
                holder.profileFrame.setBackgroundResource(R.drawable.border_selected);
            } else {
                holder.profileFrame.setBackgroundResource(R.drawable.border_default);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            int newPosition = holder.getAdapterPosition();
            if (newPosition == RecyclerView.NO_POSITION) return;

            if (newPosition == 0) {
                isAllSelected = true;
                selectedPositions.clear();
                selectedPositions.add(0);
            } else {
                if (isAllSelected) {
                    isAllSelected = false;
                    selectedPositions.clear();
                }
                if (selectedPositions.contains(newPosition)) {
                    selectedPositions.remove(newPosition);
                    if (selectedPositions.isEmpty()) {
                        isAllSelected = true;
                        selectedPositions.add(0);
                    }
                } else {
                    selectedPositions.add(newPosition);
                }
            }

            notifyDataSetChanged();
            listener.onFriendSelected(friend);
        });
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvName, tvInitials;
        FrameLayout profileFrame;

        FriendViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            profileFrame = itemView.findViewById(R.id.profile_frame);
        }
    }

    private String getInitials(String firstName, String lastName) {
        String initials = "";
        if (lastName != null && !lastName.isEmpty()) {
            initials += lastName.charAt(0);
        }
        if (firstName != null && !firstName.isEmpty()) {
            initials += firstName.charAt(0);
        }
        return initials;
    }

    public List<String> getSelectedFriendIds() {
        List<String> selectedIds = new ArrayList<>();

        if (isAllSelected) {
            for (int i = 1; i < friendsList.size(); i++) {
                selectedIds.add(friendsList.get(i).getUserId());
            }
        } else {
            for (int pos : selectedPositions) {
                if (pos > 0) {
                    selectedIds.add(friendsList.get(pos).getUserId());
                }
            }
        }
        return selectedIds;
    }

}
