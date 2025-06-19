package com.hfad.veloxmechanics.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatUser implements Parcelable {

    private String name;
    private String user_id;
    private String image="default";

    public ChatUser(){

    }

    public ChatUser(String name, String user_id, String image) {
        this.name = name;
        this.user_id = user_id;
        this.image = image;
    }

    protected ChatUser(Parcel in) {
        name = in.readString();
        user_id = in.readString();
        image = in.readString();
    }

    public static final Creator<ChatUser> CREATOR = new Creator<ChatUser>() {
        @Override
        public ChatUser createFromParcel(Parcel in) {
            return new ChatUser(in);
        }

        @Override
        public ChatUser[] newArray(int size) {
            return new ChatUser[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(user_id);
        parcel.writeString(image);
    }
}
