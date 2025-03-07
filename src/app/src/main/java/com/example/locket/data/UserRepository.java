package com.example.locket.data;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.locket.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private static final String COLLECTION_NAME = "users";
    private FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void saveUser(User user) {
        db.collection(COLLECTION_NAME)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User đã được lưu thành công!"))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi khi lưu User", e));
    }

    public void getAllUsers(final FirestoreCallback<List<User>> callback) {
        db.collection(COLLECTION_NAME).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> userList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            userList.add(user);
                        }
                        callback.onSuccess(userList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void getUserById(String userId, final FirestoreCallback<User> callback) {
        db.collection(COLLECTION_NAME).document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User không tồn tại!"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getFriendsList(String userId, final FirestoreCallback<List<User>> callback) {
        getUserById(userId, new FirestoreCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user.getFriends() == null || user.getFriends().isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<User> friendsList = new ArrayList<>();
                List<String> friendIds = user.getFriends();
                for (String friendId : friendIds) {
                    db.collection(COLLECTION_NAME).document(friendId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    User friend = documentSnapshot.toObject(User.class);
                                    friendsList.add(friend);
                                }
                                if (friendsList.size() == friendIds.size()) {
                                    callback.onSuccess(friendsList);
                                }
                            })
                            .addOnFailureListener(callback::onFailure);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
