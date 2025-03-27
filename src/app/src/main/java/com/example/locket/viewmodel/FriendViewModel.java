package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.locket.model.Friend;
import com.example.locket.data.FriendRepository;
import com.example.locket.model.User;

import java.util.List;

public class FriendViewModel extends ViewModel {
    private FriendRepository friendRepository = new FriendRepository();
    private final MutableLiveData<List<User>> friends = new MutableLiveData<>();
    public LiveData<List<User>> getFriends() {
        return friends;
    }

    public void loadFriends(String userId) {
        friendRepository.getFriendList(userId, new FriendRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                friends.setValue(data);
            }
            @Override
            public void onFailure(Exception e) {
                friends.setValue(null);
            }
        });
    }

    public void sendFriendRequest(String fromUid, String toUid) {
        friendRepository.sendFriendRequest(fromUid, toUid);
    }

    public void acceptFriendRequest(String currentUserId, String requesterId) {
        friendRepository.acceptFriendRequest(currentUserId, requesterId);
    }

    public void unfriend(String userId, String friendId) {
        friendRepository.unfriend(userId, friendId);
    }
}

