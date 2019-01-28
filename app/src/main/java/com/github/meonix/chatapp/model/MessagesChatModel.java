package com.github.meonix.chatapp.model;

public class MessagesChatModel {
    private String date, message, time,from_uid,type;

    public MessagesChatModel(String date, String from_uid, String message, String time, String type) {
        this.date = date;
        this.message = message;
        this.time = time;
        this.from_uid= from_uid;
        this.type = type;
    }
    public MessagesChatModel(){}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getFrom_uid() {
        return from_uid;
    }

    public void setFrom_uid(String from_uid) {
        this.from_uid = from_uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
