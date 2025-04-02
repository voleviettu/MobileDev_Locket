package com.example.locket.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
// import android.text.InputType; // Không thấy dùng trực tiếp, có thể xóa nếu không cần
import android.text.TextWatcher;
import android.util.Log; // *** Thêm import Log ***
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.method.PasswordTransformationMethod;
// import android.widget.TextView; // Không thấy dùng trực tiếp, có thể xóa nếu không cần
import android.view.View;
import android.widget.Toast; // *** Thêm import Toast ***

import androidx.annotation.NonNull; // *** Thêm import NonNull ***
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.example.locket.ui.main.MainActivity; // *** Có thể cần để chuyển về hoặc dùng logic tương tự ***
import com.example.locket.ui.photo.PhotoActivity; // *** Import màn hình đích sau khi login thành công ***
import com.google.android.gms.tasks.OnCompleteListener; // *** Thêm import OnCompleteListener ***
import com.google.android.gms.tasks.Task; // *** Thêm import Task ***
import com.google.firebase.auth.AuthResult; // *** Thêm import AuthResult ***
import com.google.firebase.auth.FirebaseAuth; // *** Thêm import FirebaseAuth ***
import com.google.firebase.auth.FirebaseUser; // *** Thêm import FirebaseUser ***


public class SignInPassword extends AppCompatActivity {

    private static final String TAG = "SignInPassword";

    private EditText passwordEditText;
    private ImageView passwordToggleImage;
    private Button continueButton;
    private Button forgotPassword;
    private ImageButton backButton;

    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth; // *** Firebase Auth instance ***
    private String email; // *** Biến lưu email từ Intent ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_password);

        // *** Khởi tạo Firebase Auth ***
        mAuth = FirebaseAuth.getInstance();

        passwordEditText = findViewById(R.id.input_password);
        passwordToggleImage = findViewById(R.id.password_toggle_image);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        forgotPassword = findViewById(R.id.forgot_password);

        // *** Lấy email từ Intent ***
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Email không được truyền sang SignInPassword!");
            Toast.makeText(this, "Lỗi: Không nhận được email.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có email
            return;
        }

        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        passwordToggleImage.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        forgotPassword.setOnClickListener(v -> {
            // TODO: Triển khai chức năng quên mật khẩu thực sự (gửi email reset)
            forgotPassword.setText("Chức năng đang phát triển");
            // forgotPassword.setEnabled(false);
            Toast.makeText(SignInPassword.this, "Chức năng quên mật khẩu chưa được cài đặt.", Toast.LENGTH_SHORT).show();
        });

        // Password validation (TextWatcher) - Chỉ kiểm tra độ dài cơ bản
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                boolean isValid = password.length() >= 8; // Điều kiện cơ bản
                continueButton.setEnabled(isValid);
                if (isValid) {
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.button)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black)); // Hoặc màu chữ mong muốn
                } else {
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.gray_light)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black)); // Hoặc màu chữ mong muốn
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Initially disable button if password is empty
        validatePasswordAndUpdateButton(passwordEditText.getText().toString());

        // Continue button click
        continueButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            // Kiểm tra lại độ dài trước khi gọi signIn (dù đã có TextWatcher)
            if (password.length() >= 8) {
                signIn(email, password); // *** Gọi hàm signIn ***
                // Hiển thị trạng thái đang xử lý
                continueButton.setEnabled(false);
                continueButton.setText("Đang đăng nhập...");
            } else {
                // Hiển thị thông báo nếu bằng cách nào đó nút được nhấn khi mật khẩu < 8 ký tự
                Toast.makeText(this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> {
            // Quay lại SignInEmail
            Intent backIntent = new Intent(SignInPassword.this, SignInEmail.class);
            // Không cần finish() ở đây nếu chỉ quay lại màn hình trước trong backstack
            // finish();
            startActivity(backIntent); // Hoặc chỉ cần gọi finish() nếu SignInEmail vẫn còn trong stack
            finish(); // Đóng màn hình hiện tại
        });
    }

    // Hàm kiểm tra và cập nhật trạng thái nút ban đầu
    private void validatePasswordAndUpdateButton(String password) {
        boolean isValid = password.length() >= 8;
        continueButton.setEnabled(isValid);
        if (isValid) {
            continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button)));
            continueButton.setTextColor(ContextCompat.getColor(this, R.color.black));
        } else {
            continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light)));
            continueButton.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }


    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(null);
            passwordToggleImage.setImageResource(R.drawable.ic_hide);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggleImage.setImageResource(R.drawable.ic_view);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> { // Sử dụng 'this' hoặc SignInPassword.this
                    // Hoàn tác trạng thái đang xử lý của nút
                    continueButton.setEnabled(true);
                    continueButton.setText("Tiếp tục");
                    // Kích hoạt lại nút dựa trên trạng thái password hiện tại
                    validatePasswordAndUpdateButton(passwordEditText.getText().toString());


                    if (task.isSuccessful()) {
                        // Đăng nhập thành công
                        FirebaseUser user = mAuth.getCurrentUser(); // Lấy user vừa đăng nhập
                        if (user != null) {
                            Log.d(TAG, "signInWithEmail:success - UID: " + user.getUid());
                            Toast.makeText(SignInPassword.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            // *** Chuyển đến màn hình chính (ví dụ: PhotoActivity) ***
                            Intent intent = new Intent(SignInPassword.this, PhotoActivity.class);
                            // Xóa các activity đăng nhập/đăng ký khỏi back stack
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng SignInPassword và các màn hình trước đó (do cờ CLEAR_TASK)

                        } else {
                            // Trường hợp hiếm gặp: thành công nhưng user null?
                            Log.w(TAG, "signInWithEmail:success, but currentUser is null");
                            Toast.makeText(SignInPassword.this, "Đăng nhập thành công nhưng không lấy được thông tin người dùng.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Đăng nhập thất bại
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignInPassword.this, "Đăng nhập thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show(); // Hiển thị lỗi chi tiết
                    }
                });
    }
}