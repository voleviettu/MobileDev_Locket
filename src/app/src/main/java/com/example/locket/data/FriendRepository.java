package com.example.locket.data;

import android.util.Log;
import com.example.locket.model.Friend;
import com.example.locket.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FriendRepository {
    private static final String TAG = "FriendRepository";
    private static final String COLLECTION_NAME = "friend_relationships";

    private static final String USERS_COLLECTION = "users";
    private final FirebaseFirestore db;

    public FriendRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void sendFriendRequest(String fromUid, String toUid) {
        Friend sender = new Friend(fromUid, toUid, "pending"); // gửi
        Friend receiver = new Friend(toUid, fromUid, "requested"); // nhận

        db.collection(COLLECTION_NAME).add(sender)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Đã gửi lời mời kết bạn"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi gửi lời mời", e));

        db.collection(COLLECTION_NAME).add(receiver)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Đã tạo bản ghi cho người nhận"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi tạo bản ghi người nhận", e));
    }

    public void acceptFriendRequest(String currentUserId, String requesterId) {
        Query query = db.collection(COLLECTION_NAME)
                .whereIn("status", List.of("pending", "requested"))
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", requesterId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                db.collection(COLLECTION_NAME).document(doc.getId())
                        .update("status", "accepted");
            }

            db.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", requesterId)
                    .whereEqualTo("friendId", currentUserId)
                    .get()
                    .addOnSuccessListener(docs -> {
                        for (DocumentSnapshot d : docs) {
                            db.collection(COLLECTION_NAME).document(d.getId())
                                    .update("status", "accepted");
                        }
                    });

        }).addOnFailureListener(e -> Log.e(TAG, "Lỗi khi chấp nhận lời mời", e));
    }

    public void getFriendList(String userId, FirestoreCallback<List<User>> callback) {
        // Query 1: Tìm các bản ghi mà userId hiện tại là "chủ"
        Task<QuerySnapshot> query1 = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "accepted")
                .get();

        // Query 2: Tìm các bản ghi mà userId hiện tại là "bạn"
        Task<QuerySnapshot> query2 = db.collection(COLLECTION_NAME)
                .whereEqualTo("friendId", userId)
                .whereEqualTo("status", "accepted")
                .get();

        // Kết hợp kết quả của cả hai query khi chúng hoàn thành thành công
        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(query1, query2);

        allTasks.addOnSuccessListener(results -> {
            // Dùng Set để lưu friend IDs, tự động loại bỏ trùng lặp
            Set<String> friendIds = new HashSet<>();

            // Xử lý kết quả từ query 1 (lấy friendId)
            QuerySnapshot result1 = results.get(0); // Kết quả của query1
            for (DocumentSnapshot doc : result1) {
                Friend friend = doc.toObject(Friend.class);
                if (friend != null && friend.getFriendId() != null) {
                    friendIds.add(friend.getFriendId());
                }
            }

            // Xử lý kết quả từ query 2 (lấy userId, vì friendId là userId hiện tại)
            QuerySnapshot result2 = results.get(1); // Kết quả của query2
            for (DocumentSnapshot doc : result2) {
                Friend friend = doc.toObject(Friend.class);
                if (friend != null && friend.getUserId() != null) {
                    friendIds.add(friend.getUserId());
                }
            }

            // Loại bỏ chính userId khỏi danh sách bạn bè (nếu vô tình thêm vào)
            friendIds.remove(userId);

            // Nếu không có ID bạn bè nào, trả về danh sách rỗng
            if (friendIds.isEmpty()) {
                Log.d(TAG, "Không tìm thấy bạn bè nào cho user: " + userId);
                callback.onSuccess(new ArrayList<>());
                return;
            }

            Log.d(TAG, "Danh sách friendIds cần lấy thông tin ("+ friendIds.size() +"): " + friendIds);

            // Firestore giới hạn query 'whereIn' tối đa 30 phần tử (kể từ tháng 11/2023)
            // Nếu có khả năng danh sách bạn bè > 30, cần chia nhỏ query
            List<String> friendIdList = new ArrayList<>(friendIds);
            fetchUsersInBatches(friendIdList, callback); // Gọi hàm xử lý batch

        }).addOnFailureListener(e -> {
            // Xử lý lỗi nếu một trong hai query ban đầu thất bại
            Log.e(TAG, "Lỗi khi thực hiện query bạn bè", e);
            callback.onFailure(e);
        });
    }

    /**
     * Hàm hỗ trợ lấy thông tin người dùng theo từng batch (nhóm) ID,
     * để tránh giới hạn 30 phần tử của Firestore cho toán tử 'whereIn'.
     *
     * @param allFriendIds Danh sách đầy đủ các ID bạn bè cần lấy thông tin.
     * @param finalCallback Callback cuối cùng để trả về danh sách User tổng hợp.
     */
    private void fetchUsersInBatches(List<String> allFriendIds, FirestoreCallback<List<User>> finalCallback) {
        if (allFriendIds == null || allFriendIds.isEmpty()) {
            finalCallback.onSuccess(new ArrayList<>());
            return;
        }

        List<User> combinedUserList = new ArrayList<>();
        List<Task<QuerySnapshot>> fetchTasks = new ArrayList<>();
        int batchSize = 30; // Giới hạn của Firestore 'whereIn'

        for (int i = 0; i < allFriendIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allFriendIds.size());
            // Lấy sublist một cách an toàn
            if (i >= end) continue; // Bỏ qua nếu chỉ số bắt đầu >= kết thúc
            List<String> batchIds = allFriendIds.subList(i, end);

            if (!batchIds.isEmpty()) {
                Log.d(TAG, "Đang tạo batch query cho IDs: " + batchIds);
                // Query batch hiện tại bằng FieldPath.documentId() cho hiệu quả
                Task<QuerySnapshot> batchTask = db.collection(USERS_COLLECTION)
                        .whereIn(FieldPath.documentId(), batchIds)
                        .get();
                fetchTasks.add(batchTask);
            }
        }

        // Nếu không có task nào (ví dụ list ID rỗng ban đầu), trả về list rỗng
        if (fetchTasks.isEmpty()) {
            finalCallback.onSuccess(combinedUserList);
            return;
        }

        // Chờ tất cả các batch query hoàn thành
        Tasks.whenAllSuccess(fetchTasks).addOnSuccessListener(results -> {
            Log.d(TAG, "Tất cả các batch query user đã hoàn thành.");
            // results là List<Object>, mỗi object là một QuerySnapshot
            for (Object snapshotList : results) {
                if (snapshotList instanceof QuerySnapshot) {
                    QuerySnapshot querySnapshot = (QuerySnapshot) snapshotList;
                    for (DocumentSnapshot userDoc : querySnapshot) {
                        User user = userDoc.toObject(User.class);
                        if (user != null) {
                            // Rất quan trọng: Set UID từ document ID
                            // vì toObject() không phải lúc nào cũng map document ID vào field 'uid'
                            user.setUid(userDoc.getId());
                            combinedUserList.add(user);
                        } else {
                            Log.w(TAG, "Không thể chuyển đổi DocumentSnapshot thành User: " + userDoc.getId());
                        }
                    }
                } else {
                    Log.w(TAG, "Kết quả trả về không phải là QuerySnapshot: " + snapshotList);
                }
            }
            Log.d(TAG, "Lấy thông tin user theo batch thành công. Tổng số lượng: " + combinedUserList.size());
            finalCallback.onSuccess(combinedUserList); // Trả về kết quả tổng hợp

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi lấy thông tin users theo batch", e);
            finalCallback.onFailure(e); // Trả về lỗi
        });
    }

    public void unfriend(String userId, String friendId) {
        deleteRelation(userId, friendId);
        deleteRelation(friendId, userId);
    }

    private void deleteRelation(String userId, String friendId) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("friendId", friendId)
                .get()
                .addOnSuccessListener(docs -> {
                    for (DocumentSnapshot doc : docs) {
                        db.collection(COLLECTION_NAME).document(doc.getId()).delete();
                    }
                });
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
