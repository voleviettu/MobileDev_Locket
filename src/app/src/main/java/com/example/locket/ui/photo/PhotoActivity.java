package com.example.locket.ui.photo;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.camera.core.Preview;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.ui.friend.FriendList;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.locket.ui.profile.ProfileActivity;
import com.example.locket.ui.photo.DetailPhotoFriendActivity;
import com.example.locket.ui.chat.FullChatActivity;

import androidx.lifecycle.ViewModelProvider;
import com.example.locket.viewmodel.UserViewModel;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.model.User;

public class PhotoActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private FriendViewModel friendViewModel;
    private User currentUser;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA};
    private CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();
    private boolean isFlashOn = false;
    private boolean isBackCamera = true;
    int friendCount;

    private static final int REQUEST_CODE_PICK_IMAGE = 101;
    // Khai báo launcher xin quyền
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        // Đăng ký ActivityResultLauncher để xin quyền đọc bộ nhớ
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(PhotoActivity.this, "Quyền đọc bộ nhớ bị từ chối!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                ImageView btnProfile = findViewById(R.id.btn_profile);
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(this)
                            .load(user.getAvatar())
                            .circleCrop()
                            .into(btnProfile);
                } else {
                    btnProfile.setImageResource(R.drawable.ic_profile);
                }

                friendViewModel.loadFriends(currentUser.getUid());
                friendViewModel.getFriends().observe(this, friends -> {
                    int friendCount = friends != null ? friends.size() : 0;
                    TextView numFriendsTextView = findViewById(R.id.num_friends);
                    numFriendsTextView.setText(friendCount + " Bạn bè");
                });
                Log.d("PhotoActivity", "User hiện tại: " + currentUser.getUsername());
            } else {
                Log.e("PhotoActivity", "Không tìm thấy user!");
            }
        });

        previewView = findViewById(R.id.view_finder);
        ImageView btnCapture = findViewById(R.id.btn_capture);
        ImageView btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        ImageView btnFlash = findViewById(R.id.btn_flash);

        // Chuyển đến Profile
        ImageView btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Chuyển đến Chat
        ImageView btnChat = findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, FullChatActivity.class);
            startActivity(intent);
        });

        // Chuyển đến ảnh của bạn bè
        LinearLayout btnFriend = findViewById(R.id.history_container);
        btnFriend.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, DetailPhotoFriendActivity.class);
            startActivity(intent);
        });

        LinearLayout btnFriendList = findViewById(R.id.btn_friends);
        btnFriendList.setOnClickListener(v-> {
            Intent intent = new Intent(PhotoActivity.this, FriendList.class);
            startActivity(intent);
        });

        // Upload ảnh (nút upload ảnh)
        ImageView btnUploadImg = findViewById(R.id.btn_upload);
        btnUploadImg.setOnClickListener(v -> {
            if (currentUser != null) {
                if (currentUser.isPremium()) {
                    // Nếu là gold user thì mở thư viện ảnh
                    checkStoragePermission();
                } else {
                    // Nếu là user thường thì chuyển sang UploadImageActivity
                    Intent intent = new Intent(PhotoActivity.this, UploadImageActivity.class);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(PhotoActivity.this, "Chưa load thông tin user, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        // Phần chụp ảnh
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCapture.setOnClickListener(v -> takePhoto());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnFlash.setOnClickListener(v -> toggleFlash(btnFlash));
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setFlashMode(isFlashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF)
                        .build();
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng chờ tải user...", Toast.LENGTH_SHORT).show();
            Log.e("PhotoActivity", "currentUser == null, không có user để lưu ảnh");
            return;
        }
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Intent intent = new Intent(PhotoActivity.this, DetailPhotoActivity.class);
                        intent.putExtra("photo_path", photoFile.getAbsolutePath());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(PhotoActivity.this, "Chụp ảnh thất bại!", Toast.LENGTH_SHORT).show();
                        Log.e("CameraX", "Photo capture failed: " + exception.getMessage());
                    }
                });
    }

    private void switchCamera() {
        isBackCamera = !isBackCamera;
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(isBackCamera ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT)
                .build();
        startCamera();
    }

    private void toggleFlash(ImageView btnFlash) {
        isFlashOn = !isFlashOn;
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(isFlashOn);
        }
        btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Quyền camera bị từ chối!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // Phương thức kiểm tra quyền đọc bộ nhớ và xin quyền nếu cần
    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    // Phương thức mở thư viện ảnh
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                copyImageFromUriAndProceed(selectedImageUri);
            } else {
                Toast.makeText(this, "Không có ảnh được chọn", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copyImageFromUriAndProceed(Uri imageUri) {
        try {
            // Mở InputStream từ URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            // Tạo file mới với tên dựa trên thời gian hiện tại
            File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
            OutputStream outputStream = new FileOutputStream(photoFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            // Sau khi copy xong, chuyển sang DetailPhotoActivity với file path
            Intent intent = new Intent(PhotoActivity.this, DetailPhotoActivity.class);
            intent.putExtra("photo_path", photoFile.getAbsolutePath());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xử lý ảnh từ thư viện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("PhotoActivity", "Error copying image: ", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}