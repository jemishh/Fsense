package com.example.fsense.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fsense.R;
import com.example.fsense.activity.MainActivity;
import com.example.fsense.adapters.SearchRvAdapter;
import com.example.fsense.interfaces.BackKeyPressedListener;
import com.example.fsense.models.UserModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SearchFragment extends Fragment implements BackKeyPressedListener, View.OnTouchListener {

    View view;
    Context context;
    public static BackKeyPressedListener backKeyPressedListener;
    TextInputEditText tetSearch;
    RecyclerView rv_listSearchProfiles;
    LinearLayout ll_searchProfile;
    TextView tv_initial;
    ImageView img_initial;

    ArrayList<UserModel> usersList = new ArrayList<>();
    SearchRvAdapter searchRvAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context=getContext();

        init();
        setListener();
        return view;
    }

    private void init(){
        tetSearch = view.findViewById(R.id.tetSearch);
        rv_listSearchProfiles = view.findViewById(R.id.rv_listSearchProfiles);
        ll_searchProfile = view.findViewById(R.id.ll_searchProfile);

        //initial setUp
        tv_initial = view.findViewById(R.id.tv_initial);
        img_initial = view.findViewById(R.id.img_initial);
        tv_initial.setVisibility(View.VISIBLE);
        img_initial.setVisibility(View.VISIBLE);

        closeKeyboard();
        initTextListener();
    }

    private void setListener(){
        rv_listSearchProfiles.setOnTouchListener(this);
        //ll_searchProfile.setOnTouchListener(this);
    }

    private void initTextListener() {
        tetSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text= tetSearch.getText().toString().toLowerCase(Locale.getDefault());
                searchForMatch(text);
            }
        });
    }

    private void searchForMatch (String keyword){
/*
        usersList.clear();
*/
        //update the usersList
        if(keyword.length() == 0){
            tv_initial.setVisibility(View.VISIBLE);
            img_initial.setVisibility(View.VISIBLE);
            usersList.clear();
            if(searchRvAdapter != null) {
                searchRvAdapter.notifyDataSetChanged();
            }
        }else{
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            Query query = dbRef.child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(keyword);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        tv_initial.setVisibility(View.GONE);
                        img_initial.setVisibility(View.GONE);
                        for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                            Boolean containsUser = false;
                            if(usersList.size() !=0) {
                                for(int i=0; i<usersList.size();i++){
                                    if(usersList.get(i).getUsername().equals(singleSnapshot.getValue(UserModel.class).getUsername())){
                                        containsUser = true;
                                        break;
                                    } else {
                                        containsUser = false;
                                    }
                                }
                                if(!containsUser) {
                                    usersList.add(singleSnapshot.getValue(UserModel.class));
                                    //update the recycler view
                                    updateUsersList();
                                }
                            } else {
                                usersList.add(singleSnapshot.getValue(UserModel.class));
                                //update the recycler view
                                updateUsersList();
                            }


                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private  void updateUsersList(){
        searchRvAdapter = new SearchRvAdapter(context, usersList, new SearchRvAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if(usersList.get(position) != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("userData", usersList.get(position));
                    bundle.putInt("position", position);
                    SearchedProfileFragment searchedProfileFragment = new SearchedProfileFragment();
                    searchedProfileFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(
                                    R.anim.fade_in,
                                    R.anim.fade_out)
                            .replace(R.id.fl_mainContainer, searchedProfileFragment)
                            .commit();
                } else{
                    Toast.makeText(context, "Unable to find the user", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rv_listSearchProfiles.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false
        ));
        rv_listSearchProfiles.setAdapter(searchRvAdapter);
    }


    /*------------------------------------Handle Back press---------------------------*/
    @Override
    public void backPressed() {
        ((MainActivity)getActivity()).loadFragment(new HomeFragment());
        ((MainActivity)getActivity()).bottomNavigation.show(1,true);
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
        if(view.getId() == R.id.rv_listSearchProfiles){
            closeKeyboard();
        }
        return true;
    }
}
