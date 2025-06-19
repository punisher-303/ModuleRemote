package com.hfad.veloxmechanics.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Mechanic implements Parcelable {

    private String name;
    private String email;
    private String specialty;
    private String user_id;
    private String phone_number;
    private String image = "default";
    private GeoPoint location = null;
    private boolean online = false;
    private @ServerTimestamp
    Date time_stamp;
    private double geoFireLocation = 0;

    public Mechanic() {

    }

    public Mechanic(String name, String email, String specialty, String user_id, String phone_number) {
        this.name = name;
        this.email = email;
        this.specialty = specialty;
        this.user_id = user_id;
        this.phone_number = phone_number;

    }

    public Mechanic(Parcel in) {
        name = in.readString();
        email = in.readString();
        specialty = in.readString();
        user_id = in.readString();
        phone_number = in.readString();
        image = in.readString();
        online = in.readByte() != 0;
        geoFireLocation = in.readDouble();
        Double lat = in.readDouble();
        Double lng = in.readDouble();
        if (lat != -1 && lng != -1)
            location = new GeoPoint(lat, lng);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(specialty);
        parcel.writeString(user_id);
        parcel.writeString(phone_number);
        parcel.writeString(image);
        parcel.writeByte((byte) (online ? 1 : 0));
        parcel.writeDouble(geoFireLocation);
        if (location != null) {
            parcel.writeDouble(location.getLatitude());
            parcel.writeDouble(location.getLongitude());
        }
        else{
            parcel.writeDouble(-1);
            parcel.writeDouble(-1);
        }
    }

    public static final Creator<Mechanic> CREATOR = new Creator<Mechanic>() {
        @Override
        public Mechanic createFromParcel(Parcel in) {
            return new Mechanic(in);
        }

        @Override
        public Mechanic[] newArray(int size) {
            return new Mechanic[size];
        }
    };

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Date getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }

    public double getGeoFireLocation() {
        return geoFireLocation;
    }

    public void setGeoFireLocation(double geoFireLocation) {
        this.geoFireLocation = geoFireLocation;
    }

    @Override
    public int describeContents() {
        return 0;
    }


}
