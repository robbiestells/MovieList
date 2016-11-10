package com.example.android.movielist;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Rob on 11/9/2016.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mMovieTitleTV;

    MovieObject selectedMovie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        mMovieTitleTV = (TextView) findViewById(R.id.movieDetailsTitle);

        //get intent from MainActivity
       Intent intent = getIntent();
       selectedMovie = (MovieObject) intent.getParcelableExtra("selectedMovie");

        mMovieTitleTV.setText(selectedMovie.getMovieTitle());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
