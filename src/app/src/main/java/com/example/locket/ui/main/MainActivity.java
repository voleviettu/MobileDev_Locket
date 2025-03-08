package com.example.locket.ui.main;

import android.app.Application;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

import com.example.locket.MyApplication;
import com.example.locket.auth.WelcomeActivity;
import com.example.locket.ui.photo.PhotoActivity;

// thử lưu user lên firestore
import com.example.locket.data.UserRepository;
import com.example.locket.model.User;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;
import com.example.locket.viewmodel.UserViewModel;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserRepository userRepository;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // thử lưu user lên firestore
        //userRepository = new UserRepository();
        //saveTestUser();


        // khởi tạo user cho toàn bộ src
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();

        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userViewModel.loadUser("2"); // hàm của UserRepository

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("MainActivity", "User hiện tại: " + user.getUsername());
            } else {
                Log.e("MainActivity", "Không tìm thấy user, kiểm tra Firestore!");
                // saveTestUser(); // Nếu không có user, thử tạo user mới
            }
        });


        boolean currentUser = true;
        if (!currentUser) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            startActivity(new Intent(this, PhotoActivity.class));
        }
        finish();
    }


    // thử lưu user lên firestore, dùng khi signup
    private void saveTestUser() {
        List<String> friends = new ArrayList<>();
         // cho tạm 2 bạn
        friends.add("2");

        User testUser = new User(
                "7",
                "ban7@gmail.com",
                "",
                "bay",
                ".",
                "ban7",
                "",
                friends
        );
        userRepository.saveUser(testUser);
    }
}
