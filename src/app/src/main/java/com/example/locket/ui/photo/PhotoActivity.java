package com.example.locket.ui.photo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import com.example.locket.MyApplication;
import com.example.locket.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.locket.ui.profile.ProfileActivity; // để navigate đến pf
import com.example.locket.ui.photo.DetailPhotoFriendActivity; // để qua friend phôt

import androidx.lifecycle.ViewModelProvider;
import com.example.locket.viewmodel.UserViewModel;
import com.example.locket.model.User;

public class PhotoActivity extends AppCompatActivity {
    private UserViewModel userViewModel;
    private User currentUser;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ExecutorService cameraExecutor;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{android.Manifest.permission.CAMERA};
    private CameraSelector cameraSelector = new CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build();  // để xoay camera
    private boolean isFlashOn = false;
    private boolean isBackCamera = true;
    int friendCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                friendCount = currentUser.getFriends() != null ? currentUser.getFriends().size() : 0;
                Log.d("PhotoActivity", "User hiện tại: " + currentUser.getUsername());

                TextView numFriendsTextView = findViewById(R.id.num_friends);
                numFriendsTextView.setText(friendCount + " Bạn bè");
            } else {
                Log.e("PhotoActivity", "Không tìm thấy user!");
            }
        });



        previewView = findViewById(R.id.view_finder);
        ImageView btnCapture = findViewById(R.id.btn_capture);
        ImageView btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        ImageView btnFlash = findViewById(R.id.btn_flash);

        // chuyển đến profile
        ImageView btnProfile = findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // chuyển đến ảnh của bạn bè
        LinearLayout btnFriend = findViewById(R.id.history_container);
        btnFriend.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, DetailPhotoFriendActivity.class);
            startActivity(intent);
        });

        // upload ảnh từ máy (gold)
        ImageView btnUploadImg = findViewById(R.id.btn_upload);
        btnUploadImg.setOnClickListener(v -> {
            Intent intent = new Intent(PhotoActivity.this, UploadImageActivity.class);
            startActivity(intent);
        });

        // phần chụp ảnh
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
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

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
            if (ContextCompat.checkSelfPermission(this, permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
