package com.example.locket.auth;
import com.example.locket.ui.main.MainActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Import Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
// import android.widget.TextView; // TextView không được dùng, có thể xóa
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.example.locket.data.UserRepository; // Import UserRepository
import com.example.locket.model.User; // Import User model
import com.example.locket.ui.photo.PhotoActivity;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser

public class SignUpUsername extends AppCompatActivity {

    private static final String TAG = "SignUpUsername"; // Thêm TAG cho logging

    private EditText usernameEditText;
    private Button continueButton;
    private ImageButton backButton;

    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;

    private String email;
    private String password; // Biến để lưu password

    private FirebaseAuth mAuth; // Firebase Auth instance
    private UserRepository userRepository; // UserRepository instance

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_username);

        // Khởi tạo Firebase Auth và UserRepository
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();

        // Find the views by their IDs
        usernameEditText = findViewById(R.id.input_username);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);

        // Get ColorStateLists
        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));

        // Retrieve the email and password from the intent
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("email")) {
                email = intent.getStringExtra("email");
            }
            if (intent.hasExtra("password")) {
                password = intent.getStringExtra("password"); // Lưu password vào biến thành viên
            }
        }

        // Kiểm tra nếu email hoặc password bị null (đề phòng)
        if (email == null || password == null) {
            Log.e(TAG, "Email hoặc Password bị null khi khởi tạo SignUpUsername");
            Toast.makeText(this, "Lỗi: Không nhận được đủ thông tin đăng ký.", Toast.LENGTH_LONG).show();
            // Có thể quay lại màn hình trước hoặc hiển thị lỗi nghiêm trọng
            finish(); // Đóng activity này nếu thiếu thông tin
            return;
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
                // Gửi lại email để màn hình trước có thể điền sẵn nếu cần
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
        boolean isValid = isUsernameValid(trimmedUsername); // Gọi hàm kiểm tra chung

        continueButton.setEnabled(isValid);
        if (isValid) {
            continueButton.setBackgroundTintList(yellowColorStateList);
        } else {
            continueButton.setBackgroundTintList(grayColorStateList);
        }
    }


    private void validateAndContinue() {
        String username = usernameEditText.getText().toString().trim();

        if (isUsernameValid(username)) {
            // *** GỌI HÀM SIGNUP Ở ĐÂY ***
            // Tạm thời dùng "" cho firstname và lastname
            signup(email, password, "", "", username);
            // Hiển thị loading indicator (nếu có)
            continueButton.setEnabled(false); // Vô hiệu hóa nút trong khi chờ
            continueButton.setText("Đang xử lý..."); // Thay đổi text nút (tùy chọn)

        } else {
            // Mặc dù nút bị vô hiệu hóa nếu username không hợp lệ,
            // kiểm tra lại ở đây để đảm bảo an toàn.
            Toast.makeText(this, "Username không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm kiểm tra username (sử dụng chung)
    private boolean isUsernameValid(String username) {
        String trimmedUsername = username.trim();
        // Logic kiểm tra username của bạn
        // Ví dụ: Ít nhất 3 ký tự, không chứa khoảng trắng, chỉ chứa chữ cái và số
        return trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9]*$");
    }

    // *** HÀM SIGNUP ĐÃ CHUYỂN TỪ MAINACTIVITY ***
    private void signup(String email, String password, String firstname, String lastname, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> { // Sử dụng 'this' hoặc SignUpUsername.this
                    // Ẩn loading indicator (nếu có)
                    continueButton.setEnabled(true); // Kích hoạt lại nút
                    continueButton.setText("Tiếp tục"); // Đặt lại text nút

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(TAG, "Đăng ký tài khoản thành công: " + firebaseUser.getUid());

                            // Tạo đối tượng User mới
                            User newUser = new User(
                                    firebaseUser.getUid(),
                                    firebaseUser.getEmail(),
                                    firstname, // Hiện tại là ""
                                    lastname,  // Hiện tại là ""
                                    username,
                                    "", // phoneNumber - tạm để trống
                                    false // isPrivate - mặc định
                            );

                            // Lưu user vào Firestore
                            userRepository.saveUser(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Lưu user vào Firestore thành công");

                                        // *** Chuyển sang màn hình tiếp theo ***
                                        Intent intent = new Intent(SignUpUsername.this, MainActivity.class);
                                        // Xóa các activity trước đó khỏi back stack (tùy chọn)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish(); // Đóng SignUpUsername activity

                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Lỗi khi lưu user vào Firestore", e);
                                        // Có thể hiển thị lỗi cho người dùng hoặc xử lý khác
                                        Toast.makeText(SignUpUsername.this, "Lỗi khi lưu thông tin người dùng.", Toast.LENGTH_SHORT).show();
                                        // Cân nhắc: Có nên xóa tài khoản Firebase Auth vừa tạo không?
                                        // firebaseUser.delete(); // Nếu muốn rollback
                                    });
                        } else {
                            Log.e(TAG, "firebaseUser null sau khi signup thành công (?)");
                            Toast.makeText(SignUpUsername.this, "Đăng ký thành công nhưng không lấy được thông tin user.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Đăng ký tài khoản thất bại", task.getException());
                        // Hiển thị lỗi cụ thể hơn cho người dùng
                        Toast.makeText(SignUpUsername.this, "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}