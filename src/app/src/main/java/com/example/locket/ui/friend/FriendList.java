package com.example.locket.ui.friend;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Thêm thư viện Glide hoặc Picasso vào build.gradle
import com.example.locket.R;
import com.example.locket.data.FriendRepository;
import com.example.locket.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FriendList extends AppCompatActivity {

    private static final String TAG = "FriendListActivity";

    private RecyclerView recyclerViewFriends;
    private FriendAdapter friendAdapter;
    private List<User> friendList;
    private FriendRepository friendRepository;
    private FirebaseAuth firebaseAuth;
    private TextView textViewFriendCount;
    private MaterialButton buttonAddFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        // Khởi tạo Firebase Auth và Repository
        firebaseAuth = FirebaseAuth.getInstance();
        friendRepository = new FriendRepository();

        // Khởi tạo List bạn bè
        friendList = new ArrayList<>();

        // Ánh xạ View
        textViewFriendCount = findViewById(R.id.textViewFriendCount);
        buttonAddFriend = findViewById(R.id.buttonAddFriend);
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);

        // Cài đặt RecyclerView
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        friendAdapter = new FriendAdapter(friendList, friend -> {
            // Xử lý khi nhấn nút xóa bạn
            removeFriend(friend);
        });
        recyclerViewFriends.setAdapter(friendAdapter);

        // Tải danh sách bạn bè
        loadFriendsList();

        // Xử lý sự kiện nút "Add a new friend" (Ví dụ: chuyển sang Activity tìm kiếm)
        buttonAddFriend.setOnClickListener(v -> {
            // Intent intent = new Intent(FriendList.this, SearchFriendActivity.class); // Thay SearchFriendActivity bằng Activity thực tế
            // startActivity(intent);
            Toast.makeText(this, "Chức năng Add Friend chưa được cài đặt", Toast.LENGTH_SHORT).show(); // Placeholder
        });
    }

    private void loadFriendsList() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Người dùng chưa đăng nhập!");
            // Có thể chuyển về màn hình đăng nhập hoặc hiển thị thông báo
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity này nếu user chưa đăng nhập
            return;
        }

        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Đang tải danh sách bạn bè cho user: " + currentUserId);

        friendRepository.getFriendList(currentUserId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Tải danh sách bạn bè thành công. Số lượng: " + data.size());
                friendList.clear();
                friendList.addAll(data);
                friendAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                updateFriendCount(friendList.size()); // Cập nhật số lượng bạn bè trên TextView
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi tải danh sách bạn bè", e);
                Toast.makeText(FriendList.this, "Lỗi tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
                updateFriendCount(0); // Cập nhật số lượng về 0 khi có lỗi
            }
        });
    }

    private void updateFriendCount(int count) {
        // Cập nhật TextView hiển thị số lượng bạn bè
        // Bạn có thể thay đổi số "20" nếu giới hạn bạn bè là động
        textViewFriendCount.setText(count + " out of 20 friends");
    }

    private void removeFriend(User friendToRemove) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }
        String currentUserId = currentUser.getUid();
        String friendIdToRemove = friendToRemove.getUid();

        Log.d(TAG, "Yêu cầu xóa bạn: " + friendIdToRemove);

        // Gọi hàm unfriend từ repository
        friendRepository.unfriend(currentUserId, friendIdToRemove);

        // Cập nhật UI ngay lập tức (hoặc đợi callback nếu cần xác nhận từ server)
        // Cách 1: Xóa khỏi list local và cập nhật adapter (nhanh hơn về mặt hiển thị)
        int position = -1;
        for (int i = 0; i < friendList.size(); i++) {
            if (friendList.get(i).getUid().equals(friendIdToRemove)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            friendList.remove(position);
            friendAdapter.notifyItemRemoved(position);
            friendAdapter.notifyItemRangeChanged(position, friendList.size()); // Cần thiết để cập nhật vị trí các item còn lại
            updateFriendCount(friendList.size()); // Cập nhật lại số lượng
            Log.d(TAG, "Đã xóa bạn khỏi danh sách cục bộ: " + friendIdToRemove);
            Toast.makeText(this, "Đã xóa " + friendToRemove.getFullName(), Toast.LENGTH_SHORT).show();
        } else {
            Log.w(TAG, "Không tìm thấy bạn để xóa trong danh sách cục bộ: " + friendIdToRemove);
        }


        // Cách 2: Tải lại toàn bộ danh sách (chắc chắn đồng bộ với server hơn sau khi xóa)
        // loadFriendsList();
        // Toast.makeText(this, "Đã gửi yêu cầu xóa " + friendToRemove.getFullName(), Toast.LENGTH_SHORT).show();

    }

    // --- Adapter Class ---
    private static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

        private final List<User> friendList;
        private final OnRemoveFriendListener removeListener;

        // Interface để xử lý sự kiện click nút xóa
        interface OnRemoveFriendListener {
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
            return friendList.size();
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

            public void bind(final User friend, final OnRemoveFriendListener listener) {
                textViewFriendName.setText(friend.getFullName()); // Sử dụng getFullName()

                // Sử dụng Glide để tải ảnh đại diện (hoặc Picasso)
                // Đảm bảo bạn đã thêm dependency của Glide vào build.gradle
                Glide.with(itemView.getContext())
                        .load(friend.getAvatar()) // getAvatar() đã xử lý http -> https
                        .placeholder(R.drawable.default_avatar) // Ảnh hiển thị khi đang tải
                        .error(R.drawable.default_avatar)       // Ảnh hiển thị khi lỗi
                        // .circleCrop() // Bỏ comment nếu muốn ảnh luôn tròn (không phụ thuộc background)
                        .into(profileAvatar);

                // Đặt sự kiện click cho nút xóa
                buttonRemoveFriend.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveClicked(friend);
                    }
                });
            }
        }
    }
}