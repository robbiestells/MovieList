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
 * Created by rsteller on 11/18/2016.
 */

public class TrailerAdapter extends ArrayAdapter<TrailerObject> {
    public TrailerAdapter(Context context, ArrayList<TrailerObject> trailers) {
        super(context, 0, trailers);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Check if the existing view is being reused, otherwise inflate the view
        View listViewItem = convertView;
        if (listViewItem == null) {
            listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.trailer_listview_item, parent, false);
        }

        //get the current Trailer
        TrailerObject currentTrailer = getItem(position);

        //attach poster to the listView
        TextView numberTV = (TextView) listViewItem.findViewById(R.id.trailerNumberTV);
        numberTV.setText("Watch Trailer " + currentTrailer.getTrailerNumber());

        return listViewItem;
    }
}
