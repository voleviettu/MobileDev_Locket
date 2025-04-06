package com.example.locket.ui.friend;

import android.content.Context;
import android.graphics.Color; // Import Color
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import ContextCompat
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;
import java.util.HashMap; // Dùng để lưu trạng thái pending
import java.util.List;
import java.util.Map;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private final List<User> userList;
    private final OnAddFriendListener addListener;
    private final Map<String, Boolean> pendingState = new HashMap<>(); // Lưu trạng thái pending theo userId

    // Interface để gửi sự kiện click nút "Add" về Activity
    public interface OnAddFriendListener {
        void onAddClicked(User user);
    }

    public SearchResultAdapter(List<User> userList, OnAddFriendListener addListener) {
        this.userList = userList;
        this.addListener = addListener;
    }

    // Hàm để Activity cập nhật trạng thái của item thành "Pending"
    public void updateItemState(String userId, String state) {
        if ("Pending".equalsIgnoreCase(state)) {
            pendingState.put(userId, true);
            // Tìm vị trí của user và cập nhật item đó
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getUid().equals(userId)) {
                    notifyItemChanged(i); // Yêu cầu RecyclerView vẽ lại item đó
                    break;
                }
            }
        }
        // Có thể thêm các state khác nếu cần
    }


    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        User user = userList.get(position);
        boolean isPending = pendingState.getOrDefault(user.getUid(), false);
        holder.bind(user, addListener, isPending);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // --- ViewHolder Class ---
    static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        ImageView profileAvatar;
        TextView textViewUserName;
        Button buttonAddFriend;
        Context context; // Lưu context để lấy color

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext(); // Lấy context từ itemView
            profileAvatar = itemView.findViewById(R.id.profile_avatar_search);
            textViewUserName = itemView.findViewById(R.id.textViewUserNameSearch);
            buttonAddFriend = itemView.findViewById(R.id.buttonAddSearchResult);
        }

        public void bind(final User user, final OnAddFriendListener listener, boolean isPending) {
            textViewUserName.setText(user.getFullName()); // Hoặc getUsername() tùy yêu cầu

            Glide.with(itemView.getContext())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop() // Hoặc dùng background tròn như XML
                    .into(profileAvatar);

            if (isPending) {
                // Cấu hình nút khi ở trạng thái "Pending"
                buttonAddFriend.setText("Pending");
                buttonAddFriend.setEnabled(false);
                // Đổi màu nền và chữ (ví dụ)
                buttonAddFriend.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pendingButtonBackground));
                buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                // Cấu hình nút khi ở trạng thái "Add"
                buttonAddFriend.setText("+ Add");
                buttonAddFriend.setEnabled(true);
                // Set lại màu mặc định từ XML (hoặc định nghĩa trong colors.xml)
                buttonAddFriend.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.searchResultAddButtonBackground));
                buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.searchResultAddButtonText));

                buttonAddFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onAddClicked(user);
                    }
                });
            }
        }
    }
}