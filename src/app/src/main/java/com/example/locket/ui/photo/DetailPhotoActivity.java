package com.example.locket.ui.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.locket.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DetailPhotoActivity extends AppCompatActivity {

    private String photoPath;
    private ImageView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        photoView = findViewById(R.id.photo);
        ImageView btnDownload = findViewById(R.id.btn_download);
        ImageView btnClose = findViewById(R.id.btn_flash);

        photoPath = getIntent().getStringExtra("photo_path");
        if (photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, photoPath);
            photoView.setImageBitmap(rotatedBitmap);
        }

        btnDownload.setOnClickListener(v -> savePhotoToGallery());
        btnClose.setOnClickListener(v -> finish());
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
