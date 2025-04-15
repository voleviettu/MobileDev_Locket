package com.example.locket.data;

import com.example.locket.model.Message;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageRepository {
    private final FirebaseFirestore db;
    private final ExecutorService executorService;

    public MessageRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void sendMessage(Message message, OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        executorService.execute(() -> {
            db.collection("messages")
                    .add(message)
                    .addOnSuccessListener(documentReference -> {
                        String messageId = documentReference.getId();
                        documentReference.update("id", messageId)
                                .addOnSuccessListener(aVoid -> {
                                    message.setId(messageId);
                                    onSuccess.onSuccess(messageId);
                                })
                                .addOnFailureListener(onFailure::onFailure);
                    })
                    .addOnFailureListener(onFailure::onFailure);
        });
    }

    public void getLatestMessage(String userId1, String userId2, OnSuccessListener<Message> onSuccess, OnFailureListener onFailure) {
        executorService.execute(() -> {
            db.collection("messages")
                    .whereIn("senderId", java.util.Arrays.asList(userId1, userId2))
                    .whereIn("receiverId", java.util.Arrays.asList(userId1, userId2))
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Message latestMessage = queryDocumentSnapshots.getDocuments().get(0).toObject(Message.class);
                            onSuccess.onSuccess(latestMessage);
                        } else {
                            onSuccess.onSuccess(null); // Không có tin nhắn
                        }
                    })
                    .addOnFailureListener(onFailure::onFailure);
        });
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
