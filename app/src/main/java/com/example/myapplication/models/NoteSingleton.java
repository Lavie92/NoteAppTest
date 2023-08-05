package com.example.myapplication.models;

import java.util.Date;

public class NoteSingleton {
    private static NoteSingleton instance;
    private Note note;

    private NoteSingleton() {
        note = new Note();
    }

    public static synchronized NoteSingleton getInstance() {
        if (instance == null) {
            instance = new NoteSingleton();
        }
        return instance;
    }

    public Note getNote() {
        return note;
    }

    public void setNoteContent(String content) {
        note.setContent(content);
    }

    public void setNoteDateTime(Date date) {
        note.setDateTime(date);
    }
}
