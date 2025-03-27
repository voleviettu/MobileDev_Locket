package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Friend {
    private String id;
    private String userId;
    private String friendId;
    private String status;       // "pending", "requested", "accepted"
    @ServerTimestamp
    private Date createdAt;


    public Friend() {}

    public Friend(String userId, String friendId, String status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
