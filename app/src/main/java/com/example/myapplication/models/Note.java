package com.example.myapplication.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Note implements Serializable {
    private String id;
    private Date dateTime;

    private String content;
    private long timeAlarm;
    private boolean isReminder;

    public long getTimeAlarm() {
        return timeAlarm;
    }

    public void setTimeAlarm(long timeAlarm) {
        this.timeAlarm = timeAlarm;
    }

    public boolean isReminder() {
        return isReminder;
    }

    public void setReminder(boolean reminder) {
        isReminder = reminder;
    }

    public Note() {
    }

    public Note(String content) {
        this.id = id;
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);
        this.dateTime = date;
        this.content = content;
        isReminder = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public HashMap<String, Object> convertHashMap() {
        HashMap<String, Object> work = new HashMap<>();
        work.put("id", id);
        work.put("content", content);
        work.put("dateTime", dateTime);
        work.put("timeAlarm", timeAlarm);
        work.put("isReminder", isReminder);
        return work;
    }
}
