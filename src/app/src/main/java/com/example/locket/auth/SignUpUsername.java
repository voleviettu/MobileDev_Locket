package com.example.locket.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.example.locket.data.UserRepository;
import com.example.locket.model.User; // Đảm bảo đã import User model
import com.example.locket.ui.main.MainActivity; // Màn hình chính sau khi đăng ký
// import com.example.locket.ui.photo.PhotoActivity; // Hoặc PhotoActivity nếu đó là màn hình chính
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpUsername extends AppCompatActivity {

    private static final String TAG = "SignUpUsername";

    private EditText usernameEditText;
    private Button continueButton;
    private ImageButton backButton;

    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;

    // Biến lưu trữ dữ liệu nhận từ các Activity trước
    private String email;
    private String password;
    private String firstname;
    private String lastname;

    private FirebaseAuth mAuth;
    private UserRepository userRepository;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_username);

        // Khởi tạo Firebase Auth và UserRepository
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // Ánh xạ Views
        usernameEditText = findViewById(R.id.input_username);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);

        // Lấy màu
        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));

        // --- Nhận dữ liệu từ Intent ---
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            firstname = intent.getStringExtra("firstName");
            lastname = intent.getStringExtra("lastName");
        }

        if (email == null || email.isEmpty() ||
                password == null || password.isEmpty() ||
                firstname == null || firstname.isEmpty() ||
                lastname == null || lastname.isEmpty()) {

            Log.e(TAG, "Dữ liệu đăng ký (email, password, firstname, lastname) không đầy đủ.");
            Toast.makeText(this, "Lỗi: Thông tin đăng ký không đầy đủ. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            Intent errorIntent = new Intent(this, SignUpFullName.class); // Quay về màn hình nhập tên
            errorIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Xóa stack phía trên
            startActivity(errorIntent);
            finish();
            return;
        }

        Log.d(TAG, "Dữ liệu nhận được: Email=" + email + ", Firstname=" + firstname + ", Lastname=" + lastname);


        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndContinue();
            }
        });

        backButton.setOnClickListener(v -> finish());

        // --- TextWatcher cho username ---
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                validateUsernameAndUpdateButton(s.toString());
            }
        });
        validateUsernameAndUpdateButton(usernameEditText.getText().toString()); // Kiểm tra trạng thái ban đầu
    }

    private void validateUsernameAndUpdateButton(String username) {
        boolean isValid = isUsernameValid(username);
        continueButton.setEnabled(isValid);
        continueButton.setBackgroundTintList(isValid ? yellowColorStateList : grayColorStateList);
        continueButton.setTextColor(ContextCompat.getColor(this, isValid ? R.color.black : R.color.black));
    }

    private void validateAndContinue() {
        String username = usernameEditText.getText().toString().trim();

        if (isUsernameValid(username)) {
            signup(email, password, firstname, lastname, username);

            continueButton.setEnabled(false);
            continueButton.setText("Đang xử lý...");

        } else {
            Toast.makeText(this, "Username không hợp lệ. Vui lòng kiểm tra lại.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isUsernameValid(String username) {
        String trimmedUsername = username.trim();
        // Quy tắc: >= 3 ký tự, không có khoảng trắng, chỉ chữ cái/số
        return trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9_]*$"); // Cho phép cả dấu gạch dưới
    }

    private void signup(String email, String password, String firstname, String lastname, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    validateUsernameAndUpdateButton(usernameEditText.getText().toString());
                    continueButton.setText("Đăng ký");

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Đăng ký tài khoản Firebase Auth thành công: " + firebaseUser.getUid());

                            User newUser = new User(
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail(), // Lấy email từ firebaseUser cho chắc chắn
                                    firstname,
                                    lastname,
                                    username,
                                    null, // Avatar ban đầu là null
                                    false // isPremium ban đầu là false
                            );

                            userRepository.saveUser(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Lưu user vào Firestore thành công");
                                        Toast.makeText(SignUpUsername.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                                        // Chuyển sang màn hình chính (MainActivity hoặc PhotoActivity)
                                        Intent mainIntent = new Intent(SignUpUsername.this, MainActivity.class); // Đổi thành PhotoActivity nếu cần
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish(); // Đóng tất cả các Activity đăng ký trước đó

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi lưu user vào Firestore", e);
                                        Toast.makeText(SignUpUsername.this, "Đăng ký thành công nhưng lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        } else {
                            Log.e(TAG, "firebaseUser null sau khi signup thành công");
                            Toast.makeText(SignUpUsername.this, "Đăng ký thành công nhưng có lỗi lấy thông tin người dùng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Đăng ký tài khoản thất bại", task.getException());
                        Toast.makeText(SignUpUsername.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}