package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class PhotoReaction {
    private String id;
    private String userId;
    private String photoId;
    private String reaction;

    @ServerTimestamp
    private Date createdAt;

    public PhotoReaction() {}

    public PhotoReaction(String userId, String photoId, String reaction) {
        this.userId = userId;
        this.photoId = photoId;
        this.reaction = reaction;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
