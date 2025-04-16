package com.example.locket.utils;

import android.util.Log;
import com.example.locket.model.PhotoReaction;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class ReactionListener {
    private static final String TAG = "ReactionListener";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    public void listenForReactions(String userId, OnNewReactionListener callback) {
        Log.d(TAG, "Bắt đầu lắng nghe reactions cho user: " + userId);
        Query query = db.collection("reactions")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10);

        listener = query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e(TAG, "Lỗi lắng nghe Firestore: ", error);
                return;
            }

            Log.d(TAG, "Nhận snapshot với " + snapshots.getDocuments().size() + " documents");
            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    Log.d(TAG, "Document change type: " + dc.getType());
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        PhotoReaction reaction = dc.getDocument().toObject(PhotoReaction.class);
                        Log.d(TAG, "Reaction mới: photoId=" + reaction.getPhotoId() + ", reaction=" + reaction.getReaction());
                        db.collection("photos")
                                .document(reaction.getPhotoId())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String photoOwnerId = documentSnapshot.getString("userId");
                                    Log.d(TAG, "Photo owner: " + photoOwnerId + ", Current user: " + userId);
                                    if (userId.equals(photoOwnerId)) {
                                        Log.d(TAG, "Gọi callback cho reaction: " + reaction.getReaction());
                                        callback.onNewReaction(reaction);
                                    } else {
                                        Log.d(TAG, "Reaction không thuộc user hiện tại");
                                    }
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Lỗi kiểm tra photo: ", e));
                    }
                }
            }
        });
    }

    public void stopListening() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }

    public interface OnNewReactionListener {
        void onNewReaction(PhotoReaction reaction);
    }
}