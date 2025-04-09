package com.example.locket.ui.settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.locket.R;
import com.example.locket.model.Photo;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final Context context;
    private List<Photo> photoList;
    private OnPhotoClickListener onPhotoClickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo);
    }

    public ImageAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList != null ? photoList : new ArrayList<>();
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Photo photo = photoList.get(position);
        String imageUrl = photo.getImageUrl();
        Log.d("ImageAdapter", "Loading image: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .override(300, 300)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .transform(new CenterCrop(), new RoundedCorners(30))
                    .into(holder.imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_logo)
                    .into(holder.imageView);
        }

        holder.imageView.setOnClickListener(v -> {
            if (onPhotoClickListener != null) {
                onPhotoClickListener.onPhotoClick(photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoList != null ? photoList.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    public void updatePhotos(List<Photo> newPhotoList) {
        if (newPhotoList == null) {
            this.photoList = new ArrayList<>();
        } else {
            this.photoList = new ArrayList<>(newPhotoList);
        }
        notifyDataSetChanged();
    }
}