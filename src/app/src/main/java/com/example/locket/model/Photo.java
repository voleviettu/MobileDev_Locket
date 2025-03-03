package com.example.locket.model;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class Photo {
    private String photoId;
    private String userId;
    private String imageUrl;
    private String caption;
    private String musicUrl;
    private GeoPoint location;
    private List<String> receivers;

    @ServerTimestamp
    private Date createdAt;

    public Photo() {}

    public Photo(String photoId, String userId, String imageUrl, String caption, String musicUrl, GeoPoint location, List<String> receivers) {
        this.photoId = photoId;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.musicUrl = musicUrl;
        this.location = location;
        this.receivers = receivers;
    }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public String getMusicUrl() { return musicUrl; }
    public void setMusicUrl(String musicUrl) { this.musicUrl = musicUrl; }

    public GeoPoint getLocation() { return location; }
    public void setLocation(GeoPoint location) { this.location = location; }

    public List<String> getReceivers() { return receivers; }
    public void setReceivers(List<String> receivers) { this.receivers = receivers; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
