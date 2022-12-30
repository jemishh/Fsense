package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.adapters.ProfilePostsRVAdapter;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.LikesModel;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilePostsFragment extends Fragment implements BackKeyPressedListener, View.OnClickListener {

    View view;
    Context context;
    public static BackKeyPressedListener backKeyPressedListener;

    //Views
    ImageView img_backArrow;
    TextView tv_posts;
    RecyclerView rv_listProfilePosts;

    //firebase
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;

    //PhotoModel
    ArrayList<PhotoModel> photoModel = new ArrayList<>();

    //Variables
    String from = "";
    int position = 0;
    ProfilePostsRVAdapter adapter;
    Animation animation;
    private static final DecelerateInterpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_profile_posts, container, false);
        context = getContext();

        init();
        setupFirebaseAuth();
        setUpRecyclerView();
        setListener();


        return view;
    }

    private void init() {
        //get the arguments
        Bundle bundle = this.getArguments();
        photoModel = (ArrayList<PhotoModel>) bundle.getSerializable("photoModel");
        position = bundle.getInt("position");
        from = bundle.getString("from");
        //initialize
        img_backArrow = view.findViewById(R.id.img_backArrow);
        tv_posts = view.findViewById(R.id.tv_posts);
        rv_listProfilePosts = view.findViewById(R.id.rv_listProfilePosts);
        //cimg_posts = view.findViewById(R.id.cimg_posts);

        //bounce animation
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        if (from.equals("HomeFragment")) {
            tv_posts.setText("Home");
            img_backArrow.setVisibility(View.GONE);
        } else {
            tv_posts.setText("Posts");
            img_backArrow.setVisibility(View.VISIBLE);
        }

    }


    private void setListener() {
        img_backArrow.setOnClickListener(this);
    }

    private void setUpRecyclerView() {
        if (photoModel.size() != 0) {
            adapter = new ProfilePostsRVAdapter(context, "", photoModel, new ProfilePostsRVAdapter.Utils() {
                @Override
                public GestureDetector gestureDetector(int position, ImageView img_heartOutlined, ImageView img_heartFilled, String photoId, Boolean likedByCurrentUser, String userId) {
                    return new GestureDetector(context, new GestureListener(position, img_heartOutlined, img_heartFilled, photoId, likedByCurrentUser, userId));
                }

                @Override
                public void onRedirectClick(String redirect, String username,String user_id) {

                    if(!redirect.equals("")) {
                        Dialog redirectDialog = new Dialog(context);
                        redirectDialog.setContentView(R.layout.layout_redirect_dialog);
                        redirectDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        //initializing dialog elements
                        CardView cv_redirectDialog = redirectDialog.findViewById(R.id.cv_redirectDialog);
                        TextView dialogTitle = redirectDialog.findViewById(R.id.dialogTitle);
                        TextView tv_redirect = redirectDialog.findViewById(R.id.tv_redirect);
                        if(user_id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            dialogTitle.setText(decorateString("You",context.getString(R.string.text_got_it_from), Typeface.BOLD));
                        } else {
                            dialogTitle.setText(decorateString(username,context.getString(R.string.text_got_it_from), Typeface.BOLD));
                        }
                        tv_redirect.setText(redirect);
                        redirectDialog.show();
                    }
                }
            });
            rv_listProfilePosts.setLayoutManager(new LinearLayoutManager(
                    getActivity(), LinearLayoutManager.VERTICAL, false
            ));
            Objects.requireNonNull(rv_listProfilePosts.getLayoutManager()).scrollToPosition(position);
            rv_listProfilePosts.setAdapter(adapter);

        }
    }

    /*---------------gesture detector------------------*/
    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        ImageView img_heartOutlined, img_heartFilled;
        String photo_id;
        int position;
        Boolean likedByCurrentUser;
        String userId;

        public GestureListener(int position, ImageView img_heartOutlined, ImageView img_heartFilled, String photo_id, Boolean likedByCurrentUser, String userId) {
            this.img_heartOutlined = img_heartOutlined;
            this.img_heartFilled = img_heartFilled;
            this.photo_id = photo_id;
            this.position = position;
            this.likedByCurrentUser = likedByCurrentUser;
            this.userId = userId;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            //DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            Query query = dbRef
                    .child(context.getString(R.string.dbname_photos))
                    .child(photo_id)
                    .child(context.getString(R.string.field_likes));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        String keyId = singleSnapshot.getKey();
                        //case 1: the user already liked the photo
                        if (likedByCurrentUser && singleSnapshot.getValue(LikesModel.class).getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            removeLike(photo_id, keyId, userId);
                            toggleLike(img_heartOutlined, img_heartFilled);
                            adapter.notifyDataSetChanged();
                            updateLikesInPhotoModel(position);

                        }
                        //case 2: the user has not liked the photo
                        else if (!likedByCurrentUser) {
                            //add new like
                            addNewLike(photo_id, userId);
                            toggleLike(img_heartOutlined, img_heartFilled);
                            adapter.notifyDataSetChanged();
                            updateLikesInPhotoModel(position);
                            break;
                        }
                    }
                    if (!snapshot.exists()) {
                        //add new like
                        addNewLike(photo_id, userId);
                        toggleLike(img_heartOutlined, img_heartFilled);
                        adapter.notifyDataSetChanged();
                        updateLikesInPhotoModel(position);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            return true;
        }
    }

    private void removeLike(String photo_id, String keyId, String userId) {
        dbRef.child(context.getString(R.string.dbname_user_photos)).child(userId)
                .child(photo_id)
                .child(context.getString(R.string.field_likes))
                .child(keyId)
                .removeValue();
        dbRef.child(context.getString(R.string.dbname_photos))
                .child(photo_id)
                .child(context.getString(R.string.field_likes))
                .child(keyId)
                .removeValue();
    }

    private void addNewLike(String photo_id, String userId) {
        String newLikeId = dbRef.push().getKey();
        LikesModel likesModel = new LikesModel();
        likesModel.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        dbRef.child(context.getString(R.string.dbname_user_photos))
                .child(userId)
                .child(photo_id)
                .child(context.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(likesModel);
        dbRef.child(context.getString(R.string.dbname_photos))
                .child(photo_id)
                .child(context.getString(R.string.field_likes))
                .child(newLikeId)
                .setValue(likesModel);
    }

    public void toggleLike(ImageView img_heartOutlined, ImageView img_heartFilled) {
        AnimatorSet animatorSet = new AnimatorSet();
        if (img_heartFilled.getVisibility() == View.VISIBLE) {
            img_heartFilled.setScaleX(0.1f);
            img_heartFilled.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(img_heartFilled, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(img_heartFilled, "scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(ACCELERATE_INTERPOLATOR);

            img_heartFilled.setVisibility(View.GONE);
            img_heartOutlined.setVisibility(View.VISIBLE);
            animatorSet.playTogether(scaleDownY, scaleDownX);
        } else if (img_heartFilled.getVisibility() == View.GONE) {
            img_heartFilled.setScaleX(0.1f);
            img_heartFilled.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(img_heartFilled, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DECELERATE_INTERPOLATOR);
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(img_heartFilled, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DECELERATE_INTERPOLATOR);

            img_heartFilled.setVisibility(View.VISIBLE);
            img_heartOutlined.setVisibility(View.GONE);
            animatorSet.playTogether(scaleDownY, scaleDownX);
        }

        animatorSet.start();
    }


    private void updateLikesInPhotoModel(int position) {
        //update the likes to generate the likedBy string
        String photo_id = photoModel.get(position).getPhoto_id();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef.child(context.getString(R.string.dbname_photos))
                .child(photo_id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    List<LikesModel> likesList = new ArrayList<>();
                    for (DataSnapshot dSnapshot : snapshot.child(getString(R.string.field_likes)).getChildren()) {
                        LikesModel likesModel = new LikesModel();
                        likesModel.setUser_id(dSnapshot.getValue(LikesModel.class).getUser_id());
                        likesList.add(likesModel);
                    }
                    photoModel.get(position).setLikes(likesList);
                    adapter.notifyDataSetChanged();
                    //getLikesString(photoModel.get(position).getLikes());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    /*---------------------------------------- Handle Onclick ------------------------------------*/
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_backArrow:
                backPressed();
                break;

        }
    }

    /*---------------------------------------Firebase---------------------------------*/
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuthProfile: Setting up firebase auth in profilePostsFragment.");
        fAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();
        fAuthListener = firebaseAuth -> {
            FirebaseUser user = fAuth.getCurrentUser();
            if (user != null) {
                //user is signed in do something
            } else {
                //user is signed out
                Log.d(TAG, "profilePostsFragment: signed_out");
            }
        };

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.notifyDataSetChanged();
                //retrieve user info from the database
                /* setProfileWidgets(firebaseMethods.getUserSettings(snapshot));
                 * this method will get called everytime dataset changes hence if I call setUpRecyclerview
                 * from inside  this method it will get refreshed everytime change occurs in
                 * database so DO NOT call setUpRecyclerview from inside here
                 * */
                //retrieve images for the user in question
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


   /* private void setProfileWidgets(UserSettingsModel userSettings) {
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieved from firebase database"+userSettings.toString());

        //Set the values
        UserInfoModel userInfoModel = userSettings.getUserInfoModel();
            //GlideImageLoader.setImage(userInfoModel.getProfile_photo(), cimg_posts, null, "");
        username=userInfoModel.getUsername();
        cimgUrl=userInfoModel.getProfile_photo();
        //setUpRecyclerView();
    }*/

    @Override
    public void onStart() {
        super.onStart();
        fAuth.addAuthStateListener(fAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fAuthListener != null) {
            fAuth.removeAuthStateListener(fAuthListener);
        }
    }

    /*------------------------------------Handle Back press---------------------------------------*/
    @Override
    public void backPressed() {
        if (from.equals("ProfileFragment")) {
            ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
            ((MainActivity) getActivity()).bottomNavigation.show(3, true);
        } else if (from.equals("SearchedProfileFragment")) {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            Query query = dbRef.child(context.getString(R.string.dbname_users))
                    .child(photoModel.get(position).getUser_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userData", snapshot.getValue(UserModel.class));
                    bundle.putInt("position", 0);
                    SearchedProfileFragment searchedProfileFragment = new SearchedProfileFragment();
                    searchedProfileFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(
                                    R.anim.fade_in,
                                    R.anim.fade_out)
                            .replace(R.id.fl_mainContainer, searchedProfileFragment)
                            .commit();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (from.equals("HomeFragment")) {
            getActivity().finish();
        }
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


    //generate Bold string
    private SpannableStringBuilder decorateString(String boldPart, String normalPart, int typeface) {
        String originalString = boldPart + " " + normalPart;
        final SpannableStringBuilder newString = new SpannableStringBuilder(originalString);
        final StyleSpan bold = new StyleSpan(typeface);
        newString.setSpan(new RelativeSizeSpan(1.05f), 0, boldPart.length(), 0); // set size
        newString.setSpan(bold, 0, boldPart.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return newString;
    }

}
