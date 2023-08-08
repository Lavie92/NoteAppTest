package com.example.myapplication.auth;

public class UserInfo {
    private String username;
    private String email;
    private String phone;

    public UserInfo(String username, String email, String phone) {
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
