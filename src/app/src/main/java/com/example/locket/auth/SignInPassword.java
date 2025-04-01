package com.example.locket.auth;

import android.app.ProgressDialog; // Thêm import ProgressDialog
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;              // Thêm import Log
import android.util.Patterns;        // Thêm import Patterns
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;        // Thêm import Toast

import androidx.annotation.NonNull; // Thêm import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.ui.main.MainActivity;
import com.example.locket.R;
import com.google.android.gms.tasks.OnCompleteListener; // Thêm import OnCompleteListener
import com.google.android.gms.tasks.Task;             // Thêm import Task
import com.google.firebase.auth.AuthResult;          // Thêm import AuthResult
import com.google.firebase.auth.FirebaseAuth;          // Thêm import FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException; // Thêm để bắt lỗi sai mật khẩu
import com.google.firebase.auth.FirebaseAuthInvalidUserException;      // Thêm để bắt lỗi user không tồn tại

public class SignInPassword extends AppCompatActivity {

    private EditText passwordEditText;
    private ImageView passwordToggleImage;
    private Button continueButton;
    private Button forgotPassword;
    private ImageButton backButton;

    private boolean isPasswordVisible = false;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog; // Để hiển thị loading

    private String email; // Lưu email từ Intent

    private static final String TAG = "SignInPassword"; // Tag để log lỗi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false); // Không cho hủy khi đang xử lý

        passwordEditText = findViewById(R.id.input_password);
        passwordToggleImage = findViewById(R.id.password_toggle_image);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        forgotPassword = findViewById(R.id.forgot_password);

        // Lấy email từ Intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            // Xử lý lỗi nếu không có email (nên quay lại màn hình trước)
            Log.e(TAG, "Email is missing from Intent");
            Toast.makeText(this, "Lỗi: Không nhận được email.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity này
            return; // Dừng thực thi onCreate
        }

        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Password visibility toggle
        passwordToggleImage.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        // Forgot Password button click
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi hàm gửi email reset password
                sendPasswordReset();
            }
        });

        // Password validation (TextWatcher) - Giữ nguyên logic cập nhật UI nút Continue
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                // Chỉ cần kiểm tra độ dài để enable/disable nút, việc check đúng/sai sẽ do Firebase lo
                boolean isValidLength = password.length() >= 8; // Hoặc độ dài tối thiểu bạn yêu cầu
                continueButton.setEnabled(isValidLength);
                if (isValidLength) {
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.button)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black));
                } else {
                    continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(SignInPassword.this, R.color.gray_light)));
                    continueButton.setTextColor(ContextCompat.getColor(SignInPassword.this, R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Continue button click - **SỬA ĐỔI QUAN TRỌNG**
        continueButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            // Gọi hàm đăng nhập bằng Firebase
            signInUser(email, password);
        });

        backButton.setOnClickListener(v -> finish()); // Chỉ cần đóng activity hiện tại
    }

    // --- HÀM ĐĂNG NHẬP VỚI FIREBASE ---
    private void signInUser(String email, String password) {
        if (password.isEmpty()) {
            passwordEditText.setError("Vui lòng nhập mật khẩu");
            passwordEditText.requestFocus();
            return;
        }
        if (password.length() < 8) { // Kiểm tra lại độ dài tối thiểu nếu cần
            passwordEditText.setError("Mật khẩu phải có ít nhất 8 ký tự");
            passwordEditText.requestFocus();
            return;
        }


        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss(); // Luôn ẩn dialog khi hoàn thành
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(SignInPassword.this, "Đăng nhập thành công.", Toast.LENGTH_SHORT).show();

                            // Chuyển đến màn hình chính và xóa các màn hình đăng nhập/đăng ký khỏi stack
                            Intent intent = new Intent(SignInPassword.this, MainActivity.class); // <<=== THAY MainActivity bằng Activity chính của bạn
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng SignInPassword activity

                        } else {
                            // Đăng nhập thất bại
                            Log.w(TAG, "signInWithEmail:failure", task.getException());

                            // Hiển thị lỗi cụ thể hơn
                            String errorMessage = "Đăng nhập thất bại.";
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                errorMessage = "Tài khoản không tồn tại.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "Sai mật khẩu.";
                            } catch (Exception e) {
                                Log.e(TAG, "signInWithEmail:failure", e);
                                errorMessage = "Lỗi: " + e.getMessage();
                            }
                            Toast.makeText(SignInPassword.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // --- HÀM GỬI EMAIL RESET PASSWORD ---
    private void sendPasswordReset() {

        progressDialog.setMessage("Đang gửi yêu cầu...");
        progressDialog.show();
        forgotPassword.setEnabled(false); // Vô hiệu hóa nút tạm thời khi đang gửi

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password reset email sent successfully.");
                            Toast.makeText(SignInPassword.this, "Đã gửi email đặt lại mật khẩu đến " + email, Toast.LENGTH_LONG).show();
                            // Có thể giữ nút bị vô hiệu hóa hoặc thay đổi text như trước
                            forgotPassword.setText("Email đã được gửi đi!");
                            // forgotPassword.setEnabled(false); // Giữ vô hiệu hóa
                        } else {
                            Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                            Toast.makeText(SignInPassword.this, "Gửi email thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                            forgotPassword.setEnabled(true); // Kích hoạt lại nút nếu gửi lỗi
                            forgotPassword.setText(R.string.forgot_password); // Đặt lại text gốc (nếu bạn dùng string resource) hoặc text cứng
                        }
                    }
                });
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
}