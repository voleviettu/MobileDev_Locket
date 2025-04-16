package com.example.locket.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.locket.MyApplication;
import com.example.locket.auth.WelcomeActivity;
import com.example.locket.data.UserRepository;
import com.example.locket.model.User;
import com.example.locket.ui.photo.PhotoActivity;
import com.example.locket.utils.NotificationHelper;
import com.example.locket.utils.ReactionListener;

import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private UserViewModel userViewModel;
    private UserRepository userRepository;
    private ReactionListener reactionListener;
    private NotificationHelper notificationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //new FriendViewModel().acceptFriendRequest("UZVHQWcfC2N1edVJyAUn3vmVQwQ2", "N1vOkqQ7BQSHHHclZBJ8j28ED6P2");
        //new FriendViewModel().acceptFriendRequest("Rcpcy5LbCVWNYRfnIHTtb1wu8jI3", "N1vOkqQ7BQSHHHclZBJ8j28ED6P2");
        //new FriendViewModel().acceptFriendRequest("6AFqeqFFK9QoWkHSW4iQ7XXKlgK2", "N1vOkqQ7BQSHHHclZBJ8j28ED6P2");

        // !!! XÓA DÒNG NÀY KHI CÓ NÚT LOGOUT THẬT !!!
        // FirebaseAuth.getInstance().signOut();
         Log.d(TAG, "!!! ĐÃ GỌI signOut() TẠM THỜI TRONG MAINACTIVITY !!!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Quyền thông báo: " + (hasPermission ? "Đã cấp" : "Chưa cấp"));
            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

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

        // Khởi tạo ReactionListener và NotificationHelper
        reactionListener = new ReactionListener();
        notificationHelper = new NotificationHelper(this);

        // Lắng nghe reaction mới
        reactionListener.listenForReactions(uid, reaction -> {
            notificationHelper.showReactionNotification(reaction);
        });


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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Ngừng lắng nghe reaction
        if (reactionListener != null) {
            reactionListener.stopListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "Kết quả quyền thông báo: " + (granted ? "Đã cấp" : "Bị từ chối"));
        }
    }
}