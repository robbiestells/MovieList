package com.example.android.movielist;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Rob on 11/9/2016.
 */

public class DetailActivity extends AppCompatActivity {

    private TextView mMovieTitleTV;
    private TextView mMoviePlotTV;
    private TextView mMovieVoteTV;
    private TextView mMovieDateTV;
    private ImageView mMoviePosterIV;

    MovieObject selectedMovie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        mMovieTitleTV = (TextView) findViewById(R.id.movieDetailsTitle);
        mMovieDateTV = (TextView) findViewById(R.id.movieDetailsDate);
        mMoviePlotTV = (TextView) findViewById(R.id.movieDetailsPlot);
        mMovieVoteTV = (TextView) findViewById(R.id.movieDetailsVote);
        mMoviePosterIV = (ImageView) findViewById(R.id.movieDetailsPoster);

        //get intent from MainActivity
        Intent intent = getIntent();

        //get selected movie
        selectedMovie = (MovieObject) intent.getParcelableExtra("selectedMovie");

        //assign movie data to views
        mMovieTitleTV.setText(selectedMovie.getMovieTitle());
        mMovieDateTV.setText(selectedMovie.getReleaseDate());
        mMoviePlotTV.setText(selectedMovie.getPlotSyn());
        mMovieVoteTV.setText(selectedMovie.getVoteAvg());
        Picasso.with(this).load(selectedMovie.getPosterUrl()).into(mMoviePosterIV);
    }

}
