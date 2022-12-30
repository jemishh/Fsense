package com.example.fsense.utils;

import android.app.Application;
import android.content.Context;

public class FsenseContext extends Application {
    public static Context getApplicationContext;
    //make sure to add name property in Manifest file

    @Override
    public void onCreate() {
        super.onCreate();
        getApplicationContext = this;
    }
}
