package com.example.locket.data;

import android.util.Log;
import com.example.locket.model.Friend;
import com.example.locket.model.User;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class FriendRepository {
    private static final String TAG = "FriendRepository";
    private static final String COLLECTION_NAME = "friend_relationships";
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
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(friendDocs -> {
                    List<String> friendIds = new ArrayList<>();
                    for (DocumentSnapshot doc : friendDocs) {
                        Friend friend = doc.toObject(Friend.class);
                        friendIds.add(friend.getFriendId());
                    }

                    if (friendIds.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    db.collection("users")
                            .whereIn("uid", friendIds)
                            .get()
                            .addOnSuccessListener(userDocs -> {
                                List<User> userList = new ArrayList<>();
                                for (DocumentSnapshot userDoc : userDocs) {
                                    User user = userDoc.toObject(User.class);
                                    userList.add(user);
                                }
                                callback.onSuccess(userList);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
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
