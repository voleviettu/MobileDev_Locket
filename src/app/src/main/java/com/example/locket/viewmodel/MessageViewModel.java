package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.model.Message;
import com.example.locket.data.MessageRepository;

import java.util.HashMap;
import java.util.Map;

public class MessageViewModel extends ViewModel {
    private final MessageRepository messageRepository;
    private final MutableLiveData<Boolean> sendMessageSuccess;
    private final MutableLiveData<String> sendMessageError;
    private final MutableLiveData<Map<String, Message>> latestMessages;
    private final MutableLiveData<String> latestFriendId; // Thêm để lưu friendId

    public MessageViewModel() {
        this.messageRepository = new MessageRepository();
        this.sendMessageSuccess = new MutableLiveData<>();
        this.sendMessageError = new MutableLiveData<>();
        this.latestMessages = new MutableLiveData<>(new HashMap<>());
        this.latestFriendId = new MutableLiveData<>(); // Khởi tạo
    }

    public void sendMessage(Message message) {
        messageRepository.sendMessage(
                message,
                messageId -> {
                    sendMessageSuccess.postValue(true);
                    latestFriendId.postValue(message.getReceiverId()); // Lưu friendId khi gửi thành công
                },
                e -> sendMessageError.postValue(e.getMessage())
        );
    }

    public void getLatestMessage(String userId1, String userId2, String friendId) {
        messageRepository.getLatestMessage(
                userId1,
                userId2,
                message -> {
                    Map<String, Message> currentMessages = latestMessages.getValue();
                    if (currentMessages == null) {
                        currentMessages = new HashMap<>();
                    }
                    currentMessages.put(friendId, message);
                    latestMessages.postValue(currentMessages);
                },
                e -> sendMessageError.postValue(e.getMessage())
        );
    }

    public LiveData<Boolean> getSendMessageSuccess() {
        return sendMessageSuccess;
    }

    public LiveData<String> getSendMessageError() {
        return sendMessageError;
    }

    public LiveData<Map<String, Message>> getLatestMessages() {
        return latestMessages;
    }

    public LiveData<String> getLatestFriendId() {
        return latestFriendId;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        messageRepository.shutdown();
    }
}