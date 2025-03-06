package com.example.locket.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.method.PasswordTransformationMethod;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;

public class SignInPassword extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageView passwordToggleImage;
    private Button continueButton;

    private Button forgotPassword;
    private ImageButton backButton;

    private boolean isPasswordVisible = false; // Keep track of visibility

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_password);

        passwordEditText = findViewById(R.id.input_password);
        passwordToggleImage = findViewById(R.id.password_toggle_image);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        forgotPassword = findViewById(R.id.forgot_password);

        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Password visibility toggle
        passwordToggleImage.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        // Password validation (TextWatcher)
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.length() >= 8) {
                    continueButton.setEnabled(true);
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.button)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black));
                } else {
                    continueButton.setEnabled(false);
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.gray_light)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Continue button click
        continueButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString();
            String email = getIntent().getStringExtra("email"); // Get email from previous activity

            // Chưa đổi next activity!!!!!!!!!!!!!!!!!!!
            Intent intent = new Intent(SignInPassword.this, SignUpFullName.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
        });
        backButton.setOnClickListener(v -> finish());
    }

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            // Show password
            passwordEditText.setTransformationMethod(null); // Remove the PasswordTransformationMethod
            passwordToggleImage.setImageResource(R.drawable.ic_hide); // Use your "view" image
        } else {
            // Hide password
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Apply PasswordTransformationMethod
            passwordToggleImage.setImageResource(R.drawable.ic_view); // Use your "hide" image
        }

        // Move the cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.getText().length());
    }
}