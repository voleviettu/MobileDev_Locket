package com.example.locket;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;

import com.example.locket.viewmodel.UserViewModel;

public class MyApplication extends Application {
    private UserViewModel userViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        userViewModel = new ViewModelProvider.AndroidViewModelFactory(this)
                .create(UserViewModel.class);
    }

    public UserViewModel getUserViewModel() {
        return userViewModel;
    }
}
