package com.example.locket.auth;

import com.example.locket.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import java.util.HashMap; // Để tạo đối tượng dữ liệu cho Firestore
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.*;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.*;

public class SignUpUsername extends AppCompatActivity {

    private EditText usernameEditText;
    private Button continueButton;
    private ImageButton backButton;
    private ColorStateList yellowColorStateList;
    private ColorStateList grayColorStateList;

    // Biến để lưu trữ dữ liệu từ các màn hình trước
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    // Firebase Auth và Firestore
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // Hoặc DatabaseReference nếu dùng Realtime DB

    private ProgressDialog progressDialog;

    private static final String TAG = "SignUpUsername"; // Tag để log lỗi

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_username);

        // Khởi tạo Firebase Auth và Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Hoặc FirebaseDatabase.getInstance().getReference();

        // Khởi tạo ProgressDialog (tùy chọn)
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tạo tài khoản...");
        progressDialog.setCancelable(false);


        // ... findViewById như cũ ...
        usernameEditText = findViewById(R.id.input_username);
        continueButton = findViewById(R.id.button_continue);
        backButton = findViewById(R.id.button_back);

        yellowColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.button));
        grayColorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.gray_light));

        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            firstName = intent.getStringExtra("firstName");
            lastName = intent.getStringExtra("lastName");
        } else {
            // Xử lý lỗi nếu không có dữ liệu (ví dụ: quay lại màn hình trước)
            Log.e(TAG, "Intent is null or missing data");
            Toast.makeText(this, "Lỗi: Không nhận được dữ liệu đăng ký.", Toast.LENGTH_LONG).show();
            finish(); // Quay lại màn hình trước
            return; // Dừng thực thi onCreate
        }


        // ... setOnClickListener và addTextChangedListener như cũ ...
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Không gọi validateAndContinue cũ nữa, mà gọi hàm đăng ký Firebase
                attemptRegistration();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi back, không cần truyền lại password vì lý do bảo mật
                // Intent backIntent = new Intent(SignUpUsername.this, SignUpFullName.class);
                // backIntent.putExtra("email", email);
                // backIntent.putExtra("password", password); // Có thể không cần gửi lại pass
                // backIntent.putExtra("firstName", firstName); // Gửi lại để user không phải nhập lại
                // backIntent.putExtra("lastName", lastName);
                // startActivity(backIntent);
                finish(); // Đơn giản là đóng activity hiện tại
            }
        });

        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                validateUsernameAndUpdateButton(s.toString());
            }
        });

        continueButton.setEnabled(false);
        continueButton.setBackgroundTintList(grayColorStateList);
    }

    // Hàm kiểm tra username và cập nhật nút (giữ nguyên)
    private void validateUsernameAndUpdateButton(String username) {
        String trimmedUsername = username.trim();
        boolean isValid = trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9_.]*$"); // Cho phép chữ, số, _, .

        continueButton.setEnabled(isValid);
        if (isValid) {
            continueButton.setBackgroundTintList(yellowColorStateList);
        } else {
            continueButton.setBackgroundTintList(grayColorStateList);
        }
    }

    // Hàm kiểm tra username (để dùng trước khi gọi Firebase)
    private boolean validateUsername(String username) {
        String trimmedUsername = username.trim();
        return trimmedUsername.length() >= 3 &&
                !trimmedUsername.contains(" ") &&
                trimmedUsername.matches("^[a-zA-Z0-9_.]*$");
    }

    // **Hàm mới để thực hiện đăng ký với Firebase**
    private void attemptRegistration() {
        String username = usernameEditText.getText().toString().trim();

        // Kiểm tra lại lần cuối trước khi gọi Firebase
        if (!validateUsername(username)) {
            Toast.makeText(this, "Username không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra null cho các biến quan trọng (đề phòng)
        if (email == null || password == null || firstName == null || lastName == null) {
            Log.e(TAG, "Attempting registration with null data.");
            Toast.makeText(this, "Đã xảy ra lỗi. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            // Có thể điều hướng người dùng về màn hình đầu tiên
            Intent intent = new Intent(SignUpUsername.this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }


        progressDialog.show(); // Hiển thị loading (tùy chọn)

        // 1. Tạo tài khoản bằng email và password với Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng ký Auth thành công
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // 2. Lưu thông tin bổ sung (tên, username) vào Firestore
                                saveAdditionalUserInfo(firebaseUser.getUid(), username);
                            } else {
                                // Trường hợp hiếm gặp: user null sau khi tạo thành công
                                progressDialog.dismiss();
                                Log.w(TAG, "createUserWithEmail:success, but firebaseUser is null");
                                Toast.makeText(SignUpUsername.this, "Đã xảy ra lỗi không xác định.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Đăng ký Auth thất bại
                            progressDialog.dismiss();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            // Hiển thị lỗi cụ thể hơn nếu có thể
                            String errorMessage = "Đăng ký thất bại.";
                            try {
                                throw task.getException();
                            } catch(com.google.firebase.auth.FirebaseAuthUserCollisionException e) {
                                errorMessage = "Địa chỉ email đã tồn tại.";
                            } catch(com.google.firebase.auth.FirebaseAuthWeakPasswordException e) {
                                errorMessage = "Mật khẩu quá yếu.";
                            } catch(Exception e) {
                                errorMessage = "Lỗi: " + e.getMessage();
                            }
                            Toast.makeText(SignUpUsername.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // **Hàm mới để lưu thông tin người dùng vào Firestore**
    private void saveAdditionalUserInfo(String userId, String username) {
        // Tạo một đối tượng Map để lưu dữ liệu
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("username", username);
        user.put("email", email); // Lưu lại email để tiện truy vấn nếu cần
        // Bạn có thể thêm các trường khác như ngày tạo, ảnh đại diện mặc định,...
        // user.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        // Lưu vào Firestore trong collection "users" với document ID là UID của user
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss(); // Ẩn loading
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile created in Firestore successfully.");
                            // 3. Chuyển đến màn hình chính hoặc WelcomeActivity sau khi hoàn tất
                            Toast.makeText(SignUpUsername.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(SignUpUsername.this, WelcomeActivity.class); // Hoặc MainActivity nếu muốn vào thẳng app
                            // Xóa các activity đăng ký khỏi stack để người dùng không back lại được
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Đóng activity hiện tại
                        } else {
                            Log.w(TAG, "Error adding document to Firestore", task.getException());
                            // Xử lý lỗi khi lưu vào Firestore (hiếm khi xảy ra nếu Auth thành công)
                            // Có thể cân nhắc xóa tài khoản Auth vừa tạo nếu lưu Firestore lỗi? (Phức tạp hơn)
                            Toast.makeText(SignUpUsername.this, "Lỗi lưu thông tin người dùng.", Toast.LENGTH_SHORT).show();
                            // Nên đăng xuất người dùng để họ thử lại?
                            // mAuth.signOut();
                        }
                    }
                });
    }


    // Bỏ phương thức validateAndContinue cũ đi vì đã thay bằng attemptRegistration
   /*
   private void validateAndContinue() {
       String username = usernameEditText.getText().toString().trim();

       // ... (logic cũ bị thay thế) ...
   }
   */
}