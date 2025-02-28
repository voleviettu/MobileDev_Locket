package com.example.locket.ui.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

import com.example.locket.auth.WelcomeActivity;
import com.example.locket.ui.photo.PhotoActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        boolean currentUser = false;
        if (!currentUser) {
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            startActivity(new Intent(this, PhotoActivity.class));
        }
        finish();
    }
}
