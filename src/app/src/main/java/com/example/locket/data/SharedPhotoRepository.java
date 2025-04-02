package com.example.locket.data;

import android.util.Log;
import com.example.locket.model.SharedPhoto;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SharedPhotoRepository {
    private static final String TAG = "SharedPhotoRepository";
    private static final String COLLECTION_NAME = "shared_photos";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void sharePhotoToUser(String photoId, String senderId, String receiverId) {
        SharedPhoto sharedPhoto = new SharedPhoto(photoId, senderId, receiverId);
        db.collection(COLLECTION_NAME).add(sharedPhoto)
                .addOnSuccessListener(docRef -> Log.d(TAG, "Ảnh đã chia sẻ tới: " + receiverId))
                .addOnFailureListener(e -> Log.e(TAG, "Lỗi chia sẻ ảnh", e));
    }
    public void getSharedPhotos(String userId, PhotoRepository.FirestoreCallback<List<String>> callback) {
        db.collection(COLLECTION_NAME)
                .whereIn("receiverId", List.of(userId))
                .get()
                .addOnSuccessListener(receiverSnapshots -> {
                    List<String> photoIds = new ArrayList<>();

                    for (var doc : receiverSnapshots) {
                        SharedPhoto sharedPhoto = doc.toObject(SharedPhoto.class);
                        photoIds.add(sharedPhoto.getPhotoId());
                    }

                    db.collection(COLLECTION_NAME)
                            .whereEqualTo("senderId", userId)
                            .get()
                            .addOnSuccessListener(senderSnapshots -> {
                                for (var doc : senderSnapshots) {
                                    SharedPhoto sharedPhoto = doc.toObject(SharedPhoto.class);
                                    if (!photoIds.contains(sharedPhoto.getPhotoId())) {
                                        photoIds.add(sharedPhoto.getPhotoId());
                                    }
                                }
                                callback.onSuccess(photoIds);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

}
