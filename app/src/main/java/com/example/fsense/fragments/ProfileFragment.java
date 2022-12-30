package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import static com.example.fsense.utils.FsenseContext.getApplicationContext;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.activity.SplashScreen;
import com.example.fsense.adapters.GridRVAdapter;
import com.example.fsense.bottomSheet.GalleryBottomSheet;
import com.example.fsense.events.UploadEvent;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.LikesModel;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserSettingsModel;
import com.example.fsense.preferences.FsensePreferences;
import com.example.fsense.utils.FirebaseMethods;
import com.example.fsense.utils.GlideImageLoader;
import com.example.fsense.utils.Permissions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements BackKeyPressedListener, AppBarLayout.OnOffsetChangedListener, View.OnClickListener {

    View view;
    Context context;
    public static BackKeyPressedListener backKeyPressedListener;
    private FrameLayout fl_profileTitle;
    private CircleImageView cimg_profile;
    private LinearLayout ll_profileTitle;
    private AppBarLayout appBarProfile;
    private Toolbar main_toolbar;
    ProgressBar progressBar;

    //Views
    private TextView tv_usernameTop, tv_username, tv_displayName, tv_description, tvPostNo, tvFollowerNo, tvFollowingNo;
    RecyclerView rv_gridProfile;
    private MaterialCardView cv_addPhoto;
    private MaterialCardView cv_menu;
    NestedScrollView nestedScrollProfile;
    GridRVAdapter gridRVAdapter;

    //firebase
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;
    private FirebaseMethods firebaseMethods;

    //alert dialog
    MaterialAlertDialogBuilder builder;

    //variables
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.6f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    FsensePreferences fsensePreferences;
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    private final String mAppend = "file://";
    String username, cimgUrl="";
    int followers, following, posts = 0;


    //bottom sheet gallery
    ActivityResultLauncher<String[]> requestPermissionLauncher;
    GalleryBottomSheet galleryBottomSheet;
    ProgressBar progressNewUpload;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        context = getActivity();
        init();
        appBarProfile.addOnOffsetChangedListener(this);

        startAlphaAnimation(tv_usernameTop, 0, View.INVISIBLE);
        setupFirebaseAuth();
        setListener();
        closeKeyboard();
        getPhotos();


        return view;
    }

    private void init() {
        //initialize
        main_toolbar = view.findViewById(R.id.main_toolbar);
        main_toolbar.bringToFront();
        ll_profileTitle = view.findViewById(R.id.ll_profileTitle);
        appBarProfile = view.findViewById(R.id.appBarProfile);
        fl_profileTitle = view.findViewById(R.id.fl_profileTitle);
        tv_usernameTop = view.findViewById(R.id.tv_usernameTop);
        tv_username = view.findViewById(R.id.tv_username);
        tv_displayName = view.findViewById(R.id.tv_displayName);
        tv_description = view.findViewById(R.id.tv_description);
        //tvPostNo = view.findViewById(R.id.tvPostNo);
        tvFollowerNo = view.findViewById(R.id.tvFollowerNo);
        tvFollowingNo = view.findViewById(R.id.tvFollowingNo);
        cv_addPhoto = view.findViewById(R.id.cv_addPhoto);
        cv_menu = view.findViewById(R.id.cv_menu);
        cimg_profile = view.findViewById(R.id.cimg_profile);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        rv_gridProfile = view.findViewById(R.id.rv_gridProfile);
        //nestedScrollProfile = view.findViewById(R.id.nestedScrollProfile);
        progressNewUpload = view.findViewById(R.id.progressNewUpload);
        progressNewUpload.setVisibility(View.GONE);
        progressNewUpload.bringToFront();

        /*((AppCompatActivity)getActivity()).setSupportActionBar(main_toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Disable toolbar title
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);*/


        //firebase
        firebaseMethods = new FirebaseMethods(context);

        //shared preferences
        fsensePreferences = new FsensePreferences();

        //gallery bottom sheet
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> isGranted) {

                        //open bottom sheet and pass the permission map
                        if (galleryBottomSheet != null) {
                            if (galleryBottomSheet.isVisible()) {
                                galleryBottomSheet.dismiss();
                                galleryBottomSheet = new GalleryBottomSheet(context, isGranted, getResources().getString(R.string.profile_fragment));
                                galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                            } else {
                                galleryBottomSheet = new GalleryBottomSheet(context, isGranted, getResources().getString(R.string.profile_fragment));
                                galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                            }
                        } else {
                            galleryBottomSheet = new GalleryBottomSheet(context, isGranted, getResources().getString(R.string.profile_fragment));
                            galleryBottomSheet.show(getActivity().getSupportFragmentManager(), galleryBottomSheet.getTag());
                        }
                    }
                });
    }


    /*-------------------------------Handle On Click----------------------------------------- */
    private void setListener() {
        cv_menu.setOnClickListener(this);
        cv_addPhoto.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /*--------------------------------- bottom sheet menu-------------------------------------*/
            case R.id.cv_menu:
                if (bottomSheetDialog != null) {
                    if (bottomSheetDialog.isShowing()) {
                        bottomSheetDialog.dismiss();
                        openMenu();
                    } else {
                        openMenu();
                    }
                } else {
                    openMenu();
                }

                break;
            case R.id.cv_addPhoto:
                //access to permissions and launch bottom sheet
                requestPermissionLauncher.launch(Permissions.PERMISSIONS);
                break;

        }
    }

    private void openMenu() {
        bottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        bottomSheetDialog.cancel();
        bottomSheetView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_bottom_menu, view.findViewById(R.id.ll_profileMenu), false);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        bottomSheetView.findViewById(R.id.cv_logOut).setOnClickListener(view1 -> {
            buildAlertDialog(bottomSheetDialog);
        });

        bottomSheetView.findViewById(R.id.cv_editProfile).setOnClickListener(view12 -> {
            //replace fragment
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_mainContainer, new EditProfileFragment()).commitAllowingStateLoss();

            bottomSheetDialog.dismiss();
        });
    }

    /*---------------------------------------Firebase---------------------------------*/
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuthProfile: Setting up firebase auth.");
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

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //setup the grid images
                //retrieve user info from the database
                setProfileWidgets(firebaseMethods.getUserSettings(snapshot));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setProfileWidgets(UserSettingsModel userSettings) {
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieved from firebase database"+userSettings.toString());

        //Set the values
        UserInfoModel userInfoModel = userSettings.getUserInfoModel();
        GlideImageLoader.setImage(userInfoModel.getProfile_photo(), cimg_profile, null, "");
        progressBar.setVisibility(View.GONE);

        tv_displayName.setText(userInfoModel.getDisplay_name());
        tv_username.setText(userInfoModel.getUsername());
        tv_usernameTop.setText(userInfoModel.getUsername());
        tv_description.setText(userInfoModel.getDescription());
        getFollowers();
        getFollowing();
        //need to send to ProfilePostsFragment
        username=userInfoModel.getUsername();
        cimgUrl=userInfoModel.getProfile_photo();

        /*tvPostNo.setText(String.valueOf(userInfoModel.getPosts()));
        tvFollowerNo.setText(String.valueOf(userInfoModel.getFollowers()));
        tvFollowingNo.setText(String.valueOf(userInfoModel.getFollowing()));*/
    }

    private void getFollowers(){
        followers = 0;
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef.child(context.getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()); //for user's profile
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               for(DataSnapshot singleSnapshot :snapshot.getChildren()) {
                    followers++;
               }
               tvFollowerNo.setText(String.valueOf(followers));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getFollowing(){
        following = 0;
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef.child(context.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()); //for user's profile
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot singleSnapshot :snapshot.getChildren()) {
                    following++;
                }
                tvFollowingNo.setText(String.valueOf(following));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

    /*---------------------------------Toolbar animation------------------------------------------*/
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //Log.d(TAG, "onOffsetChanged: "+verticalOffset);
        // Collapsed
        int padding_in_dp = 60;  // 6 dps
        final float scale = context.getResources().getDisplayMetrics().density;
        int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
        //Log.d(TAG, "onOffsetChanged: "+padding_in_px);
        //Log.d(TAG, "onOffsetChanged: --"+ (appBarLayout.getTotalScrollRange()-padding_in_px));

        if (Math.abs(verticalOffset) >= (appBarLayout.getTotalScrollRange() - padding_in_px)) {
            //calculating the padding for recycler view (or the nested scroll- which ever is below the appbar) when appbar is collapsed
            int padding = Math.abs((Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange())) - padding_in_px;
            rv_gridProfile.setPadding(0, Math.abs(padding), 0, 0);

        } else if (verticalOffset == 0) {
            // When Appbar is Expanded
            rv_gridProfile.setPadding(0, 0, 0, 0);
        } else {
            // When Appbar is Somewhere in between
            rv_gridProfile.setPadding(0, 0, 0, 0);
        }
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }


    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(tv_usernameTop, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(tv_usernameTop, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(ll_profileTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(ll_profileTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }


    /*--------------------------------------Alert Dialog------------------------------------------*/

    private void buildAlertDialog(BottomSheetDialog bottomSheetDialog) {
        builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog);
        builder.setMessage(R.string.log_out_popup)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fAuth.signOut();
                        bottomSheetDialog.cancel();
                        //fsensePreferences.clearPrefData();
                        Intent intent = new Intent(getActivity(), SplashScreen.class);
                        intent.putExtra("logged_out", true);
                        startActivity(intent);
                        getActivity().finish();

                    }
                })
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    //  Action for 'NO' Button
                    dialog.cancel();
                });
        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle(R.string.log_out);
        alert.show();
    }


    /*------------------------------------Handle Back press---------------------------------------*/
    @Override
    public void backPressed() {
        ((MainActivity) getActivity()).loadFragment(new HomeFragment());
        ((MainActivity) getActivity()).bottomNavigation.show(1, true);
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


    /*---------------------------- set the Grid Recycler view -----------------------------------------------*/

    private void getPhotos() {
        ArrayList<PhotoModel> photoModelList = new ArrayList<>();
        //make query to get the data
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {

                    //we're using nested node of likes so can not directly add the photoModel, first we have to typecast it to hashmap
                    PhotoModel photoModel = new PhotoModel();
                    Map<String, Object> objectMap = (Map<String, Object>) singleSnapshot.getValue();

                    assert objectMap != null;
                    photoModel.setCaption(objectMap.get(context.getString(R.string.field_caption)).toString());
                    photoModel.setRedirect(objectMap.get(context.getString(R.string.field_redirect)).toString());
                    photoModel.setDate_created(objectMap.get(context.getString(R.string.field_date_created)).toString());
                    photoModel.setImage_path(objectMap.get(context.getString(R.string.field_image_path)).toString());
                    photoModel.setPhoto_id(objectMap.get(context.getString(R.string.field_photo_id)).toString());
                    photoModel.setUser_id(objectMap.get(context.getString(R.string.field_photo_user_id)).toString());
                    photoModel.setTags(objectMap.get(context.getString(R.string.field_tags)).toString());

                    List<LikesModel> likesList = new ArrayList<LikesModel>();
                    for (DataSnapshot dSnapshot : singleSnapshot.child(context.getString(R.string.field_likes)).getChildren()){
                        LikesModel likesModel = new LikesModel();
                        likesModel.setUser_id(dSnapshot.getValue(LikesModel.class).getUser_id());
                        likesList.add(likesModel);
                    }
                    photoModel.setLikes(likesList);
                    photoModelList.add(photoModel);

                }
                //set the recycler view
                setUpRecyclerView(photoModelList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: Query cancelled");
            }
        });

    }

    private void setUpRecyclerView(ArrayList<PhotoModel> photoModel) {
        ArrayList<String> imgURLs = new ArrayList<>();
        for (int i = 0; i < photoModel.size(); i++) {
            imgURLs.add(photoModel.get(i).getImage_path());
        }

        gridRVAdapter = new GridRVAdapter(context, R.layout.item_grid_imageview, "", imgURLs, new GridRVAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //do something on item click
                Bundle bundle=new Bundle();
                bundle.putSerializable("photoModel",photoModel);
                bundle.putInt("position",position);
                bundle.putString("from","ProfileFragment");
                ProfilePostsFragment profilePostsFragment=new ProfilePostsFragment();
                profilePostsFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.fade_in,
                                R.anim.fade_out)
                        .replace(R.id.fl_mainContainer, profilePostsFragment)
                        .commit();

            }
        });

        int mNoOfColumns = calculateNoOfColumns(getApplicationContext, 130);
        rv_gridProfile.setLayoutManager(new GridLayoutManager(context, mNoOfColumns));
        rv_gridProfile.setAdapter(gridRVAdapter);
    }

    public static int calculateNoOfColumns(Context context, float columnWidthDp) { // For example columnWidthdp=180
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (screenWidthDp / columnWidthDp + 0.5); // +0.5 for correct rounding to int.
        return noOfColumns;
    }


    /*---------------------------Subscribe to photo upload progress--------------------------------*/
    //update the progressbar while uploading photo
    @Subscribe
    public void onUploadProgress(UploadEvent uploadEvent) {
        Log.d(TAG, "onUploadProgress: " + uploadEvent.progress);
        if (uploadEvent.progress >= 0) {
                /*if (galleryBottomSheet.isVisible()) {
                    galleryBottomSheet.dismiss();
                       This condition was causing problem when a edittext was used in the bottom sheet
                       if the keyboard is opened to change data in edittext than the value of bottom sheet
                       in this condition was received null
                }*/
            if (uploadEvent.progress == 101) {
                progressNewUpload.setVisibility(View.GONE);
                gridRVAdapter.notifyDataSetChanged();
                getPhotos();
            } else {
                progressNewUpload.setVisibility(View.VISIBLE);
                progressNewUpload.setProgress(uploadEvent.progress);
            }
        }
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}

