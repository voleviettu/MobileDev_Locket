package com.example.locket.model;

public class Chat {
    private String friendId; // Thêm friendId để lưu ID của bạn bè
    private String name; // Tên bạn bè
    private String message; // Tin nhắn gần nhất (hoặc "Chưa có câu trả lời nào!")
    private String time; // Thời gian đã trôi qua (hoặc rỗng nếu chưa có tin nhắn)
    private String avatarUrl; // URL ảnh đại diện (thay vì int resource)

    public Chat(String friendId, String name, String message, String time, String avatarUrl) {
        this.friendId = friendId;
        this.name = name;
        this.message = message;
        this.time = time;
        this.avatarUrl = avatarUrl;
    }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}