package com.github.meonix.chatapp.model;

public class MessageModel {
    private String date, message, name, time,uid;

    public MessageModel(String date, String message, String name, String time,String uid) {
        this.date = date;
        this.message = message;
        this.name = name;
        this.time = time;
        this.uid = uid;

    }
    public MessageModel(){
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
