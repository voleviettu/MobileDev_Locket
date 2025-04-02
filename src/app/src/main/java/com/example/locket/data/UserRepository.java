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

    public Task<Void> saveUser(User user) {
        if (user == null || user.getUid() == null || user.getUid().isEmpty()) {
            Log.e(TAG, "Không thể lưu user: User hoặc UID không hợp lệ.");
            // Trả về một Task thất bại ngay lập tức để nơi gọi xử lý
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("User hoặc UID không hợp lệ"));
        }

        DocumentReference userRef = db.collection(COLLECTION_NAME).document(user.getUid());
        Log.d(TAG, "Chuẩn bị lưu user vào Firestore với UID: " + user.getUid());

        // *** Trả về Task<Void> từ phương thức set() ***
        return userRef.set(user);
    }

    public Task<Void> updateName(String uid, String firstName, String lastName) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Không thể cập nhật tên: UID không hợp lệ.");
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("UID không hợp lệ"));
        }

        DocumentReference userRef = db.collection(COLLECTION_NAME).document(uid);
        Log.d(TAG, "Chuẩn bị cập nhật tên cho UID: " + uid);

        return userRef.update(
                "firstname", firstName,
                "lastname", lastName,
                "updatedAt", FieldValue.serverTimestamp() // Cập nhật cả thời gian chỉnh sửa
        );
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

    public void getUserById(String uid, final FirestoreCallback<User> callback) {
        db.collection(COLLECTION_NAME).document(uid).get()
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

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
