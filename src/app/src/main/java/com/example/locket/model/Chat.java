package com.example.locket.model;

public class Chat {
    private String name;
    private String message;
    private String time;
    private int profileImageResId;

    public Chat(String name, String message, String time, int profileImageResId) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.profileImageResId = profileImageResId;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getProfileImageResId() {
        return profileImageResId;
    }
}
