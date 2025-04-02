package com.example.locket.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <<< Import thư viện Glide
import com.example.locket.R;
import com.example.locket.model.Photo; // Đảm bảo import đúng lớp Photo của bạn
// import com.squareup.picasso.Picasso; // <<< Không cần Picasso nữa

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Photo> photoList;

    public ImageAdapter(Context context, List<Photo> photoList) {
        this.context = context;
        this.photoList = photoList;
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

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_logo)
                    .error(R.drawable.ic_logo)
                    .into(holder.imageView);
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_logo)
                    .into(holder.imageView);
        }
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
        this.photoList.clear();
        if (newPhotoList != null) {
            this.photoList.addAll(newPhotoList);
        }
        notifyDataSetChanged();
    }
}