package com.hardcoders.groovy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


class CustomAdapter extends ArrayAdapter<Track> {
    CustomAdapter(@NonNull Context context, ArrayList<Track> names) {
        super(context, R.layout.item_online, names);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        Track mp = getItem(position);
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            assert mp != null;
            if (mp.ImageURI == null)
                view = layoutInflater.inflate(R.layout.item_online, null);
            else
                view = layoutInflater.inflate(R.layout.item_offline, null);
        }
        if (mp != null) {
            TextView nameText = (TextView) view.findViewById(R.id.item_name);
            TextView artistText = (TextView) view.findViewById(R.id.item_artist);
            TextView albumText = (TextView) view.findViewById(R.id.item_album);
            ImageView imageView = (ImageView) view.findViewById(R.id.item_art);

            nameText.setText(mp.Name);
            artistText.setText(TextUtils.join(", ", mp.Artists));
            albumText.setText(mp.Album);
            if (mp.ImageURI == null)
                Picasso.with(getContext()).load(mp.ImageURLShort).into(imageView);
            else
                Picasso.with(getContext()).load(mp.ImageURI).into(imageView);
        }
        return view;
    }
}
