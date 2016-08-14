package com.example.chaemingyun.qwerty.firebase.database.model;

/**
 * Created by chaemingyun on 2016. 8. 10..
 */
public class User {
    private String userUid;
    private String email;
    private String displayName;

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
