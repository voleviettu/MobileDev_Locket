package com.example.locket.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.example.locket.model.Photo;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PhotoRepository {
    private static final String TAG = "PhotoRepository";
    private static final String COLLECTION_NAME = "photos";
    private FirebaseFirestore db;

    public PhotoRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void uploadAndSavePhoto(Context context, Uri fileUri, String userId, String caption, String musicUrl, GeoPoint location, List<String> receivers, FirestoreCallback<String> callback) {
        new Thread(() -> {
            try {
                // dùng hàm từ cloudinaryUploader
                String imageUrl = CloudinaryUploader.uploadImage(context, fileUri);
                if (imageUrl == null) {
                    callback.onFailure(new Exception("Upload ảnh lên Cloudinary thất bại"));
                    return;
                }

                String photoId = UUID.randomUUID().toString();
                Photo newPhoto = new Photo(photoId, userId, imageUrl, caption, musicUrl, location, receivers);

                // lưu thông tin ảnh vào Firestore
                db.collection(COLLECTION_NAME)
                        .document(photoId)
                        .set(newPhoto)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Lưu Photo vào Firestore thành công");
                            callback.onSuccess(imageUrl);
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

    public interface FirestoreCallback<T> {
        void onSuccess(T data);
        void onFailure(Exception e);
    }
}
