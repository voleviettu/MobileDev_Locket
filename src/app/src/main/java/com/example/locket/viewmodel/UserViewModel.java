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

    private MutableLiveData<List<User>> allUsers = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
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
    public LiveData<List<User>> getAllUsers() {
        userRepository.getAllUsers(new UserRepository.FirestoreCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.setValue(users);
            }

            @Override
            public void onFailure(Exception e) {
                allUsers.setValue(null);
            }
        });
        return allUsers;
    }
}

