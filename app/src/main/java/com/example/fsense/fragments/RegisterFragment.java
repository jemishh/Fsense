package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.UserModel;
import com.example.fsense.utils.FirebaseMethods;
import com.example.fsense.utils.StringManipulation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Random;

public class RegisterFragment extends Fragment implements BackKeyPressedListener, View.OnClickListener {

    View view;
    TextInputLayout tilEmail, tilPassword, tilContact, tilName, tilConfirmPass;
    TextInputEditText tetEmail, tetPassword, tetContact, tetName, tetConfirmPass;
    Button btnSignUp;
    TextView tvToLogIn;
    ConstraintLayout bg_signup;
    ProgressBar progressBar;

    //variables
    int height, width;
    Animation animation;
    public static BackKeyPressedListener backKeyPressedListener;
    String email, password, name, contact, confirmPass;
    int flag = 0;
    String username,displayName,append="";

    //firebase
    FirebaseAuth fAuth;
    FirebaseMethods firebaseMethods;
    Context context;
    FirebaseAuth.AuthStateListener fAuthListener;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_register, container, false);

        context=getContext();
        firebaseMethods=new FirebaseMethods(context);

        init();
        setListener();
        setupFirebaseAuth();



        return view;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        tilEmail = view.findViewById(R.id.tilEmail);
        tilName = view.findViewById(R.id.tilName);
        tilContact = view.findViewById(R.id.tilContact);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPass = view.findViewById(R.id.tilConfirmPass);
        tetEmail = view.findViewById(R.id.tetEmail);
        tetName = view.findViewById(R.id.tetName);
        tetContact = view.findViewById(R.id.tetContact);
        tetPassword = view.findViewById(R.id.tetPassword);
        tetConfirmPass = view.findViewById(R.id.tetConfirmPass);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        tvToLogIn = view.findViewById(R.id.tvToLogIn);
        bg_signup = view.findViewById(R.id.bg_signup);
        progressBar=view.findViewById(R.id.progressBar);

        //bounce animation
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        //close the keyboard
        bg_signup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                closeKeyboard();
                return true;
            }
        });

        //firebase
        fAuth=FirebaseAuth.getInstance();

    }

    private void setListener() {
        btnSignUp.setOnClickListener(this);
        tvToLogIn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignUp:
                int f = validation();
                if (f == 6) {
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewUser(email,password);
                }
                break;
            case R.id.tvToLogIn:
                tvToLogIn.startAnimation(animation);
                func1(false);
                break;
        }
    }

    private int validation() {
        email = tetEmail.getText().toString().trim();
        contact = tetContact.getText().toString().trim();
        name = tetName.getText().toString().trim();
        password = tetPassword.getText().toString();
        confirmPass = tetConfirmPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter Email");
        } else {
            tilEmail.setError(null);
            flag = 1;
        }
        if (TextUtils.isEmpty(name) && flag == 1) {
            tilName.setError("Please Enter Password");
        } else if (!TextUtils.isEmpty(name) && flag == 1) {
            tilName.setError(null);
            flag = 2;
        }
        if (TextUtils.isEmpty(contact) && flag == 2) {
            tilContact.setError("Please Enter Password");
        } else if (!TextUtils.isEmpty(contact) && flag == 2) {
            tilContact.setError(null);
            flag = 3;
        }
        if (TextUtils.isEmpty(password) && flag == 3) {
            tilPassword.setError("Please Enter Password");
        } else if (!TextUtils.isEmpty(password) && flag == 3) {
            tilPassword.setError(null);
            flag = 4;
        }
        if (TextUtils.isEmpty(confirmPass) && flag == 4) {
            tilConfirmPass.setError("Please Enter Password");
        } else if (!TextUtils.isEmpty(password) && flag == 4) {
            tilConfirmPass.setError(null);
            flag = 5;
        }
        if (!confirmPass.equals(password) && flag == 5) {
            tilConfirmPass.setError("Password didn't match");
        } else if (confirmPass.equals(password) && flag == 5) {
            tilConfirmPass.setError(null);
            flag = 6;
        }
        return flag;
    }


    /*-----------------------------------Handle Back Press------------------------*/
    @Override
    public void onPause() {
        backKeyPressedListener = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        backKeyPressedListener = this;
    }

    @Override
    public void backPressed() {
        func1(true);
    }


    /*---------------------change the fragments with animation-------------------*/
    public void func1(Boolean backPressed) {
        new CountDownTimer(0, 5000) {
            //millisInFuture- more the value- longer it will take to start animation
            @Override
            public void onTick(long millisUntilFinished) {
                bg_signup.animate().translationY(height).setDuration(1000).setStartDelay(0);
            }

            @Override
            public void onFinish() {
                func2(backPressed);
            }
        }.start();
    }

    public void func2(Boolean backPressed) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if(!backPressed) {
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top);
        }else{
            transaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_bottom);
        }
        transaction.replace(R.id.fragment_container, new LoginFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /*------------------------------------Firebase--------------------------------------------------*/
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: Setting up firebase auth.");
        fAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        dbRef=firebaseDatabase.getReference();
        fAuthListener= firebaseAuth -> {
            FirebaseUser user=fAuth.getCurrentUser();
            if(user!=null){
                //user is signed in
                Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        username= StringManipulation.condenseUsername(tetName.getText().toString().trim());
                        username=username.toLowerCase();
                        checkIfUsernameExists(username);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                func1(true);  //back to login
            }else {
                //user is signed out
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fAuthListener);
        Log.d(TAG, "onStart: start");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(fAuth!=null){
            fAuth.removeAuthStateListener(fAuthListener);
            Log.d(TAG, "onStop: stop");
        }
    }

    private void checkIfUsernameExists(final String username){
        Log.d(TAG, "checkIfUsernameExists: checking if "+username+" already exists");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query=reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot singleSnapshot:snapshot.getChildren()){
                    Log.d(TAG, "onDataChange: data snapshot"+ snapshot);
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: FOUND A MATCH: "+singleSnapshot.getValue(UserModel.class).getUsername());
                        //append=dbRef.push().getKey().substring(3,10);
                        append="_"+getRandomString(4,email+contact+username);
                        Log.d(TAG, "onDataChange: "+append);
                    } else{
                        Log.d(TAG, "onDataChange: no match found");
                    }
                }
                String fUsername="";
                fUsername=username+append;
                displayName=tetName.getText().toString().trim();
                //add new User to database
                firebaseMethods.addNewUser(email,fUsername,displayName,"","","",Long.parseLong(contact));
                progressBar.setVisibility(View.GONE);
                Toast.makeText(context, "Signup successful,Sending verification email", Toast.LENGTH_SHORT).show();
                fAuth.signOut();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



    /*-----------------------Other Methods-----------------------------------------*/
    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private static String getRandomString(final int sizeOfRandomString,String allowedCharacters)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(allowedCharacters.charAt(random.nextInt(allowedCharacters.length())));
        return sb.toString();
    }


}
