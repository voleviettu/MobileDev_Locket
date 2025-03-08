package com.example.locket.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.ui.photo.PhotoActivity;

public class SignUpUsername extends AppCompatActivity {

    private EditText usernameEditText;
    private Button continueButton;
    private ImageButton backButton;

    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;

    private String email; // To store the email passed from the previous activity

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_username); // You'll need to create this layout file (signup_username.xml)

        // Find the views by their IDs
        usernameEditText = findViewById(R.id.input_username);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);

        // Get ColorStateLists (same as SignUpEmail)
        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));

        // Retrieve the email from the intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("email")) {
            email = intent.getStringExtra("email");
        }
        if (intent != null && intent.hasExtra("password")) {
            String password = intent.getStringExtra("password");
        }


        // Set click listener for the Continue button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndContinue();
            }
        });

        // Set click listener for the Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to SignUpPassword
                Intent backIntent = new Intent(SignUpUsername.this, SignUpPassword.class);
                backIntent.putExtra("email", email);
                startActivity(backIntent);
                finish();
            }
        });


        // Add TextWatcher to the username EditText
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateUsernameAndUpdateButton(s.toString());
            }
        });

        // Initially disable the button
        continueButton.setEnabled(false);
        continueButton.setBackgroundTintList(grayColorStateList);
    }

    private void validateUsernameAndUpdateButton(String username) {
        String trimmedUsername = username.trim();
        // Implement your username validation logic here.  This is CRUCIAL.
        // Example (basic): At least 3 characters, no spaces, only alphanumeric
        boolean isValid = trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9]*$"); //  Regex for alphanumeric

        // Add more specific checks (e.g., check for profanity, check for uniqueness against a database, etc.)

        continueButton.setEnabled(isValid);
        if (isValid) {
            continueButton.setBackgroundTintList(yellowColorStateList);
        } else {
            continueButton.setBackgroundTintList(grayColorStateList);
        }
    }


    private void validateAndContinue() {
        String username = usernameEditText.getText().toString().trim();

        //  You might want to re-validate here, even though you have the TextWatcher,
        //  to handle edge cases where the user might bypass the TextWatcher (e.g., pasting text).
        if (validateUsername(username)) {
            Intent intent = new Intent(SignUpUsername.this, PhotoActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("username", username); // Pass the username
            startActivity(intent);
        } else {
            Toast.makeText(this, "Invalid username", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateUsername(String username) {
        String trimmedUsername = username.trim();
        // Implement your username validation logic here.  This is CRUCIAL.
        // Example (basic): At least 3 characters, no spaces, only alphanumeric
        boolean isValid = trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9]*$"); //  Regex for alphanumeric

        return isValid;
    }
}
