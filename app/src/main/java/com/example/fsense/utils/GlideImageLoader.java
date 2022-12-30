package com.example.fsense.utils;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.example.fsense.R;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;

@GlideModule    //add this line to get GlideApp
public class GlideImageLoader extends AppGlideModule {

    private static final int defaultImage= R.drawable.user;
    private Context context;

    public GlideImageLoader(Context context) {
        this.context= context;
    }

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        int memoryCacheSizeBytes = 1024*1024*20; // 20mb
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, memoryCacheSizeBytes))
                .setMemoryCache(new LruResourceCache(memoryCacheSizeBytes))
                .setDefaultRequestOptions(requestOptions(context));

    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(20, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);

        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }

    private static RequestOptions requestOptions(Context context){
        return new RequestOptions()
                .signature(new ObjectKey(
                        System.currentTimeMillis() / (24 * 60 * 60 * 1000)))
                .centerCrop()
                .encodeFormat(Bitmap.CompressFormat.PNG)
                .encodeQuality(100)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(PREFER_ARGB_8888)
                .skipMemoryCache(false);
    }

    /**
     * this method can be used to set images that are static, It can't be used if the Images are
     * being changed in the Fragment/Activity- OR if they are being set in a List or a Grid
     * @param imgURL
     * @param image
     * @param mProgressBar
     * @param append
     */
    public static void setImage(String imgURL, ImageView image, ProgressBar mProgressBar, String append){
        if(mProgressBar==null) {
            GlideApp.with(FsenseContext.getApplicationContext)
                    .load(append + imgURL)
                    .placeholder(defaultImage)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
        }else{
            GlideApp.with(FsenseContext.getApplicationContext)
                    .load(append + imgURL)
                    .placeholder(defaultImage)
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(image);
        }
    }

}
