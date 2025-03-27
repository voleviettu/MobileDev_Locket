package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String id;
    private String userId; // người nhận
    private String senderId;
    private String type;
    private String content;
    private String photoId;
    private boolean isRead;

    @ServerTimestamp
    private Date createdAt;

    public Notification() {}

    public Notification(String userId, String senderId, String type, String content, String photoId, boolean isRead) {
        this.userId = userId;
        this.senderId = senderId;
        this.type = type;
        this.content = content;
        this.photoId = photoId;
        this.isRead = isRead;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
