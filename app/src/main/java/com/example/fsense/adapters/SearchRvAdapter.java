package com.example.fsense.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.models.PhotoModel;
import com.example.fsense.models.UserInfoModel;
import com.example.fsense.models.UserModel;
import com.example.fsense.utils.GlideImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchRvAdapter extends RecyclerView.Adapter<SearchRvAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<UserModel> usersList;
    ClickListener clickListener;

    public SearchRvAdapter(Context context, ArrayList<UserModel> usersList, ClickListener clickListener) {
        this.context = context;
        this.usersList = usersList;
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    @NonNull
    @Override
    public SearchRvAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.item_search_user,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRvAdapter.MyViewHolder holder, int position) {
        holder.tv_searchUsername.setText(usersList.get(position).getUsername());

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query query = dbRef.child(context.getString(R.string.dbname_user_info))
                .child(usersList.get(position).getUserId());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 GlideImageLoader.setImage(snapshot.getValue(UserInfoModel.class).getProfile_photo(), holder.cimg_search_profile, null, "");
                    holder.tv_searchDisplayName.setText(snapshot.getValue(UserInfoModel.class).getDisplay_name());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView cimg_search_profile;
        TextView tv_searchUsername, tv_searchDisplayName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cimg_search_profile = itemView.findViewById(R.id.cimg_search_profile);
            tv_searchDisplayName =itemView.findViewById(R.id.tv_searchDisplayName);
            tv_searchUsername = itemView.findViewById(R.id.tv_searchUsername);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(getAdapterPosition(),view);
                }
            });
        }
    }
}
