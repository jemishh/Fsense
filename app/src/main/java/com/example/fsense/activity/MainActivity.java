package com.example.fsense.activity;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.fsense.R;
import com.example.fsense.fragments.EditProfileFragment;
import com.example.fsense.fragments.HomeFragment;
import com.example.fsense.fragments.ProfileFragment;
import com.example.fsense.fragments.ProfilePostsFragment;
import com.example.fsense.fragments.SearchFragment;
import com.example.fsense.fragments.SearchedProfileFragment;
import com.example.fsense.preferences.FsensePreferences;
import com.example.fsense.utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public MeowBottomNavigation bottomNavigation;

    private final Context context = MainActivity.this;

    //Shared preferences
    FsensePreferences fsensePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
// In Activity's onCreate() for instance
        init();
        setData();

    }

    private void init() {
        Log.d(TAG, "init: mainActivity");
        //initialize
        bottomNavigation = findViewById(R.id.bottomNavigation);
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), SplashScreen.class));
            finish();
        }

        //shared preferences
            //fsensePreferences = new FsensePreferences();
    }

    private void setData() {
        //add menu item to bottom navigation
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_search));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_profile));

        bottomNavigation.setOnShowListener(item -> {
            //initialize fragment
            Fragment fragment = null;
            switch (item.getId()) {
                case 1:
                    fragment = new HomeFragment();
                    break;
                case 2:
                    fragment = new SearchFragment();
                    break;
                case 3:
                    fragment = new ProfileFragment();
                    break;
            }
            //load fragment
            loadFragment(fragment);
        });

        bottomNavigation.show(1, true);
        bottomNavigation.setOnClickMenuListener(item -> {

        });

        bottomNavigation.setOnReselectListener(item -> {
            //initialize fragment
            Fragment fragment = null;
            switch (item.getId()) {
                case 1:
                    fragment = new HomeFragment();
                    break;
                case 2:
                    fragment = new SearchFragment();
                    break;
                case 3:
                    fragment = new ProfileFragment();
                    break;
            }
            //load fragment
            loadFragment(fragment);
        });
    }


    public void loadFragment(Fragment fragment) {
        //replace fragment
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out)
                .replace(R.id.fl_mainContainer, fragment)
                .commit();
    }


    /*----------------------------------------Handle Back press------------------------------------*/
    @Override
    public void onBackPressed() {
        if (ProfileFragment.backKeyPressedListener != null) {
            ProfileFragment.backKeyPressedListener.backPressed();
        } else if (SearchFragment.backKeyPressedListener != null) {
            SearchFragment.backKeyPressedListener.backPressed();
        } else if (EditProfileFragment.backKeyPressedListener != null) {
            EditProfileFragment.backKeyPressedListener.backPressed();
        } else if (ProfilePostsFragment.backKeyPressedListener != null) {
            ProfilePostsFragment.backKeyPressedListener.backPressed();
        } else if (SearchedProfileFragment.backKeyPressedListener != null) {
            SearchedProfileFragment.backKeyPressedListener.backPressed();
        } else {
            super.onBackPressed();
            finish();
        }
    }
}