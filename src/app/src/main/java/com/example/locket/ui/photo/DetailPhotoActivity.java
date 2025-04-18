package com.example.locket.ui.photo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.example.locket.BaseActivity;
import com.example.locket.BuildConfig;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.data.CloudinaryUploader;
import com.example.locket.model.User;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.PhotoViewModel;
import com.example.locket.viewmodel.SharedPhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import androidx.lifecycle.ViewModelProvider;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DetailPhotoActivity extends BaseActivity implements
        OptionsFragment.OnOptionSelectedListener {

    private UserViewModel userViewModel;
    private FriendViewModel friendViewModel;
    private SharedPhotoViewModel sharedPhotoViewModel;
    private User currentUser;
    FriendsAdapter adapter;
    List<User> friendsList = new ArrayList<>();
    private PhotoViewModel photoViewModel;
    private String photoPath;
    private String userId;
    private ImageView photoView;
    private ViewPager2 viewPager;
    private String selectedMessage = null;
    private String selectedSong = null;
    private String selectedLocation = null;
    private ImageView btnGenerateCaption;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                Log.d("DetailPhotoActivity", "User hiện tại: " + currentUser.getUsername());
                userId = currentUser.getUid();
                friendViewModel.loadFriends(userId);
            } else {
                Log.e("DetailPhotoActivity", "Không tìm thấy user!");
            }
        });

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
        btnGenerateCaption = findViewById(R.id.btn_generate_caption);

        viewPager = findViewById(R.id.view_pager);
        if (viewPager == null) {
            Log.e("DetailPhotoActivity", "ViewPager2 bị null!");
            return;
        }

        OptionsPagerAdapter optionAdapter = new OptionsPagerAdapter(this);
        viewPager.setAdapter(optionAdapter);

        RecyclerView recyclerView = findViewById(R.id.recycler_friends);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new FriendsAdapter(this, friendsList, user -> {
            Log.d("DetailPhotoActivity", "Chọn gửi đến: " + user.getUsername());
        });
        recyclerView.setAdapter(adapter);

        friendViewModel.getFriends().observe(this, friends -> {
            if (friends != null) {
                friendsList.clear();
                User allUser = new User();
                allUser.setFirstname(getString(R.string.all));
                allUser.setAvatar(null);
                friendsList.add(allUser);
                friendsList.addAll(friends);
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView.post(() -> {
            int sidePadding = recyclerView.getWidth() / 2 - 100;
            recyclerView.setPadding(sidePadding, 0, sidePadding, 0);
            recyclerView.setClipToPadding(false);
            layoutManager.scrollToPositionWithOffset(0, sidePadding);
        });

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        photoPath = getIntent().getStringExtra("photo_path");
        if (photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, photoPath);
            photoView.setImageBitmap(rotatedBitmap);
        }

        btnDownload.setOnClickListener(v -> savePhotoToGallery());
        btnClose.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> {
            List<String> selectedFriendIds = adapter.getSelectedFriendIds();
            if (selectedFriendIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một người nhận!", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPhoto(selectedMessage, selectedSong, selectedLocation, selectedFriendIds);
        });

        btnGenerateCaption.setOnClickListener(v -> generateCaption());
    }

    private void generateCaption() {
        if (photoPath == null) {
            Toast.makeText(this, "Không có ảnh để tạo caption!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối internet", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGenerateCaption.setEnabled(false);
        Toast.makeText(this, "Đang tạo caption...", Toast.LENGTH_SHORT).show();

        Uri fileUri = Uri.fromFile(new File(photoPath));
        new Thread(() -> {
            try {
                String imageUrl = CloudinaryUploader.uploadImage(DetailPhotoActivity.this, fileUri);
                if (imageUrl != null) {
                    Log.d("DetailPhotoActivity", "Upload thành công, URL: " + imageUrl);
                    generateCaptionWithGPT4o(imageUrl);
                } else {
                    runOnUiThread(() -> {
                        btnGenerateCaption.setEnabled(true);
                        Toast.makeText(DetailPhotoActivity.this, "Lỗi upload lên Cloudinary", Toast.LENGTH_LONG).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnGenerateCaption.setEnabled(true);
                    Toast.makeText(DetailPhotoActivity.this, "Lỗi khi upload lên Cloudinary: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void generateCaptionWithGPT4o(String imageUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-base";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("inputs", imageUrl);
            RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(HUGGINGFACE_API_URL)
                    .addHeader("Authorization", "Bearer " + BuildConfig.HUGGINGFACE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        btnGenerateCaption.setEnabled(true);
                        String errorMessage = "Lỗi khi gọi API: " + e.getMessage();
                        if (e instanceof java.net.SocketTimeoutException) {
                            errorMessage = "Hết thời gian chờ phản hồi từ API. Vui lòng thử lại!";
                        }
                        Toast.makeText(DetailPhotoActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.d("DetailPhotoActivity", "Phản hồi từ Hugging Face: " + responseBody);

                        try {
                            JSONArray jsonArray = new JSONArray(responseBody);
                            String caption = jsonArray.getJSONObject(0).getString("generated_text");

                            runOnUiThread(() -> {
                                int messageTabIndex = 0;
                                viewPager.setCurrentItem(messageTabIndex, true);

                                Fragment fragment = getSupportFragmentManager()
                                        .findFragmentByTag("f" + messageTabIndex);

                                if (fragment instanceof OptionsFragment) {
                                    ((OptionsFragment) fragment).setMessageText(caption);
                                }

                                btnGenerateCaption.setEnabled(true);
                                Toast.makeText(DetailPhotoActivity.this, "Caption đã chèn vào tin nhắn!", Toast.LENGTH_SHORT).show();
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                btnGenerateCaption.setEnabled(true);
                                Toast.makeText(DetailPhotoActivity.this, "Lỗi parse phản hồi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "Không có nội dung phản hồi";
                        Log.e("DetailPhotoActivity", "Lỗi API: " + response.code() + " - " + response.message() + "\nChi tiết: " + errorBody);
                        runOnUiThread(() -> {
                            btnGenerateCaption.setEnabled(true);
                            Toast.makeText(DetailPhotoActivity.this,
                                    "Lỗi API: " + response.code() + " - " + response.message(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            runOnUiThread(() -> {
                btnGenerateCaption.setEnabled(true);
                Toast.makeText(DetailPhotoActivity.this, "Lỗi khi tạo yêu cầu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void uploadPhoto(String caption, String song, String location, List<String> receiverIds) {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Không có kết nối internet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (photoPath == null || userId == null) {
            Toast.makeText(this, "Không có ảnh hoặc user chưa xác định!", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = Uri.fromFile(new File(photoPath));
        ImageView btnSend = findViewById(R.id.btn_capture);
        btnSend.setPadding(0, 0, 0, 0);
        btnSend.setImageResource(R.drawable.ic_done);

        String finalCaption = (caption != null && !caption.isEmpty())
                ? caption
                : (selectedMessage != null ? selectedMessage : "");
        photoViewModel.uploadPhoto(
                getApplicationContext(),
                fileUri,
                userId,
                finalCaption,
                song,
                location,
                photoId -> {
                    sharedPhotoViewModel.sharePhoto(photoId, userId, friendsList, receiverIds);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        startActivity(new Intent(DetailPhotoActivity.this, PhotoActivity.class));
                        finish();
                    }, 1000);
                }
        );

        photoViewModel.getUploadError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMessageEntered(String message) {
        selectedMessage = message;
        selectedSong = null;
        selectedLocation = null;
    }

    @Override
    public void onMusicSelected(String song) {
        selectedSong = song;
        selectedMessage = null;
        selectedLocation = null;
    }

    @Override
    public void onLocationSelected(String location) {
        selectedLocation = location;
        selectedMessage = null;
        selectedSong = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment != null) {
                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
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
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
            Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, photoPath);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "Locket_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }
                Toast.makeText(this, "Ảnh đã được lưu vào thư viện!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khi lưu ảnh!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}