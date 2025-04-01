package com.example.locket.auth;

import android.app.ProgressDialog; // Thêm import
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Thêm import
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Thêm import
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.locket.R;
import com.google.android.gms.tasks.OnCompleteListener; // Thêm import
import com.google.android.gms.tasks.Task; // Thêm import
import com.google.firebase.auth.FirebaseAuth; // Thêm import
import com.google.firebase.auth.SignInMethodQueryResult; // Thêm import

public class SignInEmail extends AppCompatActivity {

    private EditText emailEditText;
    private Button continueButton;
    private ImageButton backButton;
    private TextView termTextView;

    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog; // Để hiển thị loading

    private static final String TAG = "SignInEmail"; // Tag để log lỗi


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_email);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra email...");
        progressDialog.setCancelable(false);

        // Find the views by their IDs
        emailEditText = findViewById(R.id.input_email);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);
        termTextView = findViewById(R.id.text_terms);

        // Get ColorStateLists
        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));

        // Ban đầu vô hiệu hóa nút Continue nếu email trống hoặc không hợp lệ
        validateEmailAndUpdateButton(emailEditText.getText().toString());


        // Set click listener for the Continue button - **SỬA ĐỔI QUAN TRỌNG**
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Không gọi validateAndContinue nữa, gọi hàm mới
                checkEmailExistsAndProceed();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay về WelcomeActivity hoặc màn hình trước đó phù hợp
                startActivity(new Intent(SignInEmail.this, WelcomeActivity.class));
                finish();
            }
        });

        termTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SignInEmail.this, "Show Term and Condition", Toast.LENGTH_SHORT).show();
            }
        });

        // Add TextWatcher to the email EditText (giữ nguyên)
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                validateEmailAndUpdateButton(s.toString());
                // Xóa lỗi cũ khi người dùng bắt đầu gõ lại
                if (emailEditText.getError() != null) {
                    emailEditText.setError(null);
                }
            }
        });
    }

    // Hàm kiểm tra định dạng email và cập nhật giao diện nút (giữ nguyên)
    private void validateEmailAndUpdateButton(String email) {
        String trimmedEmail = email.trim();
        boolean isValidFormat = !trimmedEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches();

        continueButton.setEnabled(isValidFormat);
        if (isValidFormat) {
            continueButton.setBackgroundTintList(yellowColorStateList);
        } else {
            continueButton.setBackgroundTintList(grayColorStateList);
        }
    }

    // --- HÀM MỚI: KIỂM TRA EMAIL TỒN TẠI VÀ CHUYỂN TIẾP ---
    private void checkEmailExistsAndProceed() {
        String email = emailEditText.getText().toString().trim();

        // Kiểm tra định dạng trước khi gọi Firebase (đã làm trong TextWatcher nhưng check lại cho chắc)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Địa chỉ email không hợp lệ.");
            emailEditText.requestFocus();
            // Toast.makeText(this, "Địa chỉ email không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show(); // Hiển thị loading

        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        progressDialog.dismiss(); // Ẩn loading

                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            // Kiểm tra xem danh sách phương thức đăng nhập có rỗng không
                            boolean emailExists = result.getSignInMethods() != null && !result.getSignInMethods().isEmpty();

                            if (emailExists) {
                                // Email tồn tại -> Chuyển sang màn hình nhập mật khẩu
                                Log.d(TAG, "Email exists. Proceeding to password screen.");
                                Intent intent = new Intent(SignInEmail.this, SignInPassword.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                            } else {
                                // Email không tồn tại
                                Log.d(TAG, "Email does not exist.");
                                emailEditText.setError("Tài khoản không tồn tại."); // Hiển thị lỗi ngay tại EditText
                                emailEditText.requestFocus();
                                // Hoặc dùng Toast: Toast.makeText(SignInEmail.this, "Tài khoản không tồn tại.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Lỗi khi gọi Firebase (ví dụ: mất mạng)
                            Log.w(TAG, "fetchSignInMethodsForEmail:failure", task.getException());
                            Toast.makeText(SignInEmail.this, "Không thể kiểm tra email. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}