package com.example.locket.ui.photo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.Photo;
import com.example.locket.model.User;
import com.example.locket.viewmodel.PhotoReactionViewModel;
import com.example.locket.viewmodel.PhotoViewModel;
import com.example.locket.viewmodel.UserViewModel;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetailPhotoFriendAdapter extends RecyclerView.Adapter<DetailPhotoFriendAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<Photo> photoList;
    private final List<User> allUsers;
    private final String currentUserId;
    private final LifecycleOwner lifecycleOwner;
    private final PhotoViewModel photoViewModel;
    private final PhotoReactionViewModel photoReactionViewModel;
    private final UserViewModel userViewModel;

    public DetailPhotoFriendAdapter(Context context, List<Photo> photoList, List<User> allUsers, String currentUserId, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.photoList = photoList;
        this.allUsers = allUsers;
        this.currentUserId = currentUserId;
        this.lifecycleOwner = lifecycleOwner;

        this.photoViewModel = new PhotoViewModel();
        this.photoReactionViewModel = new PhotoReactionViewModel();
        this.userViewModel = ((MyApplication) context.getApplicationContext()).getUserViewModel();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_photofriend_detail, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photoList.get(position));
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {

        ImageView photo, userAvatar;
        TextView userName, postTime, infoText;
        Button songButton;
        RelativeLayout musicContainer;
        CircularProgressView musicProgress;
        ImageView playPauseButton;

        Handler handler = new Handler();
        MediaPlayer mediaPlayer = null;
        boolean isPlaying = false;
        Photo currentPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

            photo = itemView.findViewById(R.id.photo);
            userAvatar = itemView.findViewById(R.id.user_avatar);
            userName = itemView.findViewById(R.id.user_name);
            postTime = itemView.findViewById(R.id.post_time);

            infoText = itemView.findViewById(R.id.photo_caption_or_location);
            songButton = itemView.findViewById(R.id.photo_song_button);
            musicContainer = itemView.findViewById(R.id.music_progress_container);
            musicProgress = itemView.findViewById(R.id.music_progress);
            playPauseButton = itemView.findViewById(R.id.play_pause_button);
        }

        void bind(Photo photoObj) {
            this.currentPhoto = photoObj;

            if (photoObj.isAd()) {
                // Hiển thị quảng cáo
                Glide.with(context)
                        .load(Integer.parseInt(photoObj.getImageUrl())) // Cho drawable
                        // .load(photoObj.getImageUrl()) // Nếu dùng URL
                        .error(R.drawable.ic_profile) // Ảnh dự phòng
                        .into(photo);

                // Ẩn các thành phần không liên quan
                userAvatar.setVisibility(View.GONE);
                userName.setVisibility(View.GONE);
                postTime.setVisibility(View.GONE);
                infoText.setVisibility(View.GONE);
                songButton.setVisibility(View.GONE);
                musicContainer.setVisibility(View.GONE);

                // (Tùy chọn) Thêm sự kiện nhấp để mở liên kết quảng cáo
                photo.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://your-ad-url.com"));
                    context.startActivity(intent);
                });

                // Dừng nhạc nếu đang phát
                stopMusic();
            } else {
                // Hiển thị ảnh bạn bè
                Glide.with(context)
                        .load(photoObj.getImageUrl())
                        .error(R.drawable.ic_profile)
                        .into(photo);

                // Hiển thị thông tin người dùng
                userAvatar.setVisibility(View.VISIBLE);
                userName.setVisibility(View.VISIBLE);
                postTime.setVisibility(View.VISIBLE);

                for (User u : allUsers) {
                    if (u.getUid().equals(photoObj.getUserId())) {
                        userName.setText(u.getUsername());
                        Glide.with(context).load(u.getAvatar()).circleCrop().into(userAvatar);
                        break;
                    }
                }
                postTime.setText(formatTimeDifference(photoObj.getCreatedAt()));

                infoText.setVisibility(View.GONE);
                songButton.setVisibility(View.GONE);
                musicContainer.setVisibility(View.GONE);

                if (photoObj.getCaption() != null && !photoObj.getCaption().isEmpty()) {
                    infoText.setText(photoObj.getCaption());
                    infoText.setVisibility(View.VISIBLE);
                } else if (photoObj.getLocation() != null && !photoObj.getLocation().isEmpty()) {
                    infoText.setText("\uD83C\uDF0D " + photoObj.getLocation());
                    infoText.setVisibility(View.VISIBLE);
                } else if (photoObj.getMusicUrl() != null && !photoObj.getMusicUrl().isEmpty()) {
                    songButton.setVisibility(View.VISIBLE);
                    songButton.setOnClickListener(v -> playMusicWithProgress(photoObj.getMusicUrl()));
                }

                // Xóa sự kiện nhấp chuột cho ảnh bạn bè
                photo.setOnClickListener(null);
            }
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
            stopMusic(); // Dừng nhạc trước đó nếu có

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
                Toast.makeText(context, "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
            }
        }

        private void stopMusic() {
            if (mediaPlayer != null) {
                if (isPlaying) {
                    mediaPlayer.stop();
                    isPlaying = false;
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            handler.removeCallbacks(updateProgressRunnable);
            musicContainer.setVisibility(View.GONE);
            playPauseButton.setImageResource(R.drawable.ic_play);
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
}