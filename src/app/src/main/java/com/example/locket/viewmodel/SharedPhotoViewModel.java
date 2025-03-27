package com.example.locket.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.locket.data.SharedPhotoRepository;
import com.example.locket.model.User;

import java.util.List;

public class SharedPhotoViewModel extends ViewModel {
    private final SharedPhotoRepository repository = new SharedPhotoRepository();

    public void sharePhoto(String photoId, String senderId, List<User> allFriends, List<String> selectedFriendIds) {
        if (selectedFriendIds == null || selectedFriendIds.isEmpty()) {
            for (User friend : allFriends) {
                if (!friend.getUid().equals(senderId)) {
                    repository.sharePhotoToUser(photoId, senderId, friend.getUid());
                }
            }
        } else {
            for (String receiverId : selectedFriendIds) {
                repository.sharePhotoToUser(photoId, senderId, receiverId);
            }
        }
    }
}
