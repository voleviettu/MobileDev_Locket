package com.example.locket.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.locket.auth.WelcomeActivity;
import com.example.locket.ui.photo.PhotoActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // !!! XÓA DÒNG NÀY KHI CÓ NÚT LOGOUT THẬT !!!
        // FirebaseAuth.getInstance().signOut();
        // Log.d(TAG, "!!! ĐÃ GỌI signOut() TẠM THỜI TRONG MAINACTIVITY !!!");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "User chưa đăng nhập. Chuyển đến WelcomeActivity.");
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            Log.d(TAG, "User đã đăng nhập (UID: " + currentUser.getUid() + "). Chuyển đến PhotoActivity.");
            Intent intent = new Intent(this, PhotoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}