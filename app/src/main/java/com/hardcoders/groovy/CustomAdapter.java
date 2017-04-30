package com.hardcoders.groovy;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CustomAdapter extends ArrayAdapter<Track> {
    public CustomAdapter(@NonNull Context context, ArrayList<Track> names) {
        super(context, R.layout.item, names);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.item, null);
        }
        Track mp = getItem(position);
        if (mp != null) {
            TextView nameText = (TextView) view.findViewById(R.id.item_name);
            TextView artistText = (TextView) view.findViewById(R.id.item_artist);
            TextView albumText = (TextView) view.findViewById(R.id.item_album);
            ImageView imageView = (ImageView) view.findViewById(R.id.item_art);

            nameText.setText(mp.Name);
            //TODO Set Artist name and album
            artistText.setText("mp.Artist");
            albumText.setText("mp.Album");
            Picasso.with(getContext()).load(mp.ImageURLShort).into(imageView);
        }
        return view;
    }
}
