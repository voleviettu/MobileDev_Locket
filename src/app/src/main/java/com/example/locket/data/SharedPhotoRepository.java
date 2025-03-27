package com.example.locket.data;

import android.util.Log;
import com.example.locket.model.SharedPhoto;
import com.google.firebase.firestore.FirebaseFirestore;

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
}
