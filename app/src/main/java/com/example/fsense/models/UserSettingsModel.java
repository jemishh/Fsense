package com.example.fsense.models;

import java.io.Serializable;

public class UserSettingsModel {
    private UserModel userModel;
    private UserInfoModel userInfoModel;

    public UserSettingsModel(UserModel userModel, UserInfoModel userInfoModel) {
        this.userModel = userModel;
        this.userInfoModel = userInfoModel;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public UserInfoModel getUserInfoModel() {
        return userInfoModel;
    }

    public void setUserInfoModel(UserInfoModel userInfoModel) {
        this.userInfoModel = userInfoModel;
    }

    @Override
    public String toString() {
        return "UserSettingsModel{" +
                "userModel=" + userModel +
                ", userInfoModel=" + userInfoModel +
                '}';
    }
}
