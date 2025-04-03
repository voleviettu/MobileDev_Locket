package com.example.locket.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns; // Import để kiểm tra định dạng email
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.example.locket.ui.photo.PhotoActivity; // Màn hình sau khi đăng nhập thành công
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInPassword extends AppCompatActivity {

    private static final String TAG = "SignInPassword";

    private EditText passwordEditText;
    private ImageView passwordToggleImage;
    private Button continueButton;
    private Button forgotPassword;
    private ImageButton backButton;

    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private String email; // Email nhận từ SignInEmail Activity
    private String originalForgotPasswordText = ""; // Biến lưu text gốc của nút Quên mật khẩu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_password);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View
        passwordEditText = findViewById(R.id.input_password);
        passwordToggleImage = findViewById(R.id.password_toggle_image);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        forgotPassword = findViewById(R.id.forgot_password);

        // Lấy text gốc của nút Quên mật khẩu (giả sử nó được đặt trong layout)
        originalForgotPasswordText = forgotPassword.getText().toString();
        // Hoặc bạn có thể đặt cứng ở đây nếu muốn:
        // originalForgotPasswordText = "Quên mật khẩu?";

        // Lấy email từ Intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Log.e(TAG, "Lỗi: Email không được truyền sang SignInPassword!");
            Toast.makeText(this, "Lỗi: Không nhận được email.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có email
            return;
        } else {
            Log.d(TAG, "Email nhận được để đăng nhập/reset: " + email);
        }

        // Thiết lập ẩn/hiện mật khẩu ban đầu
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Xử lý sự kiện nhấn nút ẩn/hiện mật khẩu
        passwordToggleImage.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        // --- XỬ LÝ SỰ KIỆN NHẤN NÚT QUÊN MẬT KHẨU ---
        forgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Xử lý sự kiện thay đổi text trong EditText mật khẩu
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordAndUpdateButton(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Kiểm tra và cập nhật trạng thái nút "Tiếp tục" ban đầu
        validatePasswordAndUpdateButton(passwordEditText.getText().toString());

        // Xử lý sự kiện nhấn nút "Tiếp tục" (Đăng nhập)
        continueButton.setOnClickListener(v -> {
            String password = passwordEditText.getText().toString().trim();
            // Kiểm tra lại độ dài trước khi đăng nhập
            if (password.length() >= 8) {
                // Thay đổi trạng thái nút để báo đang xử lý
                continueButton.setEnabled(false);
                continueButton.setText("Đang đăng nhập..."); // *** Text cứng ***
                signIn(email, password);
            } else {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show(); // *** Text cứng ***
            }
        });

        // Xử lý sự kiện nhấn nút "Back"
        backButton.setOnClickListener(v -> {
            // Chỉ cần đóng Activity hiện tại để quay lại Activity trước đó trong stack (SignInEmail)
            finish();
        });
    }

    /**
     * Xử lý logic khi người dùng nhấn nút "Quên mật khẩu".
     */
    private void handleForgotPassword() {
        // Kiểm tra lại email một lần nữa
        if (email == null || email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignInPassword.this, "Địa chỉ email không hợp lệ để đặt lại mật khẩu.", Toast.LENGTH_SHORT).show(); // *** Text cứng ***
            Log.w(TAG, "Yêu cầu đặt lại mật khẩu với email không hợp lệ: " + email);
            return; // Dừng nếu email không hợp lệ
        }

        // Thay đổi trạng thái nút để báo đang xử lý
        forgotPassword.setEnabled(false);
        forgotPassword.setText("Đang gửi..."); // *** Text cứng ***

        Log.d(TAG, "Bắt đầu gửi email đặt lại mật khẩu tới: " + email);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    // Khôi phục trạng thái nút sau khi tác vụ hoàn thành
                    forgotPassword.setEnabled(true);
                    forgotPassword.setText(originalForgotPasswordText); // Khôi phục text gốc đã lưu

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Gửi email đặt lại mật khẩu thành công tới: " + email);
                        // *** Text cứng với nối chuỗi ***
                        Toast.makeText(SignInPassword.this, "Đã gửi email đặt lại mật khẩu tới " + email + ". Vui lòng kiểm tra hộp thư (kể cả Spam).", Toast.LENGTH_LONG).show();
                        // finish(); // Bỏ comment nếu muốn tự động quay lại màn hình trước
                    } else {
                        Log.w(TAG, "Gửi email đặt lại mật khẩu thất bại tới: " + email, task.getException());
                        // *** Text cứng ***
                        String errorMessage = "Không thể gửi email đặt lại mật khẩu.";
                        if (task.getException() != null) {
                            errorMessage += " Lỗi: " + task.getException().getMessage(); // Nối thêm thông báo lỗi từ Firebase
                        }
                        Toast.makeText(SignInPassword.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Kiểm tra độ dài mật khẩu và cập nhật trạng thái nút "Tiếp tục".
     * @param password Mật khẩu hiện tại.
     */
    private void validatePasswordAndUpdateButton(String password) {
        boolean isValid = password != null && password.length() >= 8;
        continueButton.setEnabled(isValid);
        int buttonColorRes = isValid ? R.color.button : R.color.gray_light;
        continueButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, buttonColorRes)));
        // Cập nhật text nút về mặc định nếu đang không đăng nhập (đảm bảo text đúng sau khi nhập đủ ký tự)
        if (continueButton.getText().toString().equals("Đang đăng nhập...")) {
            // Chỉ đổi lại text nếu nó đang là "Đang đăng nhập..." và mật khẩu lại hợp lệ/không hợp lệ
            // Nếu không đăng nhập thì text nút nên là "Tiếp tục"
            if (!continueButton.isEnabled() || isValid) { // Cập nhật lại khi thay đổi trạng thái enable
                continueButton.setText("Tiếp tục"); // *** Text cứng ***
            }
        } else if (continueButton.isEnabled()){
            // Đảm bảo text là "Tiếp tục" khi nút được enable và không trong trạng thái "Đang đăng nhập"
            continueButton.setText("Tiếp tục"); // *** Text cứng ***
        }


    }

    /**
     * Cập nhật trạng thái hiển thị mật khẩu và icon.
     */
    private void updatePasswordVisibility() {
        int selection = passwordEditText.getSelectionEnd();
        if (isPasswordVisible) {
            passwordEditText.setTransformationMethod(null);
            passwordToggleImage.setImageResource(R.drawable.ic_hide);
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggleImage.setImageResource(R.drawable.ic_view);
        }
        passwordEditText.setSelection(selection);
    }

    /**
     * Thực hiện đăng nhập người dùng.
     * @param email Email.
     * @param password Mật khẩu.
     */
    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Luôn khôi phục trạng thái nút "Tiếp tục"
                    continueButton.setEnabled(true);
                    continueButton.setText("Tiếp tục"); // *** Text cứng ***
                    validatePasswordAndUpdateButton(passwordEditText.getText().toString()); // Cập nhật lại trạng thái enable/disable

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "signInWithEmail:success - UID: " + user.getUid());
                            Toast.makeText(SignInPassword.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show(); // *** Text cứng ***

                            Intent intent = new Intent(SignInPassword.this, PhotoActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            Log.w(TAG, "signInWithEmail:success, but currentUser is null");
                            Toast.makeText(SignInPassword.this, "Đăng nhập thành công nhưng không lấy được thông tin người dùng.", Toast.LENGTH_SHORT).show(); // *** Text cứng ***
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        // *** Text cứng ***
                        String errorMessage = "Đăng nhập thất bại.";
                        if (task.getException() != null) {
                            errorMessage += " Lỗi: " + task.getException().getMessage();
                        }
                        Toast.makeText(SignInPassword.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}