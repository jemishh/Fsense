package com.example.fsense.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import static com.example.fsense.utils.FsenseContext.getApplicationContext;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import com.example.fsense.utils.FirebaseMethods;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment implements BackKeyPressedListener, View.OnClickListener {

    View view;
    Context context;
    TextInputLayout tilEmail, tilPassword;
    TextInputEditText tetEmail, tetPassword;
    Button btnLogin;
    TextView tvToSignUp;
    ConstraintLayout bg_login;

    //variables
    int height, width;
    Animation animation;
    public static BackKeyPressedListener backKeyPressedListener;
    String email, password;
    int flag = 0;

    //firebase
    FirebaseAuth fAuth;
    ProgressBar progressBar;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        view = inflater.inflate(R.layout.fragment_login, container, false);

        context=getContext();

        init();
        setListener();

        return view;
    }


    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        //get Screen dimensions
       /* DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;*/

        //initialize
        bg_login = view.findViewById(R.id.bg_login);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tetEmail = view.findViewById(R.id.tetEmail);
        tetPassword = view.findViewById(R.id.tetPassword);
        btnLogin = view.findViewById(R.id.btnLogIn);
        tvToSignUp = view.findViewById(R.id.tvToSignUp);
        progressBar=view.findViewById(R.id.progressBar);

        //bounce animation
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        //close the keyboard
        bg_login.setOnTouchListener(new View.OnTouchListener() {
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
        btnLogin.setOnClickListener(this);
        tvToSignUp.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogIn:
                int f = validation();
                if (f == 2) {
                    progressBar.setVisibility(View.VISIBLE);
                    closeKeyboard();
                    fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        FirebaseUser user = fAuth.getCurrentUser();
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {

                            try {
                                if (user.isEmailVerified()) {
                                    Intent intent=new Intent(getActivity(),MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    Toast.makeText(getActivity(), "welcome", Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(context, "Email is not verified \n Check your mail inbox", Toast.LENGTH_SHORT).show();
                                    user.sendEmailVerification();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    fAuth.signOut();
                                }
                            } catch (NullPointerException e) {
                                Log.e(TAG, "onCreate: NullPointerException" + e.getMessage());
                            }
                        }
                    });
                }
                break;
            case R.id.tvToSignUp:
                tvToSignUp.startAnimation(animation);
                func1();
                break;
        }
    }

    private int validation() {
        email = tetEmail.getText().toString();
        password = tetPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Please enter Email");
        } else {
            tilEmail.setError(null);
            flag = 1;
        }
        if (TextUtils.isEmpty(password) && flag == 1) {
            tilPassword.setError("Please Enter Password");
        } else if (!TextUtils.isEmpty(password) && flag == 1) {
            tilPassword.setError(null);
            flag = 2;
        }
        return flag;
    }

    /*--------------------------------------Handle Back Press--------------------------*/
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
        getActivity().finish();
    }


    /*---------------------change the fragments with animation-------------------*/
    public void func1() {
        new CountDownTimer(0, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                bg_login.animate().translationY(-height).setDuration(1000).setStartDelay(0);
            }

            @Override
            public void onFinish() {
                func2();
            }
        }.start();
    }

    public void func2() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top);
        transaction.replace(R.id.fragment_container, new RegisterFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /*----------------------------------------Other Methods-----------------------------------*/
    private void closeKeyboard(){
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
