package com.example.locket.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.data.CloudinaryUploader;
import com.example.locket.model.User;
import com.example.locket.ui.friend.FriendList;
import com.example.locket.ui.settings.SettingsActivity;
import com.example.locket.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.InputStream;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ImageView avatarImageView;
    private ImageView addAvatarBtn;
    private TextView usernameTextView;
    private TextView emailTextView;
    private Button btnEditProfile;
    private ImageView btnBack;
    private ImageView btnSettings;
    private ImageView btnFriend;

    private UserViewModel userViewModel;
    private FirebaseAuth mAuth;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        // Lấy ViewModel từ Application
        try {
            userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        } catch (ClassCastException e) {
            Log.e(TAG, "Application không phải là instance của MyApplication hoặc ViewModel chưa được khởi tạo.", e);
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnBack = findViewById(R.id.btn_back);
        avatarImageView = findViewById(R.id.profile_avatar);
        usernameTextView = findViewById(R.id.profile_username);
        emailTextView = findViewById(R.id.profile_email);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnSettings = findViewById(R.id.btn_settings);
        btnFriend = findViewById(R.id.btn_friends);
        addAvatarBtn = findViewById(R.id.add_avatar_button);

        usernameTextView.setText("Đang tải...");
        emailTextView.setText("Đang tải...");
        avatarImageView.setImageResource(R.drawable.default_avatar); // Ảnh avatar mặc định


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
            Log.e(TAG, "Không có người dùng nào đăng nhập trong ProfileActivity!");
            Toast.makeText(this, "Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (userViewModel != null) {
            userViewModel.getCurrentUser().observe(this, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        Log.d(TAG, "Dữ liệu User từ ViewModel: " + user.getUsername() + " | " + user.getEmail());
                        usernameTextView.setText(user.getFullName()); // Lấy fullname từ User model
                        emailTextView.setText(user.getEmail());

                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            loadImageFromUrl(user.getAvatar());
                        }


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

            // Đảm bảo ViewModel được yêu cầu load user nếu chưa có
            if (userViewModel.getCurrentUser().getValue() == null) {
                Log.d(TAG,"Yêu cầu ViewModel load user: " + firebaseUser.getUid());
                userViewModel.loadUser(firebaseUser.getUid());
            }

        } else {
            Log.e(TAG, "UserViewModel is null, không thể observe data.");
            usernameTextView.setText("Lỗi");
        }

        avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
        });

        addAvatarBtn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh đại diện"), PICK_IMAGE_REQUEST);
        });

        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditNameActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        btnFriend.setOnClickListener(v-> {
            Intent intent = new Intent(ProfileActivity.this, FriendList.class);
            startActivity(intent);
        });
    }

    private void loadImageFromUrl(String url) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream input = new java.net.URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                avatarImageView.setImageBitmap(bitmap);

                runOnUiThread(() -> avatarImageView.setImageBitmap(bitmap));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // Optional: Hiển thị ảnh tạm thời
            avatarImageView.setImageURI(selectedImageUri);

            // Upload ảnh lên Cloudinary trong thread mới
            new Thread(() -> {
                String imageUrl = CloudinaryUploader.uploadImage(ProfileActivity.this, selectedImageUri);
                if (imageUrl != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Tải ảnh lên thành công", Toast.LENGTH_SHORT).show();
                    });

                    // Cập nhật Firestore
                    userViewModel.updateUserAvatar(imageUrl);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ProfileActivity.this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            userViewModel.loadUser(firebaseUser.getUid());
        }
    }
}