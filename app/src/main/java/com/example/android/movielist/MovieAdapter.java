package com.example.android.movielist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Rob on 11/8/2016.
 */

public class MovieAdapter extends ArrayAdapter<MovieObject> {
    public MovieAdapter(Context context, ArrayList<MovieObject> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //get the current movie
        MovieObject currentMovieObject = getItem(position);

        //Check if the existing view is being reused, otherwise inflate the view
        View gridItemView = convertView;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(getContext()).inflate(R.layout.gridview_item, parent, false);
        }

        //attach poster to the gridview
        ImageView posterIV = (ImageView) gridItemView.findViewById(R.id.posterImage);
        Picasso.with(getContext()).load(currentMovieObject.getPosterUrl()).into(posterIV);

        return gridItemView;
    }
}
