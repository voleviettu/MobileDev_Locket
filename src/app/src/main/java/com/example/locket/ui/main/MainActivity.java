package com.example.locket.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.MyApplication;
import com.example.locket.auth.WelcomeActivity;
import com.example.locket.data.UserRepository;
import com.example.locket.model.User;
import com.example.locket.ui.photo.PhotoActivity;

import com.example.locket.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private UserViewModel userViewModel;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // !!! XÓA DÒNG NÀY KHI CÓ NÚT LOGOUT THẬT !!!
         FirebaseAuth.getInstance().signOut();
         Log.d(TAG, "!!! ĐÃ GỌI signOut() TẠM THỜI TRONG MAINACTIVITY !!!");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "User chưa đăng nhập. Chuyển đến WelcomeActivity.");
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        String uid = currentUser.getUid();

        // khởi tạo user cho toàn bộ src
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        userViewModel.loadUser(uid);
        Log.d(TAG, "User đã đăng nhập (UID: " + currentUser.getUid() + "). Chuyển đến PhotoActivity.");
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("MainActivity", "User hiện tại: " + user.getUsername());
            } else {
                Log.e("MainActivity", "Chưa có user trên Firestore, tạo mới");

                User newUser = new User(
                        uid,
                        currentUser.getEmail(),
                        "", "", "", "", false
                );
                userRepository.saveUser(newUser);
            }
            startActivity(new Intent(this, PhotoActivity.class));
            finish();
        });

    }
}