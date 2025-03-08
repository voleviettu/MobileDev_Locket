package com.example.locket.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Import ContextCompat

import com.example.locket.R;

public class SignInEmail extends AppCompatActivity {

    private EditText emailEditText;
    private Button continueButton;
    private ImageButton backButton;
    private TextView termTextView;

    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_email); // The XML layout file

        // Find the views by their IDs
        emailEditText = findViewById(R.id.input_email);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        termTextView = findViewById(R.id.text_terms);

        // Get ColorStateLists for yellow and gray (best practice)
        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));


        // Set click listener for the Continue button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndContinue();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInEmail.this, WelcomeActivity.class));
                finish();
            }
        });

        termTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: show term and condition.
                Toast.makeText(SignInEmail.this, "Show Term and Condition", Toast.LENGTH_SHORT).show();
            }
        });

        // Add TextWatcher to the email EditText
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateEmailAndUpdateButton(s.toString());
            }
        });

    }
    private void validateEmailAndUpdateButton(String email) {
        String trimmedEmail = email.trim();
        boolean isValid = !trimmedEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches();

        // Set button enabled/disabled and change color
        continueButton.setEnabled(isValid);
        if (isValid) {
            continueButton.setBackgroundTintList(yellowColorStateList);
        } else {
            continueButton.setBackgroundTintList(grayColorStateList);
        }
    }

    private void validateAndContinue() {
        String email = emailEditText.getText().toString().trim();

        // No need to re-validate here
        Intent intent = new Intent(SignInEmail.this, SignInPassword.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }
}