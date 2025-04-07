package com.example.locket.viewmodel;

import android.util.Log; // Thêm Log

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.data.FriendRepository;
import com.example.locket.model.User;
// Import Event wrapper nếu bạn sử dụng (khuyến khích)
// import com.example.locket.util.Event;

import java.util.ArrayList; // Thêm ArrayList
import java.util.List;

public class FriendViewModel extends ViewModel {
    private static final String TAG = "FriendViewModel"; // Thêm TAG

    // xài cho detail photoactivity
    public LiveData<List<User>> getFriends() {
        return friends;
    }

    private final FriendRepository friendRepository;

    private final MutableLiveData<List<User>> _friends = new MutableLiveData<>();
    public LiveData<List<User>> friends = _friends; // Đổi tên _friends để public là friends

    // LiveData cho danh sách yêu cầu kết bạn (requested) - MỚI
    private final MutableLiveData<List<User>> _friendRequests = new MutableLiveData<>();
    public LiveData<List<User>> friendRequests = _friendRequests;

    // LiveData cho kết quả tìm kiếm - MỚI
    private final MutableLiveData<List<User>> _searchResults = new MutableLiveData<>();
    public LiveData<List<User>> searchResults = _searchResults;

    // LiveData cho trạng thái loading (Tùy chọn)
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho thông báo lỗi (Tùy chọn, dùng Event để tránh hiển thị lại)
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage; // Hoặc dùng Event<String>

    // LiveData cho kết quả hành động (Tùy chọn, dùng Event)
    private final MutableLiveData<Boolean> _actionSuccess = new MutableLiveData<>();
    public LiveData<Boolean> actionSuccess = _actionSuccess; // Hoặc Event<Boolean>

    public FriendViewModel() {
        // Khởi tạo repository. Nếu cần dependency injection, hãy inject ở đây.
        this.friendRepository = new FriendRepository();
    }


    public void loadFriends(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "loadFriends: userId is null or empty");
            _friends.setValue(new ArrayList<>()); // Set list rỗng
            return;
        }
        Log.d(TAG, "Loading friends for user: " + userId);
        _isLoading.setValue(true);
        friendRepository.getFriendList(userId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Friends loaded successfully: " + data.size());
                _friends.setValue(data);
                _isLoading.setValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading friends", e);
                _friends.setValue(new ArrayList<>()); // Đặt list rỗng khi lỗi
                _errorMessage.setValue("Error loading friends: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void loadFriendRequests(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "loadFriendRequests: userId is null or empty");
            _friendRequests.setValue(new ArrayList<>());
            return;
        }
        Log.d(TAG, "Loading friend requests for user: " + userId);
        // Không cần set isLoading ở đây nếu load đồng thời với friends
        friendRepository.getFriendRequests(userId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Friend requests loaded successfully: " + data.size());
                _friendRequests.setValue(data);
            }
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading friend requests", e);
                _friendRequests.setValue(new ArrayList<>());
                _errorMessage.setValue("Error loading requests: " + e.getMessage());
            }
        });
    }

    public void searchUsers(String query, String currentUserId, List<String> excludeIds) {
        if (query == null || query.trim().isEmpty()) {
            _searchResults.setValue(new ArrayList<>()); // Xóa kết quả nếu query rỗng
            return;
        }
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.w(TAG, "searchUsers: currentUserId is null or empty");
            _searchResults.setValue(new ArrayList<>());
            return;
        }

        Log.d(TAG, "Searching users with query: " + query);
        _isLoading.setValue(true); // Có thể set loading cho tìm kiếm
        friendRepository.searchUsers(query, currentUserId, excludeIds, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                Log.d(TAG, "Search successful, found: " + data.size());
                _searchResults.setValue(data);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error searching users", e);
                _searchResults.setValue(new ArrayList<>());
                _errorMessage.setValue("Search failed: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void clearSearchResults() {
        _searchResults.setValue(new ArrayList<>());
    }

    public void sendFriendRequest(String fromUid, String toUid) {
        if (fromUid == null || fromUid.isEmpty() || toUid == null || toUid.isEmpty()) {
            Log.w(TAG, "sendFriendRequest: Invalid UIDs");
            _errorMessage.setValue("Cannot send request: Invalid user ID.");
            return;
        }
        Log.d(TAG, "Sending friend request from " + fromUid + " to " + toUid);
        // Repository xử lý việc gửi. ViewModel không cần chờ kết quả trực tiếp ở đây
        // trừ khi muốn hiển thị loading/success/error cụ thể cho việc *gửi*
        friendRepository.sendFriendRequest(fromUid, toUid);
        // UI (Activity/Fragment) có thể cập nhật trạng thái ngay lập tức (vd: xóa khỏi search)
    }

    public void acceptFriendRequest(String currentUserId, String requesterId) {
        if (currentUserId == null || currentUserId.isEmpty() || requesterId == null || requesterId.isEmpty()) {
            Log.w(TAG, "acceptFriendRequest: Invalid UIDs");
            _errorMessage.setValue("Cannot accept request: Invalid user ID.");
            return;
        }
        Log.d(TAG, "Accepting friend request from " + requesterId + " by " + currentUserId);
        _isLoading.setValue(true); // Hiển thị loading cho hành động này
        friendRepository.acceptFriendRequest(currentUserId, requesterId, new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Friend request accepted successfully.");
                _actionSuccess.setValue(true); // Thông báo thành công
                // QUAN TRỌNG: Tải lại cả danh sách bạn bè và yêu cầu
                loadFriends(currentUserId);
                loadFriendRequests(currentUserId);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error accepting friend request", e);
                _actionSuccess.setValue(false); // Thông báo thất bại
                _errorMessage.setValue("Failed to accept request: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void declineFriendRequest(String currentUserId, String requesterId) {
        if (currentUserId == null || currentUserId.isEmpty() || requesterId == null || requesterId.isEmpty()) {
            Log.w(TAG, "declineFriendRequest: Invalid UIDs");
            _errorMessage.setValue("Cannot decline request: Invalid user ID.");
            return;
        }
        Log.d(TAG, "Declining friend request from " + requesterId + " by " + currentUserId);
        _isLoading.setValue(true);
        friendRepository.declineFriendRequest(currentUserId, requesterId, new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Friend request declined successfully.");
                _actionSuccess.setValue(true);
                // Chỉ cần tải lại danh sách yêu cầu
                loadFriendRequests(currentUserId);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error declining friend request", e);
                _actionSuccess.setValue(false);
                _errorMessage.setValue("Failed to decline request: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    public void unfriend(String userId, String friendId) {
        if (userId == null || userId.isEmpty() || friendId == null || friendId.isEmpty()) {
            Log.w(TAG, "unfriend: Invalid UIDs");
            _errorMessage.setValue("Cannot unfriend: Invalid user ID.");
            return;
        }
        Log.d(TAG, "Unfriending " + friendId + " by " + userId);
        _isLoading.setValue(true);
        friendRepository.unfriend(userId, friendId, new FriendRepository.FirestoreCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Unfriend successful.");
                _actionSuccess.setValue(true);
                // Chỉ cần tải lại danh sách bạn bè
                loadFriends(userId);
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error unfriending", e);
                _actionSuccess.setValue(false);
                _errorMessage.setValue("Failed to unfriend: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }
}