package com.example.locket.ui.friend;

import android.app.AlertDialog; // Thêm nếu chưa có
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.model.User;
import com.example.locket.data.FriendRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Import các Adapter mới
import com.example.locket.ui.friend.FriendAdapter;
import com.example.locket.ui.friend.SearchResultAdapter;
import com.example.locket.ui.friend.FriendRequestAdapter;


public class FriendList extends AppCompatActivity {

    // --- Các biến thành viên giữ nguyên ---
    private static final String TAG = "FriendListActivity";

    // ... (Views, Data, Firebase, etc. giữ nguyên) ...
    private ImageView btnBack;
    private TextView textViewFriendCount;
    private TextView textViewInvitePrompt;
    private EditText editTextSearchFriend;
    private TextView buttonCancelSearch;
    private ConstraintLayout searchContainer;
    private RecyclerView recyclerViewFriends;
    private FriendAdapter friendAdapter; // Kiểu dữ liệu không đổi
    private List<User> friendList;
    private LinearLayout yourFriendsHeader;
    private RecyclerView recyclerViewSearchResults;
    private SearchResultAdapter searchResultAdapter; // Kiểu dữ liệu không đổi
    private List<User> searchResultList;
    private RecyclerView recyclerViewFriendRequests;
    private FriendRequestAdapter friendRequestAdapter; // Kiểu dữ liệu không đổi
    private List<User> friendRequestList;
    private LinearLayout friendRequestsHeader;
    private FriendRepository friendRepository;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private boolean isSearching = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        friendRepository = new FriendRepository();

        if (currentUser == null) {
            Log.e(TAG, "Người dùng chưa đăng nhập!");
            finish();
            return;
        }

        // Khởi tạo các List
        friendList = new ArrayList<>();
        searchResultList = new ArrayList<>();
        friendRequestList = new ArrayList<>();

        // Ánh xạ Views (giữ nguyên)
        btnBack = findViewById(R.id.btn_back);
        textViewFriendCount = findViewById(R.id.textViewFriendCount);
        textViewInvitePrompt = findViewById(R.id.textViewInvitePrompt);
        editTextSearchFriend = findViewById(R.id.editTextSearchFriend);
        buttonCancelSearch = findViewById(R.id.buttonCancelSearch);
        searchContainer = findViewById(R.id.searchContainer);
        recyclerViewFriends = findViewById(R.id.recyclerViewFriends);
        yourFriendsHeader = findViewById(R.id.yourFriendsHeader);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        recyclerViewFriendRequests = findViewById(R.id.recyclerViewFriendRequests);
        friendRequestsHeader = findViewById(R.id.friendRequestsHeader);

        // --- Cài đặt Adapters (Không cần thay đổi logic khởi tạo) ---

        // Adapter Bạn bè
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(this));
        // Lưu ý: this::showRemoveFriendConfirmationDialog vẫn hoạt động vì
        // FriendAdapter.OnRemoveFriendListener là public interface
        friendAdapter = new FriendAdapter(friendList, this::showRemoveFriendConfirmationDialog);
        recyclerViewFriends.setAdapter(friendAdapter);

        // Adapter Kết quả Tìm kiếm
        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        searchResultAdapter = new SearchResultAdapter(searchResultList, this::sendFriendRequest);
        recyclerViewSearchResults.setAdapter(searchResultAdapter);

        // Adapter Yêu cầu Kết bạn
        recyclerViewFriendRequests.setLayoutManager(new LinearLayoutManager(this));
        friendRequestAdapter = new FriendRequestAdapter(
                friendRequestList,
                this::acceptFriendRequestAction,
                this::declineFriendRequestAction
        );
        recyclerViewFriendRequests.setAdapter(friendRequestAdapter);

        // Tải dữ liệu ban đầu (giữ nguyên)
        loadInitialData();

        // Xử lý sự kiện nút Back (giữ nguyên)
        btnBack.setOnClickListener(v -> finish());

        // Xử lý sự kiện cho thanh tìm kiếm (giữ nguyên)
        setupSearchFunctionality();

        // Cập nhật giao diện ban đầu (giữ nguyên)
        updateUIVisibility();
    }

    // --- Các phương thức còn lại (onResume, loadData, updateUI, handlers, dialogs, ...) ---
    // --- GIỮ NGUYÊN KHÔNG THAY ĐỔI ---
    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser != null && !isSearching) {
            loadInitialData();
        }
    }

    private void loadInitialData() {
        loadFriendRequests();
        loadFriendsList();
    }

    private void loadFriendRequests() {
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Đang tải danh sách yêu cầu kết bạn cho user: " + currentUserId);

        friendRepository.getFriendRequests(currentUserId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Tải danh sách yêu cầu kết bạn thành công. Số lượng: " + data.size());
                friendRequestList.clear();
                friendRequestList.addAll(data);
                friendRequestAdapter.notifyDataSetChanged();
                updateUIVisibility();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi tải danh sách yêu cầu kết bạn", e);
                Toast.makeText(FriendList.this, "Lỗi tải yêu cầu kết bạn", Toast.LENGTH_SHORT).show();
                friendRequestList.clear();
                friendRequestAdapter.notifyDataSetChanged();
                updateUIVisibility();
            }
        });
    }

    private void loadFriendsList() {
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();
        Log.d(TAG, "Đang tải danh sách bạn bè (accepted) cho user: " + currentUserId);

        friendRepository.getFriendList(currentUserId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Tải danh sách bạn bè (accepted) thành công. Số lượng: " + data.size());
                friendList.clear();
                friendList.addAll(data);
                friendAdapter.notifyDataSetChanged();
                updateFriendCount(friendList.size());
                updateUIVisibility();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi tải danh sách bạn bè (accepted)", e);
                Toast.makeText(FriendList.this, "Lỗi tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
                updateFriendCount(0);
                friendList.clear();
                friendAdapter.notifyDataSetChanged();
                updateUIVisibility();
            }
        });
    }

    private void updateFriendCount(int count) {
        textViewFriendCount.setText(count + " out of 20 friends");
        textViewInvitePrompt.setVisibility(count == 0 && friendRequestList.isEmpty() ? View.VISIBLE : View.GONE); // Logic cập nhật lại
    }

    private void setupSearchFunctionality() {
        editTextSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    if (!isSearching) {
                        isSearching = true;
                        updateUIVisibility();
                    }
                    buttonCancelSearch.setVisibility(View.VISIBLE);
                    performSearch(query);
                } else {
                    if (isSearching) {
                        cancelSearch();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextSearchFriend.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(editTextSearchFriend.getText().toString().trim());
                // hideKeyboard(); // Bạn nên có hàm này
                return true;
            }
            return false;
        });

        buttonCancelSearch.setOnClickListener(v -> cancelSearch());

        editTextSearchFriend.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && editTextSearchFriend.getText().length() > 0) {
                buttonCancelSearch.setVisibility(View.VISIBLE);
                if (!isSearching) {
                    isSearching = true;
                    updateUIVisibility();
                    searchResultList.clear();
                    searchResultAdapter.notifyDataSetChanged();
                }
            } else if (!hasFocus && editTextSearchFriend.getText().length() == 0) {
                if (isSearching) {
                    // cancelSearch(); // Cân nhắc kỹ
                }
            } else if (editTextSearchFriend.getText().length() == 0) {
                buttonCancelSearch.setVisibility(View.GONE);
            }
        });
    }

    private void performSearch(String query) {
        if (currentUser == null || query.isEmpty()) {
            searchResultList.clear();
            searchResultAdapter.notifyDataSetChanged();
            return;
        }
        Log.d(TAG, "Performing search for query: " + query);

        // Tạo danh sách loại trừ (người đã là bạn bè + đã gửi/nhận yêu cầu kết bạn + chính bản thân)
        Set<String> excludeIds = new HashSet<>();

        for (User friend : friendList) {
            excludeIds.add(friend.getUid());
        }
        for (User pending : friendRequestList) {
            excludeIds.add(pending.getUid());
        }
        excludeIds.add(currentUser.getUid()); // Không cho chính mình vào danh sách

        // Gọi search từ repository, truyền excludeIds để loại trừ server-side (nếu có)
        friendRepository.searchUsers(query, currentUser.getUid(), new ArrayList<>(excludeIds), new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Search successful. Found " + data.size() + " users.");

                searchResultList.clear();

                for (User user : data) {
                    if (!excludeIds.contains(user.getUid())) {
                        searchResultList.add(user); // Chỉ thêm nếu chưa có quan hệ
                    }
                }

                searchResultAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Search failed", e);
                Toast.makeText(FriendList.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                searchResultList.clear();
                searchResultAdapter.notifyDataSetChanged();
            }
        });
    }


    private void cancelSearch() {
        isSearching = false;
        editTextSearchFriend.setText("");
        editTextSearchFriend.clearFocus();
        buttonCancelSearch.setVisibility(View.GONE);
        // hideKeyboard();
        searchResultList.clear();
        searchResultAdapter.notifyDataSetChanged();
        updateUIVisibility(); // Hiển thị lại các list bạn bè/yêu cầu
        // Có thể cần load lại data nếu có thay đổi khi đang search
        loadInitialData();
    }

    private void updateUIVisibility() {
        if (isSearching) {
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
            recyclerViewFriendRequests.setVisibility(View.GONE);
            friendRequestsHeader.setVisibility(View.GONE);
            recyclerViewFriends.setVisibility(View.GONE);
            yourFriendsHeader.setVisibility(View.GONE);
            textViewInvitePrompt.setVisibility(View.GONE);
        } else {
            recyclerViewSearchResults.setVisibility(View.GONE);

            boolean hasRequests = friendRequestList != null && !friendRequestList.isEmpty();
            friendRequestsHeader.setVisibility(hasRequests ? View.VISIBLE : View.GONE);
            recyclerViewFriendRequests.setVisibility(hasRequests ? View.VISIBLE : View.GONE);

            boolean hasFriends = friendList != null && !friendList.isEmpty();
            yourFriendsHeader.setVisibility(View.VISIBLE); // Luôn hiện khi không search
            recyclerViewFriends.setVisibility(hasFriends ? View.VISIBLE : View.GONE);

            // Chỉ hiện prompt khi không có bạn VÀ không có request
            textViewInvitePrompt.setVisibility(!hasFriends && !hasRequests ? View.VISIBLE : View.GONE);
        }
    }

    private void sendFriendRequest(User userToAdd) {
        if (currentUser == null) return;

        // Kiểm tra lại
        boolean alreadyFriend = friendList.stream().anyMatch(f -> f.getUid().equals(userToAdd.getUid()));
        boolean alreadyReceivedRequest = friendRequestList.stream().anyMatch(r -> r.getUid().equals(userToAdd.getUid()));
        // Thêm kiểm tra đã gửi request chưa

        if (alreadyFriend) {
            Toast.makeText(this, userToAdd.getFullName() + " is already your friend.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (alreadyReceivedRequest) {
            Toast.makeText(this, "You have a pending request from " + userToAdd.getFullName() + ".", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra nếu đã gửi request...

        Log.d(TAG, "Sending friend request to: " + userToAdd.getFullName() + " (ID: " + userToAdd.getUid() + ")");
        friendRepository.sendFriendRequest(currentUser.getUid(), userToAdd.getUid());

        // Cập nhật UI: Xóa khỏi search results
        int position = searchResultList.indexOf(userToAdd); // Cách tìm index đơn giản hơn
        if (position != -1) {
            searchResultList.remove(position);
            searchResultAdapter.notifyItemRemoved(position);
        }

        Toast.makeText(this, "Friend request sent to " + userToAdd.getFullName(), Toast.LENGTH_SHORT).show();
        // Cân nhắc cập nhật trạng thái nút thành "Pending" nếu muốn giữ item trong list
        // searchResultAdapter.updateItemState(userToAdd.getUid(), "Pending");
    }

    private void acceptFriendRequestAction(User requester) {
        if (currentUser == null) return;
        Log.d(TAG, "User clicked Accept for requester: " + requester.getFullName());

        friendRepository.acceptFriendRequest(currentUser.getUid(), requester.getUid(), new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Successfully accepted request from " + requester.getUid());
                Toast.makeText(FriendList.this, "You are now friends with " + requester.getFullName(), Toast.LENGTH_SHORT).show();

                // Cập nhật UI:
                int requestIndex = friendRequestList.indexOf(requester);
                if (requestIndex != -1) {
                    friendRequestList.remove(requestIndex);
                    friendRequestAdapter.notifyItemRemoved(requestIndex);
                }

                boolean alreadyInFriendList = friendList.stream().anyMatch(f -> f.getUid().equals(requester.getUid()));
                if (!alreadyInFriendList) {
                    friendList.add(0, requester);
                    friendAdapter.notifyItemInserted(0);
                    recyclerViewFriends.scrollToPosition(0); // Cuộn lên đầu
                }

                updateFriendCount(friendList.size());
                updateUIVisibility();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to accept friend request", e);
                Toast.makeText(FriendList.this, "Failed to accept request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineFriendRequestAction(User requester) {
        if (currentUser == null) return;
        Log.d(TAG, "User clicked Decline for requester: " + requester.getFullName());
        performDecline(requester); // Gọi trực tiếp hoặc qua dialog
    }

    private void performDecline(User requester) {
        friendRepository.declineFriendRequest(currentUser.getUid(), requester.getUid(), new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Successfully declined/deleted request from " + requester.getUid());
                Toast.makeText(FriendList.this, "Request from " + requester.getFullName() + " declined.", Toast.LENGTH_SHORT).show();

                int requestIndex = friendRequestList.indexOf(requester);
                if (requestIndex != -1) {
                    friendRequestList.remove(requestIndex);
                    friendRequestAdapter.notifyItemRemoved(requestIndex);
                    updateUIVisibility();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to decline friend request", e);
                Toast.makeText(FriendList.this, "Failed to decline request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showRemoveFriendConfirmationDialog(User friendToRemove) {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Removal")
                .setMessage("Are you sure you want to remove " + friendToRemove.getFullName() + " from your friends?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performRemoveFriend(friendToRemove);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    private void performRemoveFriend(User friendToRemove) {
        String currentUserId = currentUser.getUid();
        String friendIdToRemove = friendToRemove.getUid();

        Log.d(TAG, "Xác nhận xóa bạn: " + friendIdToRemove);

        friendRepository.unfriend(currentUserId, friendIdToRemove, new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Hủy kết bạn trên Firestore thành công.");
                Toast.makeText(FriendList.this, "Removed " + friendToRemove.getFullName(), Toast.LENGTH_SHORT).show();

                int position = friendList.indexOf(friendToRemove);
                if (position != -1) {
                    friendList.remove(position);
                    friendAdapter.notifyItemRemoved(position);
                    updateFriendCount(friendList.size());
                    updateUIVisibility();
                    Log.d(TAG, "Đã xóa bạn khỏi danh sách cục bộ: " + friendIdToRemove);
                } else {
                    Log.w(TAG, "Không tìm thấy bạn để xóa trong danh sách cục bộ: " + friendIdToRemove);
                    loadFriendsList(); // Tải lại nếu không tìm thấy
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Lỗi khi hủy kết bạn trên Firestore", e);
                Toast.makeText(FriendList.this, "Failed to remove friend: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                loadFriendsList(); // Tải lại để đồng bộ
            }
        });
    }

    // --- KHÔNG CÒN CÁC LỚP ADAPTER Ở ĐÂY ---

}