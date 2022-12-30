package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.FollowModel;
import com.example.fsense.models.LikesModel;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.utils.FirebaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    View view;
    Context context;

    LinearLayout ll_initial;
    ProgressBar progressBar;
    Toolbar tempToolbar;
    FrameLayout fl_homeContainer;
    ArrayList<PhotoModel> photoModelList = new ArrayList<>();
    ArrayList<String> followingUsers = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_home, container, false);
        context=getContext();

        init();
        getFollowing();

        return view;
    }

    private void init() {
        Log.d(TAG, "init: " +getActivity());
        ll_initial = view.findViewById(R.id.ll_initial);
        progressBar = view.findViewById(R.id.progressBarHome);
        tempToolbar = view.findViewById(R.id.tempToolbar);
        tempToolbar.setVisibility(View.VISIBLE);
        //fl_homeContainer = view.findViewById(R.id.fl_homeContainer);
        ll_initial.setVisibility(View.GONE);
    }

    private void getFollowing(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef.child(context.getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()); //for user's profile
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot singleSnapshot :snapshot.getChildren()) {
                        followingUsers.add(singleSnapshot.child(context.getString(R.string.field_user_id_userInfo)).getValue().toString());
                    }
                }
                //getPhotoModel
                getPhotoModel();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPhotoModel(){
        Log.d(TAG, "getPhotoModel: " + followingUsers.size());
        if(followingUsers.size() ==0){
            ll_initial.setVisibility(View.VISIBLE);
        }else{
            ll_initial.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            for(int i=0; i<followingUsers.size(); i++) {
                final int count = i;
                Query query = dbRef
                        .child(context.getString(R.string.dbname_user_photos))
                        .child(followingUsers.get(i))
                        .orderByChild(context.getString(R.string.field_user_id_userInfo))
                        .equalTo(followingUsers.get(i));
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
                        if(count >= followingUsers.size() -1 && getActivity()!=null){
                            //display the photos
                            setUpProfilePostsFragment();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: Query cancelled");
                    }
                });
            } //for loop ends
        }
    }

    private void setUpProfilePostsFragment(){
        if(photoModelList != null) {
            ll_initial.setVisibility(View.GONE);
            Collections.sort(photoModelList, new Comparator<PhotoModel>() {
                @Override
                public int compare(PhotoModel t1, PhotoModel t2) {
                    return t2.getDate_created().compareTo(t1.getDate_created());
                }
            });
            Bundle bundle = new Bundle();
            bundle.putSerializable("photoModel", photoModelList);
            bundle.putInt("position", 0);  //there will be no position as we are not in profile
            bundle.putString("from", "HomeFragment");
            ProfilePostsFragment profilePostsFragment = new ProfilePostsFragment();
            profilePostsFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.fade_in,
                            R.anim.fade_out)
                    .replace(R.id.fl_mainContainer, profilePostsFragment)
                    .commit();
            tempToolbar.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            ll_initial.setVisibility(View.VISIBLE);
        }
    }

}
