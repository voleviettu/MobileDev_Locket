package com.example.locket.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.text.TextWatcher;
import android.text.Editable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.locket.R;

public class SignUpFullName extends AppCompatActivity {

    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private Button continueButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_fullname);

        firstNameEditText = findViewById(R.id.input_first_name);
        lastNameEditText = findViewById(R.id.input_last_name);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);

        updateContinueButtonState();

        backButton.setOnClickListener(v -> finish());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateContinueButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        firstNameEditText.addTextChangedListener(textWatcher);
        lastNameEditText.addTextChangedListener(textWatcher);

        continueButton.setOnClickListener(v -> {
            String firstName = firstNameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = getIntent().getStringExtra("email");
            String password = getIntent().getStringExtra("password");

            Intent intent = new Intent(SignUpFullName.this, SignUpUsername.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("firstName", firstName);
            intent.putExtra("lastName", lastName);
            startActivity(intent);
        });
    }

    private boolean isValidName(String name) {
        return name.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]{2,50}$");
    }

    private void updateContinueButtonState() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        boolean isValid = isValidName(firstName) && isValidName(lastName);

        continueButton.setEnabled(isValid);
        int color = isValid ? R.color.button : R.color.gray_light;
        continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
    }
}
