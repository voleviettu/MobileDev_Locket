package com.example.locket.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.locket.model.Photo;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class PhotoRepository {
    private static final String TAG = "PhotoRepository";
    private static final String COLLECTION_NAME = "photos";
    private final String USER_ID_FIELD = "userId"; // Tên trường user ID trong document ảnh
    private final String CREATED_AT_FIELD = "createdAt";

    private static final int FIRESTORE_WHERE_IN_LIMIT = 30;
    private final FirebaseFirestore db;

    public PhotoRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void uploadAndSavePhoto(Context context, Uri fileUri, String userId, String caption, String musicUrl, String location, FirestoreCallback<String> callback) {
        new Thread(() -> {
            try {
                String imageUrl = CloudinaryUploader.uploadImage(context, fileUri);
                if (imageUrl == null) {
                    callback.onFailure(new Exception("Upload ảnh lên Cloudinary thất bại"));
                    return;
                }

                String photoId = UUID.randomUUID().toString();
                Photo newPhoto = new Photo(photoId, userId, imageUrl, caption, musicUrl, location);

                db.collection(COLLECTION_NAME)
                        .document(photoId)
                        .set(newPhoto)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Lưu Photo vào Firestore thành công");
                            callback.onSuccess(photoId);
                        })
                        .addOnFailureListener(callback::onFailure);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        }).start();
    }

    public void getPhotosByUser(String userId, FirestoreCallback<List<Photo>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Photo> photoList = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        Photo photo = doc.toObject(Photo.class);
                        photoList.add(photo);
                    }
                    callback.onSuccess(photoList);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getPhotosByIds(List<String> photoIds, FirestoreCallback<List<Photo>> callback) {
        if (photoIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        db.collection(COLLECTION_NAME)
                .whereIn("photoId", photoIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Photo> photos = new ArrayList<>();
                    for (var doc : queryDocumentSnapshots) {
                        Photo photo = doc.toObject(Photo.class);
                        photos.add(photo);
                    }

                    photos.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                    callback.onSuccess(photos);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void deletePhotoById(String photoId, FirestoreCallback<Void> callback) {
        db.collection("photos").document(photoId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
    public void getPhotoById(String photoId, FirestoreCallback<Photo> callback) {
        if (photoId == null || photoId.isEmpty()) {
            callback.onFailure(new IllegalArgumentException("photoId không được null hoặc rỗng"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(photoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Photo photo = documentSnapshot.toObject(Photo.class);
                        if (photo != null) {
                            Log.d(TAG, "Lấy ảnh thành công: " + photoId);
                            callback.onSuccess(photo);
                        } else {
                            callback.onFailure(new Exception("Không thể chuyển đổi dữ liệu ảnh"));
                        }
                    } else {
                        callback.onFailure(new Exception("Không tìm thấy ảnh với photoId: " + photoId));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi lấy ảnh: " + e.getMessage());
                    callback.onFailure(e);
                });
    }
    @NonNull
    public List<Photo> getPhotosByIdsBlocking(List<String> photoIds) throws ExecutionException, InterruptedException {
        List<Photo> photos = new ArrayList<>();
        if (photoIds == null || photoIds.isEmpty()) {
            return photos; // Trả về list rỗng nếu không có ID nào
        }

        // Chia photoIds thành các batch nhỏ hơn để tránh giới hạn của Firestore 'in' query
        for (int i = 0; i < photoIds.size(); i += FIRESTORE_WHERE_IN_LIMIT) {
            int end = Math.min(i + FIRESTORE_WHERE_IN_LIMIT, photoIds.size());
            List<String> batchIds = photoIds.subList(i, end);

            if (batchIds.isEmpty()) continue; // Bỏ qua batch rỗng (dù không nên xảy ra)

            // Truy vấn batch hiện tại
            Task<QuerySnapshot> task = db.collection(COLLECTION_NAME)
                    .whereIn(FieldPath.documentId(), batchIds) // Lấy các document có ID trong batch
                    .get();

            QuerySnapshot snapshot = Tasks.await(task); // Chờ kết quả

            if (snapshot != null && !snapshot.isEmpty()) {
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    Photo photo = document.toObject(Photo.class);
                    if (photo != null) {
                        // Quan trọng: Gán ID từ document vào object Photo nếu model không tự làm
                        // photo.setPhotoId(document.getId()); // Bỏ comment nếu cần
                        photos.add(photo);
                    }
                }
            }
        }
        return photos;
    }

    /**
     * Lấy danh sách các object Photo được đăng bởi một user cụ thể, sắp xếp theo thời gian mới nhất.
     * Chỉ gọi từ background thread.
     * @param userId ID của người đăng ảnh.
     * @return Danh sách object Photo đã sắp xếp (luôn non-null, có thể rỗng).
     * @throws ExecutionException Nếu task Firebase thất bại.
     * @throws InterruptedException Nếu thread bị gián đoạn khi đang chờ.
     */
    @NonNull
    public List<Photo> getPhotosByUserBlocking(String userId) throws ExecutionException, InterruptedException {
        List<Photo> photos = new ArrayList<>();
        Task<QuerySnapshot> task = db.collection(COLLECTION_NAME)
                .whereEqualTo(USER_ID_FIELD, userId) // Lọc theo user ID
                .orderBy(CREATED_AT_FIELD, Query.Direction.DESCENDING) // Sắp xếp mới nhất trước
                .get();

        QuerySnapshot snapshot = Tasks.await(task); // Chờ kết quả

        if (snapshot != null && !snapshot.isEmpty()) {
            for (DocumentSnapshot document : snapshot.getDocuments()) {
                Photo photo = document.toObject(Photo.class);
                if (photo != null) {
                    // photo.setPhotoId(document.getId()); // Bỏ comment nếu cần
                    photos.add(photo);
                }
            }
        }
        return photos; // Danh sách đã được Firestore sắp xếp
    }

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
