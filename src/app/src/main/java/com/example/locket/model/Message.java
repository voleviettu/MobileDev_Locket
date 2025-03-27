package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String receiverId;
    private String content;
    private String photoId;

    @ServerTimestamp
    private Date createdAt;

    public Message() {}

    public Message(String senderId, String receiverId, String content, String photoId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.photoId = photoId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
