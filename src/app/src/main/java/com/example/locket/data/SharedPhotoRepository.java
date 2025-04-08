package com.example.locket.data;

import android.util.Log;
import com.example.locket.model.SharedPhoto;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void deleteSharedPhotosByPhotoId(String photoId, PhotoRepository.FirestoreCallback<Void> callback) {
        db.collection("shared_photos")
                .whereEqualTo("photoId", photoId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    int total = query.size();
                    int[] deleted = {0};
                    boolean[] hasFailed = {false};

                    for (var doc : query) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deleted[0]++;
                                    if (deleted[0] == total && !hasFailed[0]) {
                                        callback.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!hasFailed[0]) {
                                        hasFailed[0] = true;
                                        callback.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteSharedPhotoByReceiver(String photoId, String receiverId, PhotoRepository.FirestoreCallback<Void> callback) {
        db.collection("shared_photos")
                .whereEqualTo("photoId", photoId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        callback.onSuccess(null);
                        return;
                    }

                    int total = query.size();
                    int[] deleted = {0};
                    boolean[] hasFailed = {false};

                    for (var doc : query) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    deleted[0]++;
                                    if (deleted[0] == total && !hasFailed[0]) {
                                        callback.onSuccess(null);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!hasFailed[0]) {
                                        hasFailed[0] = true;
                                        callback.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void getPhotosSharedByFriend(String senderId, String receiverId, PhotoRepository.FirestoreCallback<List<String>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("senderId", senderId)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> photoIds = new ArrayList<>();
                    for (var doc : querySnapshot) {
                        SharedPhoto sharedPhoto = doc.toObject(SharedPhoto.class);
                        photoIds.add(sharedPhoto.getPhotoId());
                    }
                    callback.onSuccess(photoIds);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void getPhotosExchangedBetweenUsers(String userId1, String userId2, PhotoRepository.FirestoreCallback<List<String>> callback) {
        db.collection(COLLECTION_NAME)
                .whereIn("senderId", Arrays.asList(userId1, userId2))
                .whereIn("receiverId", Arrays.asList(userId1, userId2))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> photoIds = new ArrayList<>();
                    for (var doc : querySnapshot) {
                        SharedPhoto sharedPhoto = doc.toObject(SharedPhoto.class);
                        if ((sharedPhoto.getSenderId().equals(userId1) && sharedPhoto.getReceiverId().equals(userId2)) ||
                                (sharedPhoto.getSenderId().equals(userId2) && sharedPhoto.getReceiverId().equals(userId1))) {
                            if (!photoIds.contains(sharedPhoto.getPhotoId())) {
                                photoIds.add(sharedPhoto.getPhotoId());
                            }
                        }
                    }
                    callback.onSuccess(photoIds);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public void getPhotosSharedWithMeGroupedBySender(String userId, PhotoRepository.FirestoreCallback<Map<String, List<String>>> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("receiverId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, List<String>> photosBySender = new HashMap<>();

                    for (var doc : querySnapshot) {
                        SharedPhoto sharedPhoto = doc.toObject(SharedPhoto.class);
                        String senderId = sharedPhoto.getSenderId();
                        String photoId = sharedPhoto.getPhotoId();

                        if (!photosBySender.containsKey(senderId)) {
                            photosBySender.put(senderId, new ArrayList<>());
                        }
                        photosBySender.get(senderId).add(photoId);
                    }

                    callback.onSuccess(photosBySender);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
