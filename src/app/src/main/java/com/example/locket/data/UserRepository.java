package com.example.locket.data;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.locket.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public Task<Void> updateAvatar(String uid, String avatarUrl) {
        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Không thể cập nhật avatar: UID không hợp lệ.");
            return com.google.android.gms.tasks.Tasks.forException(new IllegalArgumentException("UID không hợp lệ"));
        }

        DocumentReference userRef = db.collection(COLLECTION_NAME).document(uid);
        Log.d(TAG, "Chuẩn bị cập nhật avatar cho UID: " + uid);

        return userRef.update(
                "avatar", avatarUrl,
                "updatedAt", FieldValue.serverTimestamp()
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

    @Nullable
    public User getUserByIdBlocking(String userId) throws ExecutionException, InterruptedException {
        Task<DocumentSnapshot> task = db.collection(COLLECTION_NAME).document(userId).get();
        // Block thread hiện tại và chờ kết quả
        DocumentSnapshot snapshot = Tasks.await(task);
        if (snapshot != null && snapshot.exists()) {
            User user = snapshot.toObject(User.class);
            // Quan trọng: Gán UID từ document ID nếu model User không tự làm
            // if (user != null) { user.setUid(snapshot.getId()); }
            return user;
        }
        return null; // Không tìm thấy hoặc lỗi
    }

    /**
     * Lấy tất cả Users một cách đồng bộ.
     * Chú ý: Chỉ gọi từ background thread.
     * Ném ra Exception nếu task thất bại hoặc bị gián đoạn.
     */
    @NonNull
    public List<User> getAllUsersBlocking() throws ExecutionException, InterruptedException {
        Task<QuerySnapshot> task = db.collection(COLLECTION_NAME).get();
        // Block thread hiện tại và chờ kết quả
        QuerySnapshot snapshot = Tasks.await(task);
        List<User> users = new ArrayList<>();
        if (snapshot != null) {
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                User user = document.toObject(User.class);
                if (user != null) {
                    // Quan trọng: Gán UID nếu cần
                    // user.setUid(document.getId());
                    users.add(user);
                }
            }
        }
        return users;
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
