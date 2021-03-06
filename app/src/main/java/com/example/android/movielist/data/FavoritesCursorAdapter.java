package com.example.android.movielist.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.android.movielist.R;
import com.example.android.movielist.data.FavoritesContract.FavoriteEntry;
import com.squareup.picasso.Picasso;

/**
 * Created by rsteller on 11/21/2016.
 */

public class FavoritesCursorAdapter extends CursorAdapter {

    public FavoritesCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //Create a new view from the gridview_item layout
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.gridview_item, viewGroup, false);
    }

    //add data to view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //get row Id
        final int itemId = cursor.getInt(cursor.getColumnIndex(FavoriteEntry._ID));

        //find views
        final ImageView ivPoster = (ImageView) view.findViewById(R.id.posterImage);

        //find columns in table
        int imageColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_POSTER);

        //get data from table
        String image = cursor.getString(imageColumnIndex);

        //assign data to views
        Picasso.with(context).load(image).into(ivPoster);
    }
}
