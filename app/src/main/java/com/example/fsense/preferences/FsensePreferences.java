package com.example.fsense.preferences;

import android.content.SharedPreferences;

import com.example.fsense.models.UserSettingsModel;
import com.example.fsense.utils.FsenseContext;
import com.google.gson.Gson;

public class FsensePreferences {
    private static SharedPreferences pref_fsense;
    SharedPreferences.Editor editor_pref_fsense;

    public static FsensePreferences fsensePreferences = new FsensePreferences();

    public FsensePreferences() {
        pref_fsense = FsenseContext.getApplicationContext.getSharedPreferences("Fsense_Preferences", 0);
    }

    public static FsensePreferences getInstance() {
        if (fsensePreferences == null) {
            fsensePreferences = new FsensePreferences();
        }
        return fsensePreferences;
    }

    public void open_editor() {
        editor_pref_fsense = pref_fsense.edit();
    }

    public String get_userModel() {
        // TODO Auto-generated method stub
        return pref_fsense.getString("userModel", "");
    }

    public void set_userModel(String userModel) {
        // TODO Auto-generated method stub
        open_editor();
        editor_pref_fsense.putString("userModel", userModel);
        editor_pref_fsense.commit();
    }

    public String get_userInfoModel() {
        // TODO Auto-generated method stub
        return pref_fsense.getString("userInfoModel", "");
    }

    public void set_userInfoModel(String userInfoModel) {
        // TODO Auto-generated method stub
        open_editor();
        editor_pref_fsense.putString("userInfoModel", userInfoModel);
        editor_pref_fsense.commit();
    }

    public void clearPrefData() {
        open_editor();
        editor_pref_fsense.putString("userModel", "");
        editor_pref_fsense.putString("userInfoModel", "");
        editor_pref_fsense.commit();
    }
}






/*
-------------------------------- how to add the data -------------------------
    //Set the values
    Gson gson = new Gson();
    String jsonText = gson.toJson(userSettings.getUserModel());
        fsensePreferences.set_userModel(jsonText);
                //Log.d(TAG, "setProfileWidgets: "+fsensePreferences.get_userModel());

                //Retrieve the values

                String json = fsensePreferences.get_userModel();
                UserModel text = gson.fromJson(json, UserModel.class);
        Log.d(TAG, "setProfileWidgets: "+text);  */
