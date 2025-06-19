package com.hfad.veloxmechanics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hfad.veloxmechanics.R;
import com.hfad.veloxmechanics.model.SpinnerItem;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<SpinnerItem> {

    private ArrayList<SpinnerItem> list ;

    public SpinnerAdapter(@NonNull Context context, ArrayList<SpinnerItem> list) {
        super(context, 0, list);
        this.list = list;
    }

    @Nullable
    @Override
    public SpinnerItem getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return customView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return customView(position, convertView, parent);
    }

    public View customView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, parent, false);
        }
        SpinnerItem item = getItem(position);
        ImageView spinnerImage = convertView.findViewById(R.id.spinner_image);
        TextView spinnerText = convertView.findViewById(R.id.spinner_text);

        if (item != null) {
            spinnerImage.setImageResource(item.getSpinnerImage());
            spinnerText.setText(item.getSpinnerText());
        }
        return convertView;
    }

}
