package com.example.fsense.utils;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.fsense.R;
import com.example.fsense.events.UploadEvent;
import com.example.fsense.fragments.EditProfileFragment;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserModel;
import com.example.fsense.models.UserSettingsModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {

    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private final StorageReference storageReference;
    private DatabaseReference dbRef;

    //vars
    private Context context;
    private String userID = "";
    private double photoUploadProgress=0;
    ProgressBar progressProfileUpload;

    public FirebaseMethods(Context context) {
        fAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        this.context = context;
        if (fAuth.getCurrentUser() != null) {
            userID = fAuth.getCurrentUser().getUid();
            //Log.d(TAG, "FirebaseMethods: User ID- " + userID);
        } else {
            Log.d(TAG, "FirebaseMethods: didn't get the user ID");
        }
    }

    public FirebaseMethods(ProgressBar progressProfileUpload){
        storageReference = FirebaseStorage.getInstance().getReference();
        this.progressProfileUpload=progressProfileUpload;
    }

    /*-------------------------------------register new user-------------------------------------------*/
    //register user to authentication
    public void registerNewUser(final String email, String password) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Signup Successful", Toast.LENGTH_SHORT).show();
                    userID = fAuth.getCurrentUser().getUid();
                    FirebaseMethods.this.sendVerificationEmail();

                }
            }
        });
    }

    //send email
    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    } else {
                        Toast.makeText(context, "Couldn't send verification email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //add user details to database
    public void addNewUser(String email, String username, String displayName, String description, String website, String profilePhoto, long contact) {
        UserModel user = new UserModel(userID, StringManipulation.condenseUsername(username), email, contact);
        dbRef.child(context.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);
        UserInfoModel userInfo = new UserInfoModel(description, displayName, profilePhoto, StringManipulation.condenseUsername(username), website, 0, 0, 0);
        dbRef.child(context.getString(R.string.dbname_user_info))
                .child(userID)
                .setValue(userInfo);
    }

    /*--------------------------------------getting the UserInformation---------------------------*/

    /**
     * retrieves the account settings for the currently logged in user
     * Database will be the user_account_settings node.
     *
     * @param dataSnapshot
     * @return
     */
    public UserSettingsModel getUserSettings(DataSnapshot dataSnapshot) {
        Log.d(TAG, "getUserSettings: Retrieving user account settings from firebase");

        UserInfoModel userInfoModel = new UserInfoModel();
        UserModel userModel = new UserModel();

        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            //user_account_settings node
            if (ds.getKey().equals(context.getString(R.string.dbname_user_info))) {
                //Log.d(TAG, "getUserSettings: datasnapshotUserInfo: " + ds);

                try {
                    userInfoModel.setDisplay_name(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getDisplay_name());
                    userInfoModel.setUsername(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getUsername());
                    userInfoModel.setWebsite(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getWebsite());
                    userInfoModel.setDescription(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getDescription());
                    userInfoModel.setProfile_photo(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getProfile_photo());
                    userInfoModel.setPosts(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getPosts());
                    userInfoModel.setFollowers(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getFollowers());
                    userInfoModel.setFollowing(
                            ds.child(userID).
                                    getValue(UserInfoModel.class).
                                    getFollowing());
                    //Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information"+userInfoModel.toString());

                } catch (NullPointerException e) {
                    //Log.e(TAG, "getUserAccountSettings: NullPointerException" + e.getMessage());
                }
            }

            //users node
            if (ds.getKey().equals(context.getString(R.string.dbname_users))) {
                //Log.d(TAG, "getUserAccountSettings: dataSnapshotUsers: " + ds);

                userModel.setUsername(
                        ds.child(userID).
                                getValue(UserModel.class).
                                getUsername());
                userModel.setEmail(
                        ds.child(userID).
                                getValue(UserModel.class).
                                getEmail());
                userModel.setContact(
                        ds.child(userID).
                                getValue(UserModel.class).
                                getContact());
                userModel.setUserId(
                        ds.child(userID).
                                getValue(UserModel.class).
                                getUserId());
                //Log.d(TAG, "getUserAccountSettings: retrieved user information - " + userModel.toString());
            }
        }
        return new UserSettingsModel(userModel, userInfoModel);

    }

    /*------------------------------------ update information -----------------------------------------*/


    //update username in users node and user_info node
    public void updateUsername(String userName){
        Log.d(TAG, "updateUsername: updating username to: "+ userName);
        dbRef.child(context.getString(R.string.dbname_users))
                .child(userID)
                .child(context.getString(R.string.field_username))
                .setValue(userName);
        dbRef.child(context.getString(R.string.dbname_user_info))
                .child(userID)
                .child(context.getString(R.string.field_username))
                .setValue(userName);
    }

    //update email in users node
    public void updateEmail(String email){
        Log.d(TAG, "updateEmail updating email to: "+ email);
        dbRef.child(context.getString(R.string.dbname_users))
                .child(userID)
                .child(context.getString(R.string.field_email))
                .setValue(email);

    }

    //update other info in user_info node
    public void updateUserAccountSettings(String displayName,String website,String description,long phoneNumber){

        Log.d(TAG, "updateUserAccountSettings: updating user account settings");
        if(displayName !=null) {
            dbRef.child(context.getString(R.string.dbname_user_info))
                    .child(userID)
                    .child(context.getString(R.string.field_display_name))
                    .setValue(displayName);
        }
        if(website !=null) {
            dbRef.child(context.getString(R.string.dbname_user_info))
                    .child(userID)
                    .child(context.getString(R.string.field_website))
                    .setValue(website);
        }
        if(description !=null) {
            dbRef.child(context.getString(R.string.dbname_user_info))
                    .child(userID)
                    .child(context.getString(R.string.field_description))
                    .setValue(description);
        }
        if(phoneNumber != 0) {
            dbRef.child(context.getString(R.string.dbname_user_info))
                    .child(userID)
                    .child(context.getString(R.string.field_contact))
                    .setValue(website);
        }
    }


    /*--------------------------------  Dealing with photos  ---------------------------------------*/

    public int getImageCount(DataSnapshot dataSnapshot){
        int count=0;

        for(DataSnapshot ds:dataSnapshot
                .child(context.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){
            count++;
        }

        return count;
    }

    public void uploadPhoto(String photoType,String imageUrl,String caption, String redirect, int imageCount){
        //get the file paths
        FilePaths filePaths=new FilePaths();

        //--------------------------------------check if the photo is for profile photo or for post
        if (photoType.equals(context.getString(R.string.profile_photo))) {
            Log.d(TAG, "uploadPhoto: Uploading profile photo ");
            Toast.makeText(context, "Updating profile photo", Toast.LENGTH_SHORT).show();

            String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference mStorageReference = storageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + userID + "/profile_photo");

            //convert imageUrl to Bitmap
            Bitmap bitmap = ImageManager.getBitmap(imageUrl);
            byte[] imageBytes=ImageManager.getBytesFromBitmap(bitmap,100);

            //upload image to firebase
            UploadTask uploadTask=null;
            uploadTask = mStorageReference.putBytes(imageBytes);
                        // uploadTask = storageReference.putFile(file);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Log.d(TAG, "onComplete: "+downloadUri);
                                //insert into the 'user_info' node
                                setProfilePhoto(task.getResult().toString());
                            } else {

                            }
                            Toast.makeText(context, "Photo Upload Success", Toast.LENGTH_SHORT).show();
                            updateProgress(101);
                            Log.d(TAG, "onComplete: downloaded URI"+task.getResult().toString());
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(context, "photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress= (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    /*if(progress-15 > photoUploadProgress){
                        //Toast.makeText(context, "Photo Upload Progress: "+String.format("%.0f",progress)+"%" ,Toast.LENGTH_LONG).show();
                        photoUploadProgress=progress;
                        int currentProgress = (int) photoUploadProgress;
                        updateProgress(currentProgress);
                    }*/
                    int currentProgress = (int) progress;
                    updateProgress(currentProgress);
                    Log.d(TAG, "onProgress: Upload Progress: "+progress+"% done");
                }
            });
        }

        //-------------------------------------------------------uploading new Photo
        else if ((photoType.equals(context.getString(R.string.new_photo)))) {
            Log.d(TAG, "uploadPhoto: Uploading new photo ");
            Toast.makeText(context, "Posting new photo", Toast.LENGTH_SHORT).show();


            String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
            StorageReference mStorageReference = storageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + userID + "/photo"+(imageCount+1));

            //convert imageUrl to Bitmap
            Bitmap bitmap = ImageManager.getBitmap(imageUrl);
            byte[] imageBytes=ImageManager.getBytesFromBitmap(bitmap,100);

            //upload image to firebase
            UploadTask uploadTask=null;
            uploadTask = mStorageReference.putBytes(imageBytes);
            // uploadTask = storageReference.putFile(file);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mStorageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                //Log.d(TAG, "onComplete: "+downloadUri);

                            } else {

                            }
                            Toast.makeText(context, "Photo Upload Success", Toast.LENGTH_SHORT).show();

                            //add the new photo to 'photos' node and 'user_photos' node
                            addPhotoToDatabase(caption,redirect,task.getResult().toString());                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failed");
                    Toast.makeText(context, "photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress= (100 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    /*if(progress-15 > photoUploadProgress){
                        //Toast.makeText(context, "Photo Upload Progress: "+String.format("%.0f",progress)+"%" ,Toast.LENGTH_LONG).show();
                        photoUploadProgress=progress;
                        int currentProgress = (int) photoUploadProgress;
                        updateProgress(currentProgress);
                    }*/
                    int currentProgress = (int) progress;
                    updateProgress(currentProgress);
                    Log.d(TAG, "onProgress: Upload Progress: "+progress+"% done");
                }
            });
        }
    }


    //---------------methods for profile photo
    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: Setting profile image"+url);
        dbRef.child(context.getString(R.string.dbname_user_info))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(context.getString(R.string.profile_photo))
                .setValue(url);
    }

    public void updateProgress(int progress) {
        EventBus.getDefault().post(new UploadEvent(progress));
    }

    //---------------methods for new photo
    private void addPhotoToDatabase(String caption,String redirect, String url){
        Log.d(TAG, "addPhotoToDatabase: Adding photo to database");

        String tags=StringManipulation.getTags(caption);
        String newPhotoKey=dbRef.child(context.getString(R.string.dbname_photos)).push().getKey();
        PhotoModel photoModel=new PhotoModel();
        photoModel.setCaption(caption);
        photoModel.setDate_created(getTimestamp());
        photoModel.setImage_path(url);
        photoModel.setTags(tags);
        photoModel.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photoModel.setPhoto_id(newPhotoKey);
        photoModel.setRedirect(redirect);

        //finally insert into the database
        dbRef.child(context.getString(R.string.dbname_user_photos)).child(FirebaseAuth.getInstance()
                .getCurrentUser().getUid())
                .child(newPhotoKey).setValue(photoModel);
        dbRef.child(context.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photoModel);
        updateProgress(101);
    }

    private String getTimestamp(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        //search for list of android time zones
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }
}
