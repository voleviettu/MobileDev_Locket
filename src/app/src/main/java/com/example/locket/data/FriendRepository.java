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
        // Kiểm tra xem đã tồn tại relationship chưa (tránh trùng lặp)
        checkExistingRelationship(fromUid, toUid, new FirestoreCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean relationshipExists) { // Đổi tên tham số cho rõ ràng
                // Thêm kiểm tra null cho Boolean object (an toàn hơn)
                if (relationshipExists != null && !relationshipExists) {
                    Log.d(TAG, "Không có relationship, tiến hành tạo mới.");
                    Friend sender = new Friend(fromUid, toUid, "pending"); // Người gửi thấy "pending"
                    Friend receiver = new Friend(toUid, fromUid, "requested"); // Người nhận thấy "requested"

                    // Sử dụng WriteBatch
                    WriteBatch batch = db.batch();
                    DocumentReference senderRef = db.collection(COLLECTION_NAME).document();
                    DocumentReference receiverRef = db.collection(COLLECTION_NAME).document();

                    batch.set(senderRef, sender);
                    batch.set(receiverRef, receiver);

                    batch.commit()
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Đã gửi lời mời kết bạn và tạo bản ghi cho người nhận thành công (batch)."))
                            .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi gửi lời mời kết bạn (batch)", e));
                } else if (relationshipExists != null && relationshipExists) {
                    // Đã tồn tại relationship
                    Log.d(TAG, "Relationship giữa " + fromUid + " và " + toUid + " đã tồn tại.");
                    // TODO: Có thể thông báo cho người dùng ở đây thông qua ViewModel/UI
                    // Ví dụ: gọi một callback khác onFailure hoặc một callback riêng cho trường hợp "đã tồn tại"
                } else {
                    // Trường hợp relationshipExists là null (không nên xảy ra nếu checkExistingRelationship đúng)
                    Log.w(TAG, "checkExistingRelationship callback onSuccess trả về null.");
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Xử lý lỗi khi kiểm tra relationship thất bại
                Log.e(TAG, "Lỗi khi kiểm tra relationship trước khi gửi yêu cầu", e);
                // TODO: Thông báo lỗi cho người dùng qua ViewModel/UI
            }
        });
    }

    private void checkExistingRelationship(String userId1, String userId2, FirestoreCallback<Boolean> callback) {
        if (userId1 == null || userId1.isEmpty() || userId2 == null || userId2.isEmpty()) {
            Log.w(TAG, "checkExistingRelationship: Invalid user IDs provided.");
            callback.onFailure(new IllegalArgumentException("User IDs cannot be null or empty"));
            return;
        }

        // Query 1: User1 -> User2
        Task<QuerySnapshot> query1 = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId1)
                .whereEqualTo("friendId", userId2)
                .limit(1) // Chỉ cần biết có tồn tại hay không
                .get();

        // Query 2: User2 -> User1
        Task<QuerySnapshot> query2 = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId2)
                .whereEqualTo("friendId", userId1)
                .limit(1) // Chỉ cần biết có tồn tại hay không
                .get();

        // Sử dụng whenAllComplete để xử lý cả thành công và thất bại của từng task riêng lẻ
        // Hoặc tiếp tục dùng whenAllSuccess và xử lý lỗi chung
        Tasks.whenAllSuccess(query1, query2).addOnSuccessListener(results -> {
            try {
                // Kiểm tra kết quả trả về hợp lệ
                if (results == null || results.size() < 2 || !(results.get(0) instanceof QuerySnapshot) || !(results.get(1) instanceof QuerySnapshot) ) {
                    Log.e(TAG, "checkExistingRelationship: Invalid results received from Tasks.whenAllSuccess");
                    callback.onFailure(new IllegalStateException("Invalid query results"));
                    return;
                }

                QuerySnapshot snapshot1 = (QuerySnapshot) results.get(0);
                QuerySnapshot snapshot2 = (QuerySnapshot) results.get(1);

                // Kiểm tra xem có tài liệu nào trong bất kỳ snapshot nào không
                boolean exists = (snapshot1 != null && !snapshot1.isEmpty()) ||
                        (snapshot2 != null && !snapshot2.isEmpty());

                Log.d(TAG, "Relationship check between " + userId1 + " and " + userId2 + ": " + (exists ? "Exists" : "Does not exist"));
                callback.onSuccess(exists);

            } catch (Exception e) {
                // Bắt các lỗi không mong muốn trong quá trình xử lý kết quả
                Log.e(TAG, "Error processing relationship check results", e);
                callback.onFailure(e);
            }
        }).addOnFailureListener(e -> {
            // Lỗi xảy ra khi một trong các query thất bại
            Log.e(TAG, "Lỗi khi thực hiện query kiểm tra relationship", e);
            callback.onFailure(e);
        });
    }

    public void acceptFriendRequest(String currentUserId, String requesterId, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Chấp nhận yêu cầu từ: " + requesterId + " bởi: " + currentUserId);
        // Tìm bản ghi của người nhận (currentUserId)
        Task<QuerySnapshot> receiverQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", requesterId)
                .whereEqualTo("status", "requested") // Chỉ chấp nhận nếu đang ở trạng thái 'requested'
                .limit(1)
                .get();

        // Tìm bản ghi của người gửi (requesterId)
        Task<QuerySnapshot> senderQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", requesterId)
                .whereEqualTo("friendId", currentUserId)
                .whereEqualTo("status", "pending") // Chỉ chấp nhận nếu người gửi đang ở trạng thái 'pending'
                .limit(1)
                .get();

        Tasks.whenAllSuccess(receiverQuery, senderQuery).addOnSuccessListener(results -> {
            QuerySnapshot receiverSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot senderSnapshot = (QuerySnapshot) results.get(1);

            if (receiverSnapshot.isEmpty() || senderSnapshot.isEmpty()) {
                Log.w(TAG, "Không tìm thấy đầy đủ bản ghi yêu cầu/pending để chấp nhận.");
                callback.onFailure(new Exception("Không tìm thấy bản ghi yêu cầu hợp lệ."));
                return;
            }

            DocumentReference receiverDocRef = receiverSnapshot.getDocuments().get(0).getReference();
            DocumentReference senderDocRef = senderSnapshot.getDocuments().get(0).getReference();

            WriteBatch batch = db.batch();
            batch.update(receiverDocRef, "status", "accepted");
            batch.update(senderDocRef, "status", "accepted");

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Chấp nhận yêu cầu thành công (batch).");
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Lỗi khi chấp nhận yêu cầu (batch)", e);
                        callback.onFailure(e);
                    });

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi query để chấp nhận lời mời", e);
            callback.onFailure(e);
        });
    }

    public void declineFriendRequest(String currentUserId, String requesterId, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Từ chối yêu cầu từ: " + requesterId + " bởi: " + currentUserId);
        // Tìm bản ghi của người nhận (currentUserId) với status "requested"
        Task<QuerySnapshot> receiverQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("friendId", requesterId)
                .whereEqualTo("status", "requested")
                .limit(1)
                .get();

        // Tìm bản ghi của người gửi (requesterId) với status "pending"
        Task<QuerySnapshot> senderQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", requesterId)
                .whereEqualTo("friendId", currentUserId)
                .whereEqualTo("status", "pending")
                .limit(1)
                .get();

        Tasks.whenAllSuccess(receiverQuery, senderQuery).addOnSuccessListener(results -> {
            QuerySnapshot receiverSnapshot = (QuerySnapshot) results.get(0);
            QuerySnapshot senderSnapshot = (QuerySnapshot) results.get(1);

            // Có thể chỉ tìm thấy 1 trong 2 nếu có lỗi trước đó, nhưng vẫn nên xóa nếu tìm thấy
            WriteBatch batch = db.batch();
            boolean foundAny = false;

            if (!receiverSnapshot.isEmpty()) {
                DocumentReference receiverDocRef = receiverSnapshot.getDocuments().get(0).getReference();
                batch.delete(receiverDocRef);
                foundAny = true;
                Log.d(TAG, "Đã thêm lệnh xóa cho bản ghi người nhận (requested).");
            } else {
                Log.w(TAG, "Không tìm thấy bản ghi 'requested' để từ chối.");
            }

            if (!senderSnapshot.isEmpty()) {
                DocumentReference senderDocRef = senderSnapshot.getDocuments().get(0).getReference();
                batch.delete(senderDocRef);
                foundAny = true;
                Log.d(TAG, "Đã thêm lệnh xóa cho bản ghi người gửi (pending).");
            } else {
                Log.w(TAG, "Không tìm thấy bản ghi 'pending' để từ chối.");
            }

            if (foundAny) {
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Từ chối/xóa yêu cầu thành công (batch).");
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi từ chối/xóa yêu cầu (batch)", e);
                            callback.onFailure(e);
                        });
            } else {
                Log.w(TAG, "Không tìm thấy bản ghi nào để từ chối/xóa.");
                // Có thể gọi onSuccess vì không có gì để xóa, hoặc onFailure tùy logic mong muốn
                callback.onSuccess(null); // Coi như thành công vì không có gì để làm
            }

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi query để từ chối lời mời", e);
            callback.onFailure(e);
        });
    }

    public void getFriendPending(String userId, FirestoreCallback<List<User>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("friendId", userId) // Tìm những bản ghi mà user hiện tại là người được mời
                .whereEqualTo("status", "requested") // Chỉ lấy các yêu cầu đang chờ user hiện tại phản hồi
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Không có yêu cầu kết bạn nào.");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> requesterIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Friend relationship = doc.toObject(Friend.class);
                        if (relationship != null && relationship.getUserId() != null) {
                            // userId trong bản ghi này là ID của người gửi yêu cầu
                            requesterIds.add(relationship.getUserId());
                        }
                    }

                    if (requesterIds.isEmpty()) {
                        Log.d(TAG, "Danh sách requesterIds rỗng sau khi xử lý snapshot.");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    Log.d(TAG, "Danh sách requesterIds cần lấy thông tin ("+ requesterIds.size() +"): " + requesterIds);
                    // Lấy thông tin chi tiết của những người gửi yêu cầu
                    fetchUsersInBatches(requesterIds, callback); // Tái sử dụng hàm fetchUsersInBatches

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải danh sách yêu cầu kết bạn", e);
                    callback.onFailure(e);
                });
    }

    // --- Phương thức lấy danh sách YÊU CẦU KẾT BẠN ĐẾN (Mới) ---
    public void getFriendRequests(String userId, FirestoreCallback<List<User>> callback) {
        Log.d(TAG, "Đang tải danh sách yêu cầu kết bạn cho user: " + userId);
        db.collection(COLLECTION_NAME)
                .whereEqualTo("friendId", userId) // Tìm những bản ghi mà user hiện tại là người được mời
                .whereEqualTo("status", "pending") // Chỉ lấy các yêu cầu đang chờ user hiện tại phản hồi
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "Không có yêu cầu kết bạn nào.");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> requesterIds = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Friend relationship = doc.toObject(Friend.class);
                        if (relationship != null && relationship.getUserId() != null) {
                            // userId trong bản ghi này là ID của người gửi yêu cầu
                            requesterIds.add(relationship.getUserId());
                        }
                    }

                    if (requesterIds.isEmpty()) {
                        Log.d(TAG, "Danh sách requesterIds rỗng sau khi xử lý snapshot.");
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    Log.d(TAG, "Danh sách requesterIds cần lấy thông tin ("+ requesterIds.size() +"): " + requesterIds);
                    // Lấy thông tin chi tiết của những người gửi yêu cầu
                    fetchUsersInBatches(requesterIds, callback); // Tái sử dụng hàm fetchUsersInBatches

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi tải danh sách yêu cầu kết bạn", e);
                    callback.onFailure(e);
                });
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

    public void searchUsers(String query, String currentUserId, List<String> excludeIds, FirestoreCallback<List<User>> callback) {
        if (query == null || query.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Query 1: Tìm theo firstname
        Task<QuerySnapshot> searchByFirstName = db.collection(USERS_COLLECTION)
                .orderBy("firstname") // Cần có index Firestore cho trường này
                .whereGreaterThanOrEqualTo("firstname", query)
                .whereLessThanOrEqualTo("firstname", query + "\uf8ff")
                .limit(15) // Giới hạn số lượng kết quả cho mỗi query
                .get();

        // Query 2: Tìm theo lastname
        Task<QuerySnapshot> searchByLastName = db.collection(USERS_COLLECTION)
                .orderBy("lastname") // Cần có index Firestore cho trường này
                .whereGreaterThanOrEqualTo("lastname", query)
                .whereLessThanOrEqualTo("lastname", query + "\uf8ff")
                .limit(15) // Giới hạn số lượng kết quả cho mỗi query
                .get();

        // Kết hợp kết quả của các query
        Task<List<QuerySnapshot>> allSearchTasks = Tasks.whenAllSuccess(searchByFirstName, searchByLastName);

        allSearchTasks.addOnSuccessListener(results -> {
            Set<User> uniqueUsers = new HashSet<>(); // Dùng Set để tự động loại bỏ trùng lặp
            Set<String> combinedExcludeIds = new HashSet<>(excludeIds); // Kết hợp danh sách loại trừ
            combinedExcludeIds.add(currentUserId); // Luôn loại trừ người dùng hiện tại

            for (QuerySnapshot querySnapshot : results) {
                for (DocumentSnapshot doc : querySnapshot) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        user.setUid(doc.getId()); // Quan trọng: Set UID từ document ID
                        // Chỉ thêm nếu user không nằm trong danh sách loại trừ
                        if (!combinedExcludeIds.contains(user.getUid())) {
                            uniqueUsers.add(user); // Set sẽ xử lý trùng lặp nếu tìm thấy ở cả 2 query
                        }
                    }
                }
            }
            Log.d(TAG, "Search found " + uniqueUsers.size() + " unique users matching query (before limit).");
            // Chuyển Set thành List để trả về
            callback.onSuccess(new ArrayList<>(uniqueUsers));

        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error searching users", e);
            callback.onFailure(e);
        });
    }

    public void unfriend(String userId, String friendId, FirestoreCallback<Void> callback) {
        Log.d(TAG, "Hủy kết bạn giữa: " + userId + " và " + friendId);
        // Query 1: userId -> friendId
        Task<QuerySnapshot> query1 = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("friendId", friendId)
                .whereEqualTo("status", "accepted") // Chỉ xóa nếu đang là bạn bè
                .limit(1)
                .get();
        // Query 2: friendId -> userId
        Task<QuerySnapshot> query2 = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", friendId)
                .whereEqualTo("friendId", userId)
                .whereEqualTo("status", "accepted") // Chỉ xóa nếu đang là bạn bè
                .limit(1)
                .get();

        Tasks.whenAllSuccess(query1, query2).addOnSuccessListener(results -> {
            QuerySnapshot snapshot1 = (QuerySnapshot) results.get(0);
            QuerySnapshot snapshot2 = (QuerySnapshot) results.get(1);

            WriteBatch batch = db.batch();
            boolean foundAny = false;

            if (!snapshot1.isEmpty()) {
                batch.delete(snapshot1.getDocuments().get(0).getReference());
                foundAny = true;
                Log.d(TAG,"Đã thêm lệnh xóa cho bản ghi: "+ userId + " -> " + friendId);
            }
            if (!snapshot2.isEmpty()) {
                batch.delete(snapshot2.getDocuments().get(0).getReference());
                foundAny = true;
                Log.d(TAG,"Đã thêm lệnh xóa cho bản ghi: "+ friendId + " -> " + userId);
            }

            if(foundAny){
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Hủy kết bạn thành công (batch).");
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Lỗi khi hủy kết bạn (batch)", e);
                            callback.onFailure(e);
                        });
            } else {
                Log.w(TAG, "Không tìm thấy bản ghi bạn bè 'accepted' để hủy.");
                callback.onSuccess(null); // Coi như thành công vì không có gì để xóa
            }


        }).addOnFailureListener(e -> {
            Log.e(TAG, "Lỗi khi query để hủy kết bạn", e);
            callback.onFailure(e);
        });
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
