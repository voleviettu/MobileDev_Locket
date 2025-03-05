package com.example.locket.ui.main;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

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
        // userRepository = new UserRepository();
        // saveTestUser();


        // khởi tạo user cho toàn bộ src
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userViewModel.loadUser("125"); // hàm của UserRepository

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Log.d("MainActivity", "User hiện tại: " + user.getUsername());
            } else {
                Log.e("MainActivity", "Không tìm thấy user, kiểm tra Firestore!");
                saveTestUser(); // Nếu không có user, thử tạo user mới
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


    // các activity khác gọi để lấy UserViewModel
    public UserViewModel getUserViewModel() {
        return userViewModel;
    }

    // thử lưu user lên firestore, dùng khi signup
    private void saveTestUser() {
        List<String> friends = new ArrayList<>();
        friends.add("123"); // cho tạm 2 bạn
        friends.add("124");
        User testUser = new User(
                "125",
                "trantran18nbk@gmail.com",
                "",
                "trant",
                "",
                friends
        );
        userRepository.saveUser(testUser);
    }
}
