package com.example.locket.data;

import com.example.locket.model.Message;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
                        // Lưu ID của tin nhắn vào đối tượng Message
                        message.setId(documentReference.getId());
                        onSuccess.onSuccess(documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseFirestoreException) {
                            onFailure.onFailure(e);
                        } else {
                            onFailure.onFailure(new Exception("Lỗi không xác định khi gửi tin nhắn", e));
                        }
                    });
        });
    }

    // Callback interfaces
    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }

    // Đóng ExecutorService khi không cần nữa
    public void shutdown() {
        executorService.shutdown();
    }
}
