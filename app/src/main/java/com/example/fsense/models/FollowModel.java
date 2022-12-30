package com.example.fsense.models;

public class FollowModel {
    String user_id;

    public FollowModel() {
    }

    public FollowModel(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "FollowModel{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}

