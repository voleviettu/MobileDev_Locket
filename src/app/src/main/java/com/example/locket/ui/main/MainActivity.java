package com.example.locket.ui.main;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.MyApplication;
import com.example.locket.auth.WelcomeActivity;
import com.example.locket.ui.photo.PhotoActivity;

// thử lưu user lên firestore
import com.example.locket.data.UserRepository;
import com.example.locket.model.User;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private UserRepository userRepository;

    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // tạm logout (để test 2 cái hàm signup login ở dưới thôi, chứ k có dùng sau này)
        //FirebaseAuth.getInstance().signOut();

        // lấy user hiện tại, k có (chưa login) thì vào trang chào mừng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.d("MainActivity", "chưa có user, vào trang chào mừng");
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();

            //signup("khahhy@gmail.com", "12345678", "ohno", "tran", "` hy");
            //Log.d("MainActivity", "tạm signup thử trong main");

            //login("cattuongtrantran@gmail.com", "12345678");
            //Log.d("MainActivity", "tạm login thử trong main");

            return;
        }

        String uid = currentUser.getUid();

        // khởi tạo user cho toàn bộ src
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        userViewModel.loadUser(uid);

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


    // tạm bợ hàm signup
    private void signup(String email, String password, String firstname, String lastname, String username) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Log.d("MainActivity", "signup thanh cong: " + user.getUid());

                        User newUser = new User(user.getUid(), user.getEmail(), firstname, lastname, username, "", false);
                        new UserRepository().saveUser(newUser);
                    } else {
                        Log.e("MainActivity", "signup that bai", task.getException());
                    }
                });
    }

    // tạm bợ hàm login
    private void login(String email, String password) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        Log.d("MainActivity", "login thanh cong: " + user.getUid());

                        if (userViewModel == null) {
                            userViewModel = ((MyApplication) getApplication()).getUserViewModel();
                        }
                    } else {
                        Log.e("MainActivity", "login that bai: " + task.getException());
                    }
                });
    }
}
