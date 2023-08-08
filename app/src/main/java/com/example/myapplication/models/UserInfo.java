package com.example.myapplication.models;

import java.io.Serializable;
import java.util.HashMap;

public class UserInfo implements Serializable {
    private String username;
    private String email;
    private String phone;

    public UserInfo() {
    }

    public UserInfo(String username, String email, String phone) {
        this.username = username;
        this.email = email;
        this.phone = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
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
    public HashMap<String, Object> convertToHashMap() {
        HashMap<String, Object> work = new HashMap<>();
        work.put("email", email);
        work.put("phone", phone);
        work.put("username", username);
        return work;
    }
}
