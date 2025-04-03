package com.example.locket.ui.photo;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.model.User;
import com.example.locket.utils.NavigationUtils;
import com.example.locket.viewmodel.SharedPhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.media.MediaPlayer;

public class DetailPhotoFriendActivity extends AppCompatActivity {
    private ImageView btnChat, photo, userAvatar, btnShowAll, btnOption;
    private TextView userName, postTime, infoText;
    private Button songButton;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isPlaying = false;

    private CircularProgressView musicProgress;
    private ImageView playPauseButton;
    private RelativeLayout musicContainer;

    private SharedPhotoViewModel sharedPhotoViewModel;
    private UserViewModel userViewModel;
    private List<User> allUsers;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photofriend_detail);

        btnChat = findViewById(R.id.btn_chat);
        photo = findViewById(R.id.photo);
        userAvatar = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        btnShowAll = findViewById(R.id.btn_showall);
        postTime = findViewById(R.id.post_time);
        btnOption = findViewById(R.id.btn_option);
        infoText = findViewById(R.id.photo_caption_or_location);
        songButton = findViewById(R.id.photo_song_button);
        infoText.setVisibility(View.GONE);
        songButton.setVisibility(View.GONE);
        musicProgress = findViewById(R.id.music_progress);
        playPauseButton = findViewById(R.id.play_pause_button);
        musicContainer = findViewById(R.id.music_progress_container);


        userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        sharedPhotoViewModel = new ViewModelProvider(this).get(SharedPhotoViewModel.class);

        userViewModel.getAllUsers().observe(this, users -> {
            if (users != null) {
                allUsers = users;
            }
        });

        userViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                loadLatestPhoto(user.getUid());
            } else {
                Log.e("DetailPhotoFriend", "Không tìm thấy user!");
            }
        });

        btnShowAll.setOnClickListener(v -> {
            Intent intent = new Intent(DetailPhotoFriendActivity.this, FullPhotoActivity.class);
            startActivity(intent);
        });

        NavigationUtils.setChatButtonClickListener(btnChat, this);

        btnOption.setOnClickListener(v -> {
            View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_options, null);
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(view);

            // Cho phép chạm ra ngoài để đóng (mặc định đã là true)
            bottomSheetDialog.setCanceledOnTouchOutside(true); // <- dòng này có thể thêm cho chắc

            Button btnSave = view.findViewById(R.id.btn_save);
            Button btnDelete = view.findViewById(R.id.btn_delete);

            btnSave.setOnClickListener(view1 -> {
                // TODO: Xử lý khi nhấn "Lưu"
                bottomSheetDialog.dismiss();
            });

            btnDelete.setOnClickListener(view2 -> {
                // TODO: Xử lý khi nhấn "Xóa"
                bottomSheetDialog.dismiss();
            });

            bottomSheetDialog.show();
        });
    }

    private void loadLatestPhoto(String userId) {
        sharedPhotoViewModel.getSharedPhotos(userId).observe(this, photos -> {
            if (photos != null && !photos.isEmpty()) {
                Photo latestPhoto = photos.get(0);

                String username = findUsernameByUid(latestPhoto.getUserId());
                userName.setText(username);

                if (latestPhoto.getCaption() != null && !latestPhoto.getCaption().isEmpty()) {
                    infoText.setText(latestPhoto.getCaption());
                    infoText.setVisibility(View.VISIBLE);
                } else if (latestPhoto.getLocation() != null && !latestPhoto.getLocation().isEmpty()) {
                    infoText.setText("\uD83C\uDF0D " + latestPhoto.getLocation());
                    infoText.setVisibility(View.VISIBLE);
                } else if (latestPhoto.getMusicUrl() != null && !latestPhoto.getMusicUrl().isEmpty()) {
                    songButton.setVisibility(View.VISIBLE);
                    songButton.setOnClickListener(v -> {
                        String musicUrl = latestPhoto.getMusicUrl();
                        if (musicUrl != null && !musicUrl.isEmpty()) {
                            playMusicWithProgress(musicUrl);
                        } else {
                            Toast.makeText(this, "Không có nhạc để phát", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                postTime.setText(formatTimeDifference(latestPhoto.getCreatedAt()));

                Glide.with(this).load(latestPhoto.getImageUrl()).into(photo);
            } else {
                Toast.makeText(this, "Không có ảnh nào được chia sẻ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String findUsernameByUid(String uid) {
        if (allUsers != null) {
            for (User user : allUsers) {
                if (user.getUid().equals(uid)) {
                    return user.getUsername();
                }
            }
        }
        return "Không xác định";
    }

    private String formatTimeDifference(Date createdAt) {
        if (createdAt == null) return "Vừa xong";

        long diffMillis = new Date().getTime() - createdAt.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis);
        long hours = TimeUnit.MILLISECONDS.toHours(diffMillis);
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
        long weeks = days / 7;

        if (minutes < 60) {
            return minutes + "m";
        } else if (hours < 24) {
            return hours + "h";
        } else if (days < 7) {
            return days + "d";
        } else {
            return weeks + "w";
        }
    }

    private void playMusicWithProgress(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        try {
            if (url.startsWith("http://")) {
                url = url.replaceFirst("http://", "https://");
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                musicContainer.setVisibility(View.VISIBLE);
                playPauseButton.setImageResource(R.drawable.ic_stop);
                startProgressUpdate();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                playPauseButton.setImageResource(R.drawable.ic_play);
                handler.removeCallbacks(updateProgressRunnable);
                musicContainer.setVisibility(View.GONE);
            });
            mediaPlayer.prepareAsync();

            playPauseButton.setOnClickListener(v -> {
                if (isPlaying) {
                    mediaPlayer.pause();
                    isPlaying = false;
                    playPauseButton.setImageResource(R.drawable.ic_play);
                    handler.removeCallbacks(updateProgressRunnable);
                } else {
                    mediaPlayer.start();
                    isPlaying = true;
                    playPauseButton.setImageResource(R.drawable.ic_stop);
                    startProgressUpdate();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable updateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && isPlaying) {
                int duration = mediaPlayer.getDuration();
                int position = mediaPlayer.getCurrentPosition();
                float progress = (float) position / duration;
                musicProgress.setProgress(progress);
                handler.postDelayed(this, 100);
            }
        }
    };

    private void startProgressUpdate() {
        handler.post(updateProgressRunnable);
    }

}

