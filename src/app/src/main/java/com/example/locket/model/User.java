package com.example.locket.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class User {
    private String uid;
    private String email;
    private String firstname;
    private String lastname;
    private String username;
    private String avatar;
    private boolean isPremium;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    public User() {}

    public User(String uid, String email, String firstname, String lastname, String username, String avatar, boolean isPremium) {
        this.uid = uid;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.username = username;
        this.avatar = avatar;
        this.isPremium = isPremium;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() {
        return avatar != null ? avatar.replace("http://", "https://") : null;
    }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Exclude // Đánh dấu để Firestore không cố lưu trường này
    public String getFullName() {
        String first = (firstname != null) ? firstname.trim() : "";
        String last = (lastname != null) ? lastname.trim() : "";

        if (!first.isEmpty() && !last.isEmpty()) {
            return first + " " + last;
        } else if (!first.isEmpty()) {
            return first;
        } else if (!last.isEmpty()) {
            return last;
        } else {
            return (username != null) ? username : "";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return uid != null && uid.equals(user.uid);
    }

    @Override
    public int hashCode() {
        return uid != null ? uid.hashCode() : 0;
    }

}
