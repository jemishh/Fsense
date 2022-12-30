package com.example.fsense.activity;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.fsense.R;
import com.example.fsense.fragments.LoginFragment;
import com.example.fsense.fragments.RegisterFragment;
import com.example.fsense.utils.FirebaseMethods;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {
    TextView tv_fsense;
    MaterialCardView cv_background;
    int height,width;
    FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;
    Boolean loggedOut= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();


    }

    private void init() {
        fAuth = FirebaseAuth.getInstance();
        //initialize
        cv_background = findViewById(R.id.cv_background);
        tv_fsense = findViewById(R.id.tv_fsense);
        //get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        //get data
        Intent intent = getIntent();
        loggedOut = intent.getBooleanExtra("logged_out", false);

        if (loggedOut) {
            tv_fsense.animate().translationY((float) -(height/(2.5))).setDuration(0).setStartDelay(0);
            cv_background.animate().translationY((float) (-height/(1.3))).setDuration(0).setStartDelay(0);
            func2();
        } else {
            if (fAuth.getCurrentUser() != null) {
                new CountDownTimer(2000, 5000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        tv_fsense.animate().translationY((float) -(height/(2.5))).setDuration(2000).setStartDelay(2000);
                        cv_background.animate().translationY((float) (-height/(1.3))).setDuration(2000).setStartDelay(2000);
                    }

                    @Override
                    public void onFinish() {
                        startMainActivity();
                    }
                }.start();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //firebase
        if(fAuth.getCurrentUser()==null && !loggedOut) {
            func1();
        }

       
    }
    private void startMainActivity(){
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition( R.anim.enter_from_bottom, R.anim.exit_to_top );
        finish();
    }

    private void func1() {
        new CountDownTimer(2000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_fsense.animate().translationY((float) -(height/(2.5))).setDuration(2000).setStartDelay(2000);
                cv_background.animate().translationY((float) (-height/(1.3))).setDuration(2000).setStartDelay(2000);
            }

            @Override
            public void onFinish() {
                func2();
            }
        }.start();
    }
    private void func2() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom);
        transaction.replace(R.id.fragment_container, new LoginFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*------------------Handle Back press-------------*/
    @Override
    public void onBackPressed() {
        if(RegisterFragment.backKeyPressedListener!=null) {
            RegisterFragment.backKeyPressedListener.backPressed();
        }else if(LoginFragment.backKeyPressedListener!=null) {
            LoginFragment.backKeyPressedListener.backPressed();
        }else {
            super.onBackPressed();
            finish();
        }
    }


    /*
  --------------------------------------Firebase---------------------------------

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuthMain: Setting up firebase auth.");
        fAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();
        fAuthListener = firebaseAuth -> {
            FirebaseUser user = fAuth.getCurrentUser();
            if (user != null) {
                //user is signed in
                Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
            } else {
                //user is signed out
                Log.d(TAG, "onAuthStateChanged: signed_out");
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        if(fAuth.getCurrentUser()==null && !loggedOut) {
            func1();
        }
        fAuth.addAuthStateListener(fAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fAuthListener != null) {
            fAuth.removeAuthStateListener(fAuthListener);
        }
    }
    */
}
