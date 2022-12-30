package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.bottomSheet.GalleryBottomSheet;
import com.example.fsense.events.UploadEvent;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserModel;
import com.example.fsense.models.UserSettingsModel;
import com.example.fsense.utils.FirebaseMethods;
import com.example.fsense.utils.GlideImageLoader;
import com.example.fsense.utils.Permissions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements BackKeyPressedListener, View.OnClickListener, View.OnTouchListener {

    View view;
    Context context;
    public static BackKeyPressedListener backKeyPressedListener;

    //initialize
    ImageView img_backArrow;
    NestedScrollView nestedScroll;
    EditText et_username, et_fullName, et_website, et_description, et_email, et_contact;
    CardView cv_editUserPhoto;
    MaterialCardView cv_doneEdit;
    CircleImageView cimg_editProfile;
    ProgressBar progressBar;
    Dialog confirmPassDialog;
    TextView tv_confirmPass, tv_cancelPass;
    EditText et_enterConfirmPass;
    GalleryBottomSheet galleryBottomSheet;


    //variables
    UserSettingsModel userSettingsModel;
    String confirmPassword;
    Animation animation;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;

    //firebase
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;
    private FirebaseMethods firebaseMethods;
    ActivityResultLauncher<String[]> requestPermissionLauncher;

    //progress
    public ProgressBar progressProfileUpload;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        context = getContext();

        init();
        setupFirebaseAuth();
        setListener();

        return view;
    }

    private void init() {

        img_backArrow = view.findViewById(R.id.img_backArrow);
        cv_doneEdit = view.findViewById(R.id.cv_doneEdit);
        cv_editUserPhoto = view.findViewById(R.id.cv_editUserPhoto);
        et_username = view.findViewById(R.id.et_username);
        et_fullName = view.findViewById(R.id.et_fullName);
        et_website = view.findViewById(R.id.et_website);
        et_description = view.findViewById(R.id.et_description);
        et_email = view.findViewById(R.id.et_email);
        et_contact = view.findViewById(R.id.et_contact);
        cimg_editProfile = view.findViewById(R.id.cimg_editProfile);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        nestedScroll= view.findViewById(R.id.nestedScroll);

        //progressBar for profile photo upload
        progressProfileUpload=view.findViewById(R.id.progressProfileUpload);
        progressProfileUpload.setVisibility(View.GONE);


        //firebase
        firebaseMethods = new FirebaseMethods(context);

        //bounce animation
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {
                        //Log.d(TAG, "onActivityResult: " + isGranted);
                        /*String key;
                        Boolean value;
                        int count = 0;
                        for (Map.Entry<String, Boolean> entry : isGranted.entrySet()) {
                            key = entry.getKey();
                            value = entry.getValue();
                            if (!value) {
                                Toast.makeText(context, "Please allow " + key + " from settings", Toast.LENGTH_SHORT).show();
                                String[] permission = {key.replace("android", "Manifest")};
                                verifyPermission(permission);
                                break;
                            } else {
                                count++;
                            }
                        }
                        if (count == isGranted.size()) {*/

                        if (galleryBottomSheet != null) {
                            if (galleryBottomSheet.isVisible()) {
                                galleryBottomSheet.dismiss();
                                galleryBottomSheet = new GalleryBottomSheet(context,isGranted,getResources().getString(R.string.edit_profile_fragment));
                                galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                            } else {
                                galleryBottomSheet = new GalleryBottomSheet(context,isGranted,getResources().getString(R.string.edit_profile_fragment));
                                galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                            }
                        } else {
                            galleryBottomSheet = new GalleryBottomSheet(context,isGranted,getResources().getString(R.string.edit_profile_fragment));
                            galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                        }
                        //open bottom sheet and pass the permission map


                    }
                });
    }


    /*-------------------------------Handle On Click----------------------------------------- */
    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        img_backArrow.setOnClickListener(this);
        cv_doneEdit.setOnClickListener(this);
        nestedScroll.setOnTouchListener(this);
        cv_editUserPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_backArrow:
                backPressed();
                break;
            case R.id.cv_doneEdit:
                closeKeyboard();
                saveProfile();
                //backPressed();
                break;
            case R.id.cv_editUserPhoto:
                //access to permissions and launch bottom sheet
                requestPermissionLauncher.launch(Permissions.PERMISSIONS);
                break;
        }
    }

    /*------------------------------------- save profile ----------------------------------------*/

    private void saveProfile() {
        final String username = et_username.getText().toString().trim();
        final String displayName = et_fullName.getText().toString().trim();
        final String website = et_website.getText().toString().trim();
        final String description = et_description.getText().toString().trim();
        final String email = et_email.getText().toString().trim();
        final String c = et_contact.getText().toString().trim();
        final long contact = Long.parseLong(c);


        //case1: the user changed their username
        if (!userSettingsModel.getUserModel().getUsername().equals(username)) {
            checkIfUsernameExists(username);
        }

        //case2:the user changed their email
        if (!userSettingsModel.getUserModel().getEmail().equals(email)) {
            //creating confirm password dialog
            confirmPassDialog = new Dialog(context);
            confirmPassDialog.setContentView(R.layout.layout_confirm_password_dialog);
            confirmPassDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //initializing dialog elements
            CardView cv_confirmPassDialog = confirmPassDialog.findViewById(R.id.cv_confirmPassDialog);
            tv_confirmPass = confirmPassDialog.findViewById(R.id.tv_confirmPass);
            tv_cancelPass = confirmPassDialog.findViewById(R.id.tv_cancelPass);
            et_enterConfirmPass = confirmPassDialog.findViewById(R.id.et_enterConfirmPass);
            cv_confirmPassDialog.setAnimation(animation);
            cv_confirmPassDialog.startAnimation(animation);
            //cancel the dialog
            tv_cancelPass.setOnClickListener(view -> {
                tv_cancelPass.startAnimation(animation);
                confirmPassDialog.dismiss();
            });

            //confirm password
            tv_confirmPass.setOnClickListener(view -> {
                tv_confirmPass.startAnimation(animation);
                confirmPassword = et_enterConfirmPass.getText().toString().trim();
                if (confirmPassword.isEmpty()) {
                    Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show();
                } else {
                    updateEmail(confirmPassword);
                    confirmPassDialog.dismiss();
                }
            });
            confirmPassDialog.show();

        }

        /*
        change the rest of the settings that do not required uniqueness
        */
        int count =0;
        if (!userSettingsModel.getUserInfoModel().getDisplay_name().equals(displayName)) {
            //update displayName
            firebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
            count++;
        }
        if (!userSettingsModel.getUserInfoModel().getWebsite().equals(website)) {
            //update description
            firebaseMethods.updateUserAccountSettings(null, website, null, 0);
            count++;
        }
        if (!userSettingsModel.getUserInfoModel().getDescription().equals(description)) {
            //update description
            firebaseMethods.updateUserAccountSettings(null, null, description, 0);
            count++;
        }
        if (!userSettingsModel.getUserInfoModel().getProfile_photo().equals(contact)) {
            //update displayname
            firebaseMethods.updateUserAccountSettings(null, null, null, contact);
            count++;
        }
        if(count>0) {
            Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show();
        }
        
    }

    private void checkIfUsernameExists(final String username) {
        Log.d(TAG, "checkIfUsernameExists: checking if " + username + " already exists");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    firebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "username saved", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    if (singleSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: FOUND A MATCH: " + singleSnapshot.getValue(UserModel.class).getUsername());
                        Toast.makeText(getActivity(), "username already exists", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateEmail(String password) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(fAuth.getCurrentUser().getEmail(), password);
        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User re-authenticated.");

                            //check if the email already exists
                            fAuth.fetchSignInMethodsForEmail(et_email.getText().toString().trim()).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    SignInMethodQueryResult result = task1.getResult();
                                    List<String> signInMethods = result.getSignInMethods();
                                    try {
                                        if (signInMethods.size() == 1) {
                                            // User can sign in with email/password
                                            Toast.makeText(context, "That email is already in use.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            //email is available...update it
                                            fAuth.getCurrentUser().updateEmail(et_email.getText().toString())
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            Log.d(TAG, "User email address updated.");
                                                            Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show();
                                                            firebaseMethods.updateEmail(et_email.getText().toString());
                                                        }
                                                    });
                                        }
                                    } catch (NullPointerException e) {
                                        Log.e(TAG, "onComplete: NullPointerException" + e.getMessage());
                                    }

                                } else {
                                    Log.e(TAG, "Error getting sign in methods for user", task.getException());
                                }
                            });

                        } else {
                            Log.d(TAG, "onComplete: re-authentication failed");
                        }

                    }
                });
    }

    /*---------------------------------------Firebase---------------------------------------*/
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuthEditProfile: Setting up firebase auth.");
        fAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();
        fAuthListener = firebaseAuth -> {
            FirebaseUser user = fAuth.getCurrentUser();
            if (user != null) {
                //user is signed in
                //Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
            } else {
                //user is signed out
                //Log.d(TAG, "onAuthStateChanged: signed_out");
            }
        };

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //retrieve user info from the database
                setProfileWidgets(firebaseMethods.getUserSettings(snapshot));
                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //set data from firebase
    private void setProfileWidgets(UserSettingsModel userSettings) {
        //Set the values
        userSettingsModel = userSettings;
        UserInfoModel userInfoModel = userSettings.getUserInfoModel();

        GlideImageLoader.setImage(userInfoModel.getProfile_photo(), cimg_editProfile, null,"");
        et_username.setText(userInfoModel.getUsername());
        et_fullName.setText(userInfoModel.getDisplay_name());
        et_website.setText(userInfoModel.getWebsite());
        et_description.setText(userInfoModel.getDescription());
        et_email.setText(userSettings.getUserModel().getEmail());
        et_contact.setText(String.valueOf(userSettings.getUserModel().getContact()));
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fAuthListener);
        if (!EventBus.getDefault().isRegistered(this)) //subscribe to the event
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fAuthListener != null) {
            fAuth.removeAuthStateListener(fAuthListener);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this); //remove event
    }


    /*------------------------------------Handle Back press---------------------------------------*/
    @Override
    public void backPressed() {
        ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
        ((MainActivity) getActivity()).bottomNavigation.show(3, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        backKeyPressedListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        backKeyPressedListener = this;
    }


    /*-----------------------Other Methods-----------------------------------------*/
    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.nestedScroll) {
            Log.d(TAG, "onTouch: Touched");
            closeKeyboard();
        }
        return true;
    }



    //update the progressbar while uploading photo
    @Subscribe
    public void onUploadProgress(UploadEvent uploadEvent){
        Log.d(TAG, "onUploadProgress: "+uploadEvent.progress);
        if(uploadEvent.progress>=0) {

            if(uploadEvent.progress==101){
                progressProfileUpload.setVisibility(View.GONE);
            } else{

                progressProfileUpload.setVisibility(View.VISIBLE);
                progressProfileUpload.setProgress(uploadEvent.progress);
            }
        }
    }
}
