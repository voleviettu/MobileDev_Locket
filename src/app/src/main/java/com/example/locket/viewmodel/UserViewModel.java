package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.locket.model.User;
import com.example.locket.data.UserRepository;

public class UserViewModel extends ViewModel {
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private UserRepository userRepository = new UserRepository();

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public void loadUser(String userId) {
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
