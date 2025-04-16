package com.example.locket.model;

import java.util.Date;

public class Chat {
    private String friendId;
    private String name;
    private String message;
    private String time;
    private String avatarUrl;
    private Date createdAt;

    public Chat(String friendId, String name, String message, String time, String avatarUrl) {
        this.friendId = friendId;
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarUrl = avatarUrl;
        this.createdAt = null;
    }

    // Getters và setters
    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}