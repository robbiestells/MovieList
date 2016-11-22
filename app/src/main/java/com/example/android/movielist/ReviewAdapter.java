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
 * Created by rsteller on 11/22/2016.
 */

public class ReviewAdapter extends ArrayAdapter<ReviewObject> {
    public ReviewAdapter(Context context, ArrayList<ReviewObject> reviews) {
        super(context, 0, reviews);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Check if the existing view is being reused, otherwise inflate the view
        View listViewItem = convertView;
        if (listViewItem == null) {
            listViewItem = LayoutInflater.from(getContext()).inflate(R.layout.review_listview_item, parent, false);
        }

        //get the current Trailer
        ReviewObject currentReview = getItem(position);

        //attach content
        TextView contentTV = (TextView) listViewItem.findViewById(R.id.reviewContentTV);
        TextView authorTV = (TextView) listViewItem.findViewById(R.id.reviewAuthorTV);

        contentTV.setText(currentReview.getReviewContent());
        authorTV.setText("- " + currentReview.getReviewAuthor());

        return listViewItem;
    }
}

