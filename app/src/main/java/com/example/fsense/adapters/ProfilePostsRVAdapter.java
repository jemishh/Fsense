package com.example.fsense.adapters;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.models.LikesModel;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserModel;
import com.example.fsense.utils.GlideImageLoader;
import com.example.fsense.utils.SquareImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePostsRVAdapter extends RecyclerView.Adapter<ProfilePostsRVAdapter.MyviewHolder> {

    private Context context;
    private String append;
    private ArrayList<PhotoModel> photoModel;

    //variables
    Utils utils;
    Animation animation;



    public ProfilePostsRVAdapter(Context context, String append, ArrayList<PhotoModel> photoModel, Utils utils) {
        this.context = context;
        this.append = append;
        this.photoModel = photoModel;
        this.utils = utils;
    }

    public interface Utils {
        GestureDetector gestureDetector(int position, ImageView img_heartOutlined, ImageView img_heartFilled, String photoId,Boolean likedByCurrentUser,String userId);
        void onRedirectClick(String redirect,String username,String user_id);
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_posts, parent, false);
        return new MyviewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {
        String imgURL = photoModel.get(position).getImage_path();
        GlideImageLoader.setImage(imgURL, holder.postImageView, holder.postImageProgressBar, append);
        /*GlideImageLoader.setImage(cimgUrl, holder.cimg_postTop, null, "");
        holder.tv_usernamePostsTop.setText(username);*/
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        String caption = photoModel.get(position).getCaption();
        String redirect = photoModel.get(position).getRedirect();
        String user_id =  photoModel.get(position).getUser_id();

        if(!redirect.equals("")) {
            holder.img_redirect.setVisibility(View.VISIBLE);
            holder.img_redirect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.img_redirect.startAnimation(animation);
                    Query query = dbRef.child(context.getString(R.string.dbname_user_info))
                            .child(user_id);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            utils.onRedirectClick(redirect, snapshot.getValue(UserInfoModel.class).getUsername(),user_id);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        } else{
            holder.img_redirect.setVisibility(View.GONE);
        }

        Query query = dbRef.child(context.getString(R.string.dbname_user_info))
                .child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GlideImageLoader.setImage(snapshot.getValue(UserInfoModel.class).getProfile_photo(), holder.cimg_postTop, null, "");
                holder.tv_usernamePostsTop.setText(snapshot.getValue(UserInfoModel.class).getUsername());
                holder.tv_postInfo.setText(decorateString(snapshot.getValue(UserInfoModel.class).getUsername(),caption, Typeface.BOLD));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //set The date
        String timeStampDiff = getTimeStampDifference(photoModel.get(position).getDate_created());
        holder.tv_timeStampPost.setText(timeStampDiff);
        String photoId = photoModel.get(position).getPhoto_id();

        //likes on photo
        List<LikesModel> likesList =photoModel.get(position).getLikes();
        Boolean likedByCurrentUser = false;
        if (likesList.size() == 0) {
            likedByCurrentUser = false;
            holder.tv_likedBy.setVisibility(View.GONE);
            holder.tv_likedBy.setText("");
        } else {
            for(int i=0;i<likesList.size();i++){
                if((likesList.get(i).getUser_id()).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    likedByCurrentUser=true;
                    break;
                } else{
                    likedByCurrentUser =false;
                }
            }
            holder.tv_likedBy.setVisibility(View.VISIBLE);
            getLikesString(likesList,holder.tv_likedBy);
        }
        GestureDetector gestureDetector = utils.gestureDetector(position, holder.img_heartOutlined, holder.img_heartFilled, photoId,likedByCurrentUser,photoModel.get(position).getUser_id());
        if (likedByCurrentUser) {
            holder.img_heartOutlined.setVisibility(View.GONE);
            holder.img_heartFilled.setVisibility(View.VISIBLE);
            holder.postImageView.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });

        } else {
            holder.img_heartOutlined.setVisibility(View.VISIBLE);
            holder.img_heartFilled.setVisibility(View.GONE);
            holder.postImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return gestureDetector.onTouchEvent(motionEvent);
                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return photoModel.size();
    }


    public class MyviewHolder extends RecyclerView.ViewHolder {

        SquareImageView postImageView;
        ProgressBar postImageProgressBar;
        TextView tv_usernamePostsTop, tv_postInfo, tv_viewComments, tv_timeStampPost, tv_likedBy;
        ImageView img_redirect, img_heartOutlined, img_heartFilled, img_comment;
        ImageButton img_postOption;
        CircleImageView cimg_postTop;


        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            postImageProgressBar = itemView.findViewById(R.id.postImageProgressBar);
            img_heartOutlined = itemView.findViewById(R.id.img_heartOutlined);
            img_heartFilled = itemView.findViewById(R.id.img_heartFilled);
            tv_usernamePostsTop = itemView.findViewById(R.id.tv_usernamePostsTop);
            tv_postInfo = itemView.findViewById(R.id.tv_postInfo);
            tv_timeStampPost = itemView.findViewById(R.id.tv_timeStampPost);
            tv_likedBy = itemView.findViewById(R.id.tv_likedBy);
            img_comment = itemView.findViewById(R.id.img_comment);
            tv_viewComments = itemView.findViewById(R.id.tv_viewComments);
            img_redirect = itemView.findViewById(R.id.img_redirect);
            cimg_postTop = itemView.findViewById(R.id.cimg_postTop);
            img_postOption = itemView.findViewById(R.id.img_postOption);

            //bounce animation
            animation = AnimationUtils.loadAnimation(context, R.anim.bounce);
        }
    }


    private void getLikesString(List<LikesModel> likesList, TextView tv_likedBy) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        StringBuilder likedBy = new StringBuilder();
        for(int i=0; i<likesList.size(); i++) {
            Query query = dbRef
                    .child(context.getString(R.string.dbname_users))
                    .orderByChild(context.getString(R.string.field_user_id))
                    .equalTo(likesList.get(i).getUser_id());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                        likedBy.append(singleSnapshot.getValue(UserModel.class).getUsername());
                        likedBy.append(",");
                    }

                    String[] splitLikedBy = likedBy.toString().split(",");
                    int length = splitLikedBy.length;

                    if (length == 1) {
                        String originalString = "Liked by " +  splitLikedBy[0];
                        final SpannableStringBuilder newString = new SpannableStringBuilder(originalString);
                        final StyleSpan bold = new StyleSpan(Typeface.BOLD);
                        newString.setSpan(bold, 9, 9+ splitLikedBy[0].length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        newString.setSpan(new RelativeSizeSpan(1.05f), 9, 9+splitLikedBy[0].length(), 0);
                        tv_likedBy.setText(newString);
                    } else if (length == 2) {
                        tv_likedBy.setText(decorateLikedByString(splitLikedBy[0], splitLikedBy[1], Typeface.BOLD));
                    } else if (length > 2) {
                        tv_likedBy.setText(decorateLikedByString(splitLikedBy[0], (splitLikedBy.length - 1) + " others", Typeface.BOLD));
                    } else {
                        tv_likedBy.setText("");
                        tv_likedBy.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
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
    private SpannableStringBuilder decorateLikedByString(String boldPart1,String boldPart2, int typeface) {
        String originalString = "Liked by " +  boldPart1 +  " and " + boldPart2;
        final SpannableStringBuilder newString = new SpannableStringBuilder(originalString);
        //bold
        newString.setSpan(new StyleSpan(typeface), 9, 9+boldPart1.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        newString.setSpan(new StyleSpan(typeface), 9+boldPart1.length()+5, 9+boldPart1.length()+5+boldPart2.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        //size
        newString.setSpan(new RelativeSizeSpan(1.05f), 9, 9+boldPart1.length(), 0);
        newString.setSpan(new RelativeSizeSpan(1.05f), 9+boldPart1.length()+5, 9+boldPart1.length()+5+boldPart2.length(), 0);
        return newString;
    }

    //get the post time
    private String getTimeStampDifference(String photoTimeStamp) {
        String difference = "";
        float diff = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp;
        try {
            timeStamp = sdf.parse(photoTimeStamp);
            //difference = String.valueOf(Math.round((today.getTime() - timeStamp.getTime()) / 1000 / 60 /60 /24));
            diff = Math.round((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60); //will give time in hours
            if (diff < 1) {
                diff = Math.round((today.getTime() - timeStamp.getTime()) / 1000 / 60);
                if (diff == 0) {
                    difference = "just now";
                } else {
                    difference = Math.round(diff) + " minutes ago";
                }
            } else if ((diff / 24) > 1) {
                diff = diff / 24;
                if (Math.round(diff) == 1) {
                    difference = Math.round(diff) + " day ago";
                } else {
                    difference = Math.round(diff) + " days ago";
                }
                /*store the strings in String resource folder if want to publish,
                so the play store can convert it into different language*/
            } else {
                difference = Math.round(diff) + " hours ago";
            }
        } catch (ParseException e) {
            Log.e(TAG, "getTimeStampDifference: ParceException" + e.getMessage());
            difference = "0";
        }
        return difference;
    }
}


