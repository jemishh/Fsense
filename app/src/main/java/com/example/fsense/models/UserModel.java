package com.example.fsense.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {
    private String user_id,username,email;
    private long contact;

    public UserModel() {
    }

    public UserModel(String user_id, String username, String email, long contact) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.contact = contact;
    }

    protected UserModel(Parcel in) {
        user_id = in.readString();
        username = in.readString();
        email = in.readString();
        contact = in.readLong();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getContact() {
        return contact;
    }

    public void setContact(long contact) {
        this.contact = contact;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "userId='" + user_id + '\'' +
                ", userName='" + username + '\'' +
                ", email='" + email + '\'' +
                ", contact=" + contact +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(user_id);
        parcel.writeString(username);
        parcel.writeString(email);
        parcel.writeLong(contact);
    }
}
