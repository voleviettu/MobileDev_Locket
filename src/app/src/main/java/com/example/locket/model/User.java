package com.example.locket.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String phone;
    private String username;
    private String avatar;
    private List<String> friends;
    @ServerTimestamp
    private Date createdAt;

    public User() {}

    public User(String userId, String email, String phone, String username, String avatar, List<String> friends) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.avatar = avatar;
        this.friends = friends;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<String> getFriends() { return friends; }
    public void setFriends(List<String> friends) { this.friends = friends; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
