package com.example.fsense.models;

public class LikesModel {

    private String user_id;

    public LikesModel() {
    }

    public LikesModel(String user_id) {
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
        return "LikesModel{" +
                "user_id='" + user_id + '\'' +
                '}';
    }
}
