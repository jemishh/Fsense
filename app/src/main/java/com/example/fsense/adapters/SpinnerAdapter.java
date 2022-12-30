package com.example.fsense.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fsense.R;
import com.example.fsense.models.SpinnerItemModel;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<String> {

    ArrayList<String> spinnerItemList;


    public SpinnerAdapter(Context context,
                          ArrayList<String> spinnerItemList)
    {
        super(context, 0, spinnerItemList);
        this.spinnerItemList=spinnerItemList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable
            View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable
            View convertView, @NonNull ViewGroup parent)
    {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView,
                          ViewGroup parent)
    {
        // It is used to set our custom view.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spinner, parent, false);
        }

        TextView textViewName = convertView.findViewById(R.id.tv_spinnerItem);
        // It is used the name to the TextView when the
        // current item is not null.
            textViewName.setText(spinnerItemList.get(position));
        return convertView;
    }
}