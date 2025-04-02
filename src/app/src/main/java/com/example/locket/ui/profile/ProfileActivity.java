package com.example.locket.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // *** Thêm Log ***
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // *** Thêm Toast ***

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer; // *** Thêm Observer ***

import com.example.locket.MyApplication; // *** Import MyApplication ***
import com.example.locket.R;
import com.example.locket.model.User; // *** Import User model ***
import com.example.locket.ui.settings.SettingsActivity;
import com.example.locket.viewmodel.UserViewModel; // *** Import UserViewModel ***
import com.google.firebase.auth.FirebaseAuth; // *** Import FirebaseAuth ***
import com.google.firebase.auth.FirebaseUser; // *** Import FirebaseUser ***

// TODO: Import thư viện load ảnh như Glide hoặc Picasso nếu bạn muốn hiển thị avatar từ URL
// import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity"; // *** Thêm TAG ***

    private ImageView avatarImageView; // Đổi tên biến cho rõ ràng
    private TextView usernameTextView;
    private TextView emailTextView;
    private Button btnEditProfile;
    private ImageView btnBack;
    private ImageView btnSettings;

    private UserViewModel userViewModel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- Khởi tạo ---
        mAuth = FirebaseAuth.getInstance();
        // Lấy ViewModel từ Application (Cần đảm bảo MyApplication đã được cấu hình trong Manifest)
        try {
            userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        } catch (ClassCastException e) {
            Log.e(TAG, "Application không phải là instance của MyApplication hoặc ViewModel chưa được khởi tạo.", e);
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu người dùng.", Toast.LENGTH_LONG).show();
            finish(); // Đóng activity nếu không lấy được ViewModel
            return;
        }


        // --- Ánh xạ các thành phần UI ---
        btnBack = findViewById(R.id.btn_back);
        avatarImageView = findViewById(R.id.profile_avatar);
        usernameTextView = findViewById(R.id.profile_username);
        emailTextView = findViewById(R.id.profile_email);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnSettings = findViewById(R.id.btn_settings);

        // --- Đặt giá trị tạm thời/loading ---
        usernameTextView.setText("Đang tải...");
        emailTextView.setText("Đang tải...");
        avatarImageView.setImageResource(R.drawable.default_avatar); // Ảnh avatar mặc định

        // --- Lấy thông tin cơ bản từ FirebaseAuth (nhanh chóng) ---
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Lấy email trực tiếp từ FirebaseUser (thường luôn có sẵn sau khi đăng nhập)
            String emailFromAuth = firebaseUser.getEmail();
            if (emailFromAuth != null && !emailFromAuth.isEmpty()) {
                emailTextView.setText(emailFromAuth);
            } else {
                emailTextView.setText("N/A"); // Hoặc ẩn đi nếu không có email
            }
            // firebaseUser.getDisplayName() thường là null nếu chưa cập nhật profile Auth
            // firebaseUser.getPhotoUrl() thường là null nếu chưa cập nhật profile Auth

        } else {
            // Nếu không có người dùng nào đăng nhập, không nên ở màn hình này
            Log.e(TAG, "Không có người dùng nào đăng nhập trong ProfileActivity!");
            Toast.makeText(this, "Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // TODO: Chuyển hướng về màn hình đăng nhập hoặc WelcomeActivity
            // Intent intent = new Intent(this, WelcomeActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            finish(); // Đóng màn hình profile
            return;
        }

        // --- Lắng nghe dữ liệu chi tiết từ UserViewModel (bao gồm username từ Firestore) ---
        if (userViewModel != null) {
            userViewModel.getCurrentUser().observe(this, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        // Dữ liệu User từ ViewModel đã được cập nhật (đã load từ Firestore)
                        Log.d(TAG, "Dữ liệu User từ ViewModel: " + user.getUsername() + " | " + user.getEmail());
                        usernameTextView.setText(user.getFullName()); // Lấy fullname từ User model
                        emailTextView.setText(user.getEmail()); // Cập nhật lại email từ User model cho nhất quán

                        // TODO: Xử lý hiển thị Avatar thật nếu có URL
                         /*
                         String avatarUrl = user.getAvatarUrl(); // Giả sử có trường avatarUrl trong User model
                         if (avatarUrl != null && !avatarUrl.isEmpty()) {
                             Glide.with(ProfileActivity.this)
                                  .load(avatarUrl)
                                  .placeholder(R.drawable.default_avatar) // Ảnh chờ load
                                  .error(R.drawable.default_avatar) // Ảnh khi lỗi
                                  .circleCrop() // Bo tròn nếu muốn
                                  .into(avatarImageView);
                         } else {
                             avatarImageView.setImageResource(R.drawable.default_avatar);
                         }
                         */

                    } else {
                        // ViewModel trả về user null (có thể đang load hoặc load thất bại)
                        Log.w(TAG, "ViewModel trả về User null.");
                        // Giữ giá trị "Đang tải..." hoặc hiển thị lỗi nếu email từ Auth cũng null
                        if (firebaseUser.getEmail() == null || firebaseUser.getEmail().isEmpty()) {
                            emailTextView.setText("Lỗi tải dữ liệu");
                        }
                        usernameTextView.setText("Lỗi tải dữ liệu");
                    }
                }
            });

            // Đảm bảo ViewModel được yêu cầu load user nếu chưa có (phụ thuộc vào logic của bạn)
            // Nếu bạn chắc chắn ViewModel đã load user ở màn hình trước đó thì không cần dòng này
            if (userViewModel.getCurrentUser().getValue() == null) {
                Log.d(TAG,"Yêu cầu ViewModel load user: " + firebaseUser.getUid());
                userViewModel.loadUser(firebaseUser.getUid());
            }

        } else {
            Log.e(TAG, "UserViewModel is null, không thể observe data.");
            usernameTextView.setText("Lỗi");
            // Email vẫn giữ giá trị từ FirebaseAuth nếu có
        }


        // --- Xử lý sự kiện các nút ---
        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditNameActivity.class);
            // TODO: Truyền thông tin user hiện tại (nếu màn hình Edit cần)
            // intent.putExtra("currentUsername", usernameTextView.getText().toString());
            // intent.putExtra("currentEmail", emailTextView.getText().toString());
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}