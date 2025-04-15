package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.model.Message;
import com.example.locket.data.MessageRepository;

public class MessageViewModel extends ViewModel {
    private final MessageRepository messageRepository;
    private final MutableLiveData<Boolean> sendMessageSuccess;
    private final MutableLiveData<String> sendMessageError;

    public MessageViewModel() {
        this.messageRepository = new MessageRepository();
        this.sendMessageSuccess = new MutableLiveData<>();
        this.sendMessageError = new MutableLiveData<>();
    }

    public void sendMessage(Message message) {
        messageRepository.sendMessage(
                message,
                messageId -> {
                    // Thành công: Gửi tín hiệu thành công qua LiveData
                    sendMessageSuccess.postValue(true);
                },
                e -> {
                    // Thất bại: Gửi lỗi qua LiveData
                    sendMessageError.postValue(e.getMessage());
                }
        );
    }

    public LiveData<Boolean> getSendMessageSuccess() {
        return sendMessageSuccess;
    }

    public LiveData<String> getSendMessageError() {
        return sendMessageError;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        messageRepository.shutdown();
    }
}
