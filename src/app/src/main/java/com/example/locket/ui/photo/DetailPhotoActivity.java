package com.example.locket.ui.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.locket.data.CloudinaryUploader;


import androidx.lifecycle.ViewModelProvider;
import com.example.locket.viewmodel.PhotoViewModel;
import com.example.locket.data.PhotoRepository;
import com.google.firebase.firestore.GeoPoint;
import android.net.Uri;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;


public class DetailPhotoActivity extends AppCompatActivity {
    private PhotoViewModel photoViewModel;

    private String photoPath;
    private String userId;
    private ImageView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.getIsUploading().observe(this, isUploading -> {
            if (isUploading) {
                Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
            }
        });

        photoView = findViewById(R.id.photo);
        ImageView btnDownload = findViewById(R.id.btn_download);
        ImageView btnClose = findViewById(R.id.btn_flash);
        ImageView btnSend = findViewById(R.id.btn_capture);

        userId = getIntent().getStringExtra("user_id");

        photoPath = getIntent().getStringExtra("photo_path");
        if (photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, photoPath);
            photoView.setImageBitmap(rotatedBitmap);
        }
        EditText captionInput = findViewById(R.id.message_input);

        btnDownload.setOnClickListener(v -> savePhotoToGallery());
        btnClose.setOnClickListener(v -> finish());
        // Xử lý sự kiện bấm nút Gửi
        btnSend.setOnClickListener(v -> uploadPhoto(captionInput.getText().toString()));
    }

    private void uploadPhoto(String caption) {
        if (photoPath == null || userId == null) {
            Toast.makeText(this, "Không có ảnh hoặc user chưa xác định!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = Uri.fromFile(new File(photoPath));
        ImageView btnSend = findViewById(R.id.btn_capture);
        btnSend.setPadding(0, 0, 0, 0);
        btnSend.setImageResource(R.drawable.ic_done);

        photoViewModel.uploadPhoto(
                getApplicationContext(),
                fileUri,
                userId,
                caption,
                null,
                null,
                new ArrayList<>()
        );
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(DetailPhotoActivity.this, PhotoActivity.class));
            finish();
        }, 1000);

    }

    private Bitmap rotateImageIfRequired(Bitmap bitmap, String photoPath) {
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void savePhotoToGallery() {
        if (photoPath == null) return;

        File srcFile = new File(photoPath);
        File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Locket_" + System.currentTimeMillis() + ".jpg");

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
            Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, photoPath);

            FileOutputStream out = new FileOutputStream(destFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Toast.makeText(this, "Ảnh đã được lưu!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
