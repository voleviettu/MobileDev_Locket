package com.example.locket.ui.friend;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.locket.R;
import com.example.locket.model.User;

import java.util.HashMap;
import java.util.HashSet; // Import Set
import java.util.List;
import java.util.Map;
import java.util.Set; // Import Set

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {

    private final List<User> userList;
    private final OnAddFriendListener addListener;

    // Giữ lại pendingState và updateItemState cho các mục đích khác (nếu có)
    private final Map<String, Boolean> pendingState = new HashMap<>();

    // Thêm Set để lưu trạng thái pending lấy từ Firestore
    private Set<String> sentPendingIds = new HashSet<>();

    public interface OnAddFriendListener {
        void onAddClicked(User user);
    }

    // Constructor có thể nhận initialPendingIds hoặc không, tùy bạn quyết định
    public SearchResultAdapter(List<User> userList, OnAddFriendListener addListener) {
        this.userList = userList;
        this.addListener = addListener;
        // Khởi tạo sentPendingIds nếu cần
        this.sentPendingIds = new HashSet<>();
    }
    // Hoặc constructor nhận cả initialPendingIds nếu muốn:
    // public SearchResultAdapter(List<User> userList, OnAddFriendListener addListener, Set<String> initialPendingIds) {
    //     this.userList = userList;
    //     this.addListener = addListener;
    //     this.sentPendingIds = (initialPendingIds != null) ? initialPendingIds : new HashSet<>();
    // }


    // ---- Giữ nguyên hàm này ----
    // Hàm để Activity cập nhật trạng thái của item thành "Pending" thông qua Map tạm thời
    public void updateItemState(String userId, String state) {
        if ("Pending".equalsIgnoreCase(state)) {
            pendingState.put(userId, true);
            // Tìm vị trí của user và cập nhật item đó
            for (int i = 0; i < userList.size(); i++) {
                // Đảm bảo userList không bị thay đổi đồng thời từ thread khác
                if (i < userList.size() && userList.get(i) != null && userList.get(i).getUid() != null && userList.get(i).getUid().equals(userId)) {
                    notifyItemChanged(i); // Yêu cầu RecyclerView vẽ lại item đó
                    break;
                }
            }
        }
        // Có thể thêm logic xóa khỏi map nếu state khác "Pending"
        // else { pendingState.remove(userId); }
    }
    // ---- Hết phần giữ nguyên ----


    // ---- Thêm hàm này ----
    // Hàm để Activity/Fragment cập nhật danh sách ID pending (lấy từ Firestore)
    public void setSentPendingIds(Set<String> pendingIds) {
        this.sentPendingIds = (pendingIds != null) ? pendingIds : new HashSet<>();
        // Không gọi notifyDataSetChanged() ở đây, Activity/Fragment sẽ gọi khi cần
    }
    // ---- Hết phần thêm ----

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_result, parent, false);
        return new SearchResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null || user.getUid() == null) return; // Kiểm tra null an toàn

        // Ưu tiên kiểm tra sentPendingIds (từ Firestore)
        boolean isPending = sentPendingIds.contains(user.getUid());

        // Nếu không pending theo Firestore, kiểm tra pendingState (từ updateItemState)
        if (!isPending) {
            isPending = pendingState.getOrDefault(user.getUid(), false);
        }

        holder.bind(user, addListener, isPending); // Truyền trạng thái pending vào bind
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
        Context context;

        public SearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            profileAvatar = itemView.findViewById(R.id.profile_avatar_search);
            textViewUserName = itemView.findViewById(R.id.textViewUserNameSearch);
            buttonAddFriend = itemView.findViewById(R.id.buttonAddSearchResult);
        }

        // Hàm bind nhận thêm trạng thái isPending
        public void bind(final User user, final OnAddFriendListener listener, boolean isPending) {
            textViewUserName.setText(user.getFullName());

            Glide.with(itemView.getContext())
                    .load(user.getAvatar())
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .circleCrop()
                    .into(profileAvatar);

            if (isPending) {
                // Cấu hình nút khi ở trạng thái "Pending"
                buttonAddFriend.setText("Pending");
                buttonAddFriend.setEnabled(false);
                buttonAddFriend.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.pendingButtonBackground));
                buttonAddFriend.setTextColor(ContextCompat.getColor(context, R.color.white));
                buttonAddFriend.setOnClickListener(null);
            } else {
                // Cấu hình nút khi ở trạng thái "Add"
                buttonAddFriend.setText("+ Add");
                buttonAddFriend.setEnabled(true);
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