package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class SharedPhoto {
    private String photoId;
    private String senderId;
    private String receiverId;
    @ServerTimestamp
    private Date createdAt;

    public SharedPhoto() {}

    public SharedPhoto(String photoId, String senderId, String receiverId) {
        this.photoId = photoId;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
