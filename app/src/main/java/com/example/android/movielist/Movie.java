package com.example.android.movielist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rob on 11/8/2016.
 */

public class Movie implements Parcelable {

    private String movieTitle;
    private String releaseDate;
    private String posterUrl;
    private String voteAvg;
    private String plotSyn;

    public Movie(String title, String release, String poster, String vote, String plot){
        movieTitle = title;
        releaseDate = release;
        posterUrl = poster;
        voteAvg = vote;
        plotSyn = plot;
    }

    private Movie(Parcel in){
        movieTitle = in.readString();
        releaseDate = in.readString();
        posterUrl = in.readString();
        voteAvg = in.readString();
        plotSyn = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieTitle);
        parcel.writeString(releaseDate);
        parcel.writeString(posterUrl);
        parcel.writeString(voteAvg);
        parcel.writeString(plotSyn);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        public Movie createFromParcel(Parcel parcel){
            return new Movie(parcel);
        }

        public Movie[] newArray(int size){
            return new Movie[size];
        }
    };

    public String getMovieTitle(){
        return movieTitle;
    }

    public String getReleaseDate(){
        return releaseDate;
    }

    public String getPosterUrl(){
        return posterUrl;
    }

    public String getVoteAvg(){
        return voteAvg;
    }

    public String getPlotSyn(){
        return plotSyn;
    }
}
