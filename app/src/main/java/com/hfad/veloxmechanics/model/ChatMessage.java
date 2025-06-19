package com.hfad.veloxmechanics.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChatMessage {

    private String message_id;
    private String message;
    private int message_type = -1;
    private @ServerTimestamp
    Date time_stamp;
    private String from;

    public ChatMessage() {
    }

    public ChatMessage(String message_id, String message, int message_type, Date time_stamp, String from) {
        this.message_id = message_id;
        this.message = message;
        this.message_type = message_type;
        this.time_stamp = time_stamp;
        this.from = from;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessage_type() {
        return message_type;
    }

    public void setMessage_type(int message_type) {
        this.message_type = message_type;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
