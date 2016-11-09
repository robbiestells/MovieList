package com.example.android.movielist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
        //Check if the existing view is being reused, otherwise inflate the view
        View gridItemView = convertView;
        if (gridItemView == null){
            gridItemView = LayoutInflater.from(getContext()).inflate(R.layout.gridview_item, parent, false);
        }

        //get the current movie
        MovieObject currentMovieObject = getItem(position);

        //attach info to the gridview
        TextView titleTV = (TextView) gridItemView.findViewById(R.id.movieTitle);
        TextView dateTV = (TextView) gridItemView.findViewById(R.id.releaseDate);

        titleTV.setText(currentMovieObject.getMovieTitle());
        dateTV.setText(currentMovieObject.getReleaseDate());

        return gridItemView;
    }
}
