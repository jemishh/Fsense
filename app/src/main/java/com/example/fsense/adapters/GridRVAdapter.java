package com.example.fsense.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.GlideBuilder;
import com.example.fsense.R;
import com.example.fsense.utils.GlideImageLoader;
import com.example.fsense.utils.SquareImageView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GridRVAdapter extends RecyclerView.Adapter<GridRVAdapter.MyviewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private int layoutResource;
    private String append;
    private ArrayList<String> imgURLs;
    ClickListener clickListener;

    public GridRVAdapter(Context context, int layoutResource, String append, ArrayList<String> imgURLs,ClickListener clickListener) {
        this.context = context;
        this.layoutResource = layoutResource;
        this.append = append;
        this.imgURLs = imgURLs;
        this.clickListener=clickListener;
    }


    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        LayoutInflater inflater=LayoutInflater.from(context);
        view=inflater.inflate(R.layout.item_grid_imageview,parent,false);
        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {

        String imgURL=imgURLs.get(position);
        GlideImageLoader.setImage(imgURL, holder.imageView,holder.gridImageProgressBar,append);
        /*ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append + imgURL, holder.imageView, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if(holder.gridImageProgressBar!=null){
                    holder.gridImageProgressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if(holder.gridImageProgressBar!=null){
                    holder.gridImageProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if(holder.gridImageProgressBar!=null){
                    holder.gridImageProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if(holder.gridImageProgressBar!=null){
                    holder.gridImageProgressBar.setVisibility(View.GONE);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return imgURLs.size();
    }

    public class MyviewHolder extends RecyclerView.ViewHolder {

        SquareImageView imageView;
        ProgressBar gridImageProgressBar;
        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.gridImageView);
            gridImageProgressBar=itemView.findViewById(R.id.gridImageProgressBar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(getAdapterPosition(),view);
                }
            });
        }
    }
}
