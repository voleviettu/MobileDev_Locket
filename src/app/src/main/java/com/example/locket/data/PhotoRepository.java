package com.example.locket.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.locket.model.Photo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhotoRepository {
    private static final String TAG = "PhotoRepository";
    private static final String COLLECTION_NAME = "photos";
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

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
