package com.example.locket.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import com.example.locket.ui.photo.PhotoActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button btnRegister = findViewById(R.id.btn_register);
        Button btnLogin = findViewById(R.id.btn_login);


        View.OnClickListener goToPhoto = v -> {
            startActivity(new Intent(WelcomeActivity.this, PhotoActivity.class));
            finish();
        };

        btnRegister.setOnClickListener(goToPhoto);
        btnLogin.setOnClickListener(goToPhoto);
    }
}
