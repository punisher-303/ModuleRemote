package com.hfad.veloxmechanics.model;


import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

// This class is for showing all the Active Chats a user have
// Whether the user has seen the message of a certain chat or not

public class ActiveChat {
    private String user_id;
    private String user_name;
    private String profile_pic;
    private String last_message;
    private int last_message_type;
    private boolean seen = false;
    private @ServerTimestamp
    Date time_stamp;

    public ActiveChat() {
    }

    public ActiveChat(String user_id, String user_name, String profile_pic, String last_message, int last_message_type, boolean seen, Date time_stamp) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.profile_pic = profile_pic;
        this.last_message = last_message;
        this.last_message_type = last_message_type;
        this.seen = seen;
        this.time_stamp = time_stamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public int getLast_message_type() {
        return last_message_type;
    }

    public void setLast_message_type(int last_message_type) {
        this.last_message_type = last_message_type;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }
}

