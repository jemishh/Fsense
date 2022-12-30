package com.example.fsense.bottomSheet;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import static com.example.fsense.fragments.ProfileFragment.calculateNoOfColumns;
import static com.example.fsense.utils.FsenseContext.getApplicationContext;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.adapters.GridRVAdapter;
import com.example.fsense.adapters.SpinnerAdapter;
import com.example.fsense.events.UploadEvent;
import com.example.fsense.models.ImageModel;
import com.example.fsense.utils.FilePaths;
import com.example.fsense.utils.FileSearch;
import com.example.fsense.utils.FirebaseMethods;
import com.example.fsense.utils.FsenseContext;
import com.example.fsense.utils.GlideImageLoader;
import com.example.fsense.utils.ImageListGenerator;
import com.example.fsense.utils.Permissions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GalleryBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener, View.OnTouchListener{

    View bottomSheetView;
    BottomSheetDialog bottomSheetDialog;
    BottomSheetBehavior bottomSheetBehavior;
    Context context;

    //widgets
    RecyclerView rv_gridGallery;
    ImageView img_galleryTop;
    CardView cv_galleryTopImg;
    ProgressBar progressBar;
    Spinner sp_directory;
    ImageButton ibtn_camera;
    CardView cv_next;
    TextView tvNext;
    LinearLayout ll_bottomSheetGallery;

    //gallery variables
    List<ImageModel> imageModelList = new ArrayList<>();
    Map<String, Integer> listImageDirectories = new HashMap<>();
    ArrayList<String> directories = new ArrayList<>();
    ArrayList<String> directoryNames = new ArrayList<>();
    private final String mAppend = "file://";
    private String selectedImage;
    GridRVAdapter gridRVAdapter;
    SpinnerAdapter spinnerAdapter;
    String fromFragment="";

    //camera variables
    ActivityResultLauncher<Intent> activityResultLauncher;
    Bitmap thumbnail;
    Uri imageUri;
    int fsenseDirectoryPosition,cameraDirectoryPosition;
    //permissions
    Map<String, Boolean> isGranted;
    Boolean cameraAccess = false;

    //firebase
    private FirebaseAuth fAuth;
    private FirebaseAuth.AuthStateListener fAuthListener;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference dbRef;
    private FirebaseMethods firebaseMethods;
    private int imageCount=0;

    //Post dialog
    Dialog postDialog;
    MaterialCardView cv_postPhoto, cv_cancelPhoto;
    TextInputEditText tetCaption, tetRedirect;
    TextInputLayout tilCaption, tilRedirect;
    ImageView img_post;
    Animation animation;
    String caption, redirect;
    //Caption
    RelativeLayout rl_caption;




    public GalleryBottomSheet() {
    }

    public GalleryBottomSheet(Context context, Map<String, Boolean> isGranted,String fromFragment) {
        this.context = context;
        this.isGranted = isGranted;
        this.fromFragment=fromFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        return bottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bottomSheetView = inflater.inflate(R.layout.layout_bottom_gallery, container, false);
        return bottomSheetView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        init();
        setUpGallery();
        setListener();
    }

    @Override
    public int getTheme() {
        return R.style.CustomBottomSheetDialog;
    }

    private void init(){
        //set min height to parent view
        ll_bottomSheetGallery = bottomSheetDialog.findViewById(R.id.ll_bottomSheetGallery);
        assert ll_bottomSheetGallery != null;
        ll_bottomSheetGallery.setMinimumHeight(Resources.getSystem().getDisplayMetrics().heightPixels);

        //initialize the views
        rv_gridGallery = bottomSheetDialog.findViewById(R.id.rv_gridGallery);
        img_galleryTop = bottomSheetDialog.findViewById(R.id.img_galleryTop);
        cv_galleryTopImg = bottomSheetDialog.findViewById(R.id.cv_galleryTopImg);
        progressBar = bottomSheetDialog.findViewById(R.id.progressBar);
        sp_directory = bottomSheetDialog.findViewById(R.id.sp_directory);
        ibtn_camera = bottomSheetDialog.findViewById(R.id.ibtn_camera);
        //img_close=bottomSheetDialog.findViewById(R.id.img_close);
        cv_next = bottomSheetDialog.findViewById(R.id.cv_next);
        cv_next.setVisibility(View.VISIBLE);
        tvNext=bottomSheetDialog.findViewById(R.id.tvNext);
        if(fromFragment.equals(getResources().getString(R.string.edit_profile_fragment))){
            tvNext.setTextSize(12);
            tvNext.setText(getResources().getString(R.string.update));
        } else {
            tvNext.setText(getResources().getString(R.string.next));
        }

        rl_caption= bottomSheetDialog.findViewById(R.id.rl_caption);
        rl_caption.setVisibility(View.GONE);


        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
            if (result.getResultCode() == RESULT_OK) //&& result.getData() != null
            {
                try {
                    new Thread(() -> {
                        try {
                            thumbnail = MediaStore.Images.Media.getBitmap(
                                    getActivity().getContentResolver(), imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //..update your UI here
                                // set image to ImageView
                                try {
                                    if(spinnerAdapter!=null){
                                        sp_directory.setSelection(fsenseDirectoryPosition);
                                    }
                                    //img_showImage.setImageBitmap(thumbnail);
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //firebase
        setupFirebaseAuth();
        firebaseMethods=new FirebaseMethods(context);

        //bounce animation
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);
    }

    //check permissions and set the layout
    private void setUpGallery(){
        if (context != null) {
            String key;
            Boolean value;
            int count = 0;
            for (Map.Entry<String, Boolean> entry : isGranted.entrySet()) {
                key = entry.getKey();
                value = entry.getValue();
                if ((key.equals("android.permission.READ_EXTERNAL_STORAGE") || key.equals("android.permission.WRITE_EXTERNAL_STORAGE")) && value) {
                    count++;
                } else if (key.equals("android.permission.CAMERA") && value) {
                    cameraAccess = true;
                }
            }
            if (count == 2) {
                setSpinner();
            } else {
                Toast.makeText(context, "Allow Storage permission/s from settings", Toast.LENGTH_SHORT).show();
            }
        } else {
            dismiss();
        }
    }

    //set Listeners
    private void setListener() {
        cv_next.setOnClickListener(this);
        ibtn_camera.setOnClickListener(this);
        ll_bottomSheetGallery.setOnTouchListener(this);
    }


    //Setting up the spinner
    private void setSpinner() {
        String nameOfDirectory;
        listImageDirectories.clear();
        directories.clear();
        directoryNames.clear();
        /*for(int i=0;i<imageModelList.size();i++){
            Log.d(TAG, "init: "+imageModelList.);
        }*/

        //set spinner
        listImageDirectories = countContent();

        directories.addAll(listImageDirectories.keySet());
        Log.d(TAG, "setSpinner: " + directories);
        for (int i = 0; i < directories.size(); i++) {
            int index = directories.get(i).lastIndexOf("/") + 1;
            nameOfDirectory = directories.get(i).substring(index);
            if (nameOfDirectory.equals("")) {
                nameOfDirectory = "Others";
            } else if(nameOfDirectory.equals("FSense")) {
                fsenseDirectoryPosition=i;
            } else if(nameOfDirectory.equals("Camera")) {
                cameraDirectoryPosition=i;
            }
            directoryNames.add(nameOfDirectory);
        }

        spinnerAdapter = new SpinnerAdapter(context, directoryNames);
        sp_directory.setAdapter(spinnerAdapter);
        sp_directory.setSelection(cameraDirectoryPosition);
        sp_directory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(rv_gridGallery.getVisibility()==View.GONE) {
                    rl_caption.setVisibility(View.GONE);
                    rv_gridGallery.setVisibility(View.VISIBLE);
                }else{

                }
                FilePaths filePaths = new FilePaths();
                ImageView img_downArrow = view.findViewById(R.id.img_downArrow);
                img_downArrow.setVisibility(View.VISIBLE);
                //Log.d(TAG, "onItemSelected: "+directoryNames.get(position));
                try {
                    final ArrayList<String> imgURLs = FileSearch.getFilePaths(filePaths.ROOT_DIR + "/" + directories.get(position));
                    Log.d(TAG, "onItemSelected: " + filePaths.ROOT_DIR + "/" + imgURLs.size());
                    Collections.reverse(imgURLs);
                    setUpRecyclerView(imgURLs);

                    //set the first image to be displayed when the activity fragment view is inflated
                    try{
                        setImage(imgURLs.get(0), img_galleryTop, mAppend);
                        selectedImage = imgURLs.get(0);
                    }catch (ArrayIndexOutOfBoundsException e){
                        Log.e(TAG, "onItemSelected: ArrayIndexOutOfBoundsException " + e.getMessage() );
                    }

                } catch (NullPointerException e) {
                    Log.e(TAG, "onItemSelected: ", e);
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    //setting up the recycler view grid.
    private void setUpRecyclerView(ArrayList<String> imgURLs) {
        //set the grid column width
        gridRVAdapter = new GridRVAdapter(context, R.layout.item_grid_imageview, mAppend, imgURLs, new GridRVAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                setImage(imgURLs.get(position), img_galleryTop, mAppend);
                selectedImage = imgURLs.get(position);

            }
        }); //mAppend is required
        int mNoOfColumns = calculateNoOfColumns(getApplicationContext, 130);
        rv_gridGallery.setLayoutManager(new GridLayoutManager(context, mNoOfColumns));
        rv_gridGallery.setAdapter(gridRVAdapter);

    }

    //counting the images in folder
    private Map<String, Integer> countContent() {
        imageModelList.clear();
        //generate the image list
        ImageListGenerator imageListGenerator = new ImageListGenerator();
        imageModelList = imageListGenerator.getImageList(getApplicationContext);
        Log.d(TAG, "onViewCreated: " + imageModelList.size());

        Map<String, Integer> imageCount = new HashMap<>();

        //images
        for (int i = 0; i < imageModelList.size(); i++) {
            String result = imageModelList.get(i).getRelativePath();
            //Log.d(TAG, "countContent: "+result);
            ArrayList<String> arrayImage = new ArrayList<>();
            //Log.d(TAG, "countContent: "+result.substring(0, result.length() - 1));
            arrayImage.add(result.substring(0, result.length() - 1));
            Collections.sort(arrayImage);
            for (String s : arrayImage) {
                if (s.startsWith("WhatsApp")) {
                    continue;
                }
                //Log.d(TAG, "countContent: "+s);
                if (imageCount.containsKey(s)) {
                    //if the map contain this key then just increment your count
                    imageCount.put(s, imageCount.get(s) + 1);

                } else {
                    //else just create a new node with 1
                    imageCount.put(s, 1);
                }
            }
        }
        Log.d(TAG, "countContent: " + imageCount);
        return imageCount;
    }

    //set the image
    private void setImage(String imgURL, ImageView image, String append) {
        Log.d(TAG, "setImage: setting image");
        GlideImageLoader.setImage(imgURL, image, progressBar, append);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cv_next: //next or update are same
                if(fromFragment.equals(getResources().getString(R.string.edit_profile_fragment))){
                    //Log.d(TAG, "onClick: "+selectedImage);
                    FirebaseMethods firebaseMethods=new FirebaseMethods(context);
                    firebaseMethods.uploadPhoto(context.getString(R.string.profile_photo),selectedImage,null,null,0);
                } else if (fromFragment.equals(getResources().getString(R.string.profile_fragment))){

                    rl_caption.setVisibility(View.VISIBLE);
                    rv_gridGallery.setVisibility(View.GONE);
                    cv_next.setVisibility(View.GONE);
                    cv_postPhoto = bottomSheetDialog.findViewById(R.id.cv_postPhoto);
                    cv_cancelPhoto = bottomSheetDialog.findViewById(R.id.cv_cancelPhoto);
                    tetCaption = bottomSheetDialog.findViewById(R.id.tetCaption);
                    tetRedirect = bottomSheetDialog.findViewById(R.id.tetRedirect);
                    tilCaption = bottomSheetDialog.findViewById(R.id.tilCaption);
                    tilRedirect = bottomSheetDialog.findViewById(R.id.tilRedirect);
                    //cancel the dialog
                    cv_cancelPhoto.setOnClickListener(view1 -> {
                        cv_cancelPhoto.startAnimation(animation);
                        rl_caption.setVisibility(View.GONE);
                        rv_gridGallery.setVisibility(View.VISIBLE);
                        cv_next.setVisibility(View.VISIBLE);
                    });
                    cv_postPhoto.setOnClickListener(view1 -> {
                        cv_postPhoto.startAnimation(animation);
                        caption = tetCaption.getText().toString();
                        redirect = tetRedirect.getText().toString();

                        rl_caption.setVisibility(View.GONE);
                        rv_gridGallery.setVisibility(View.VISIBLE);
                        closeKeyboard(bottomSheetView);
                        FirebaseMethods firebaseMethods=new FirebaseMethods(context);
                        firebaseMethods.uploadPhoto(context.getString(R.string.new_photo),selectedImage,caption,redirect,imageCount);
                    });

                   /* //creating confirm password dialog
                    postDialog = new Dialog(context);
                    postDialog.setContentView(R.layout.layout_upload_photo_dialog);
                    postDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    postDialog.setCanceledOnTouchOutside(false);

                    //initializing dialog elements
                    CardView cv_confirmPassDialog = postDialog.findViewById(R.id.cv_newPostDialog);
                    tv_postPhoto = postDialog.findViewById(R.id.tv_postPhoto);
                    tv_cancelPhoto = postDialog.findViewById(R.id.tv_cancelPhoto);
                    tetCaption = postDialog.findViewById(R.id.tetCaption);

                    img_post = postDialog.findViewById(R.id.img_post);
                    setImage(selectedImage, img_post, mAppend);
                    cv_confirmPassDialog.setAnimation(animation);
                    cv_confirmPassDialog.startAnimation(animation);

                    //cancel the dialog
                    tv_cancelPhoto.setOnClickListener(view1 -> {
                        tv_cancelPhoto.startAnimation(animation);
                        postDialog.dismiss();
                    });

                    //create post
                    tv_postPhoto.setOnClickListener(view1 -> {
                        tv_postPhoto.startAnimation(animation);
                        String caption = tetCaption.getText().toString();
                        FirebaseMethods firebaseMethods=new FirebaseMethods(context);
                        firebaseMethods.uploadPhoto(context.getString(R.string.new_photo),selectedImage,caption,imageCount);
                        postDialog.dismiss();
                    });

                    postDialog.show();*/


                }
                break;
            case R.id.ibtn_camera:
                if (cameraAccess) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    String time = String.valueOf(System.currentTimeMillis());
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + time + ".jpg");
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, "datetaken");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "FSense");
                    imageUri = getActivity().getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                    activityResultLauncher.launch(cameraIntent);
                } else {
                    Toast.makeText(context, "Fsense don't have permission to access CAMERA", Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, "Please allow from settings", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(spinnerAdapter!=null) {
            spinnerAdapter.clear();
            setSpinner();
        }

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
                //Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());
            } else {
                //user is signed out
                //Log.d(TAG, "onAuthStateChanged: signed_out");
            }
        };

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(context!=null) {
                    imageCount = firebaseMethods.getImageCount(snapshot);
                } else dismiss();
                //Log.d(TAG, "onDataChange: image count: "+ imageCount);

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



    /*---------------------------Subscribe to photo upload progress--------------------------------*/
    //update the progressbar while uploading photo
    @Subscribe
    public void onUploadProgress(UploadEvent uploadEvent){
        Log.d(TAG, "onUploadProgress: "+uploadEvent.progress);
        if(uploadEvent.progress>=0) {
            if (bottomSheetDialog.isShowing()) {
                dismiss();
            }
        }
    }

    /*-----------------------Other Methods-----------------------------------------*/
    private void closeKeyboard(View view) {

        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view.getId() == R.id.ll_bottomSheetGallery) {
            Log.d(TAG, "onTouch: Touched");
            closeKeyboard(bottomSheetView);
        }
        return true;
    }

}
