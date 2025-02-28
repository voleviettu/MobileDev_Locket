package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Photo {
    private String photoId;
    private String userId;
    private String imageUrl;
    private String caption;

    @ServerTimestamp
    private Date timestamp;

    public Photo() {}

    public Photo(String photoId, String userId, String imageUrl, String caption) {
        this.photoId = photoId;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    public String getPhotoId() {
        return photoId;
    }
    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
