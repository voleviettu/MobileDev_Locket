package com.example.locket.ui.settings;

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
import com.example.locket.model.PhotoReaction;
import com.example.locket.model.User;

import java.util.ArrayList;
import java.util.List;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.ReactionViewHolder> {

    private final Context context;
    private final List<PhotoReaction> reactions;
    private final List<User> users;

    public ReactionAdapter(Context context, List<PhotoReaction> reactions, List<User> users) {
        this.context = context;
        this.reactions = reactions != null ? reactions : new ArrayList<>();
        this.users = users != null ? users : new ArrayList<>();
    }

    @NonNull
    @Override
    public ReactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reaction, parent, false);
        return new ReactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionViewHolder holder, int position) {
        PhotoReaction reaction = reactions.get(position);

        User user = null;
        for (User u : users) {
            if (u.getUid().equals(reaction.getUserId())) {
                user = u;
                break;
            }
        }

        if (user != null) {
            String userName = user.getFullName() != null && !user.getFullName().isEmpty()
                    ? user.getFullName()
                    : user.getUsername() != null && !user.getUsername().isEmpty()
                    ? user.getUsername()
                    : "Người dùng ẩn danh";
            holder.tvUserName.setText(userName);

            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(context)
                        .load(user.getAvatar())
                        .circleCrop()
                        .into(holder.ivUserAvatar);
            } else {
                holder.ivUserAvatar.setImageResource(R.drawable.ic_profile);
            }
        } else {
            holder.tvUserName.setText("Người dùng ẩn danh");
            holder.ivUserAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Hiển thị icon reaction
        String reactionType = reaction.getReaction();
        switch (reactionType) {
            case "love":
                holder.ivReactionIcon.setImageResource(R.drawable.ic_heart);
                break;
            case "fire":
                holder.ivReactionIcon.setImageResource(R.drawable.ic_fire);
                break;
            case "smile":
                holder.ivReactionIcon.setImageResource(R.drawable.ic_smile);
                break;
            default:
                holder.ivReactionIcon.setImageResource(R.drawable.ic_heart); // Mặc định
        }
    }

    @Override
    public int getItemCount() {
        return reactions.size();
    }

    static class ReactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName;
        ImageView ivReactionIcon;

        public ReactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            ivReactionIcon = itemView.findViewById(R.id.iv_reaction_icon);
        }
    }
}