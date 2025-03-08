package com.example.locket.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.locket.model.User;
import com.example.locket.data.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private static MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<List<User>> friendsList = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<User>> getFriendsList() {
        return friendsList;
    }

    public void loadUser(String userId) {
        if (currentUser.getValue() == null) {
            userRepository.getUserById(userId, new UserRepository.FirestoreCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    currentUser.setValue(user);
                }
                @Override
                public void onFailure(Exception e) {
                    currentUser.setValue(null);
                }
            });
        }
    }

    public void loadFriends(String userId) {
        userRepository.getFriendsList(userId, new UserRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> friends) {
                friendsList.setValue(friends);
            }
            @Override
            public void onFailure(Exception e) {
                friendsList.setValue(new ArrayList<>());
            }
        });
    }
}

