package com.example.locket.data;

import com.example.locket.model.PhotoReaction;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

public class PhotoReactionRepository {

    private final CollectionReference reactionRef = FirebaseFirestore.getInstance()
            .collection("reactions");

    public void addReaction(PhotoReaction reaction, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String id = reactionRef.document().getId();
        reaction.setId(id);
        reactionRef.document(id).set(reaction)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void getReactionsForPhoto(String photoId, EventListener<QuerySnapshot> listener) {
        reactionRef.whereEqualTo("photoId", photoId)
                .addSnapshotListener(listener);
    }

    public void removeReaction(String userId, String photoId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        reactionRef.whereEqualTo("userId", userId)
                .whereEqualTo("photoId", photoId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        reactionRef.document(doc.getId()).delete()
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    }
                });
    }
}
