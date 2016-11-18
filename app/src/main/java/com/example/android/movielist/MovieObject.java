package com.example.android.movielist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rob on 11/8/2016.
 */

public class MovieObject implements Parcelable {

    private String movieTitle;
    private String releaseDate;
    private String posterUrl;
    private String voteAvg;
    private String plotSyn;
    private String movieId;

    public MovieObject(String title, String release, String poster, String vote, String plot, String id){
        movieTitle = title;
        releaseDate = release;
        posterUrl = poster;
        voteAvg = vote;
        plotSyn = plot;
        movieId = id;
    }

    private MovieObject(Parcel in){
        movieTitle = in.readString();
        releaseDate = in.readString();
        posterUrl = in.readString();
        voteAvg = in.readString();
        plotSyn = in.readString();
        movieId = in.readString();
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
        parcel.writeString(movieId);
    }

    public static final Parcelable.Creator<MovieObject> CREATOR = new Parcelable.Creator<MovieObject>(){
        public MovieObject createFromParcel(Parcel parcel){
            return new MovieObject(parcel);
        }

        public MovieObject[] newArray(int size){
            return new MovieObject[size];
        }
    };

    public String getMovieTitle(){
        return movieTitle;
    }

    public String getReleaseDate(){
        return releaseDate;
    }

    public String getPosterUrl(){
        return "http://image.tmdb.org/t/p/w500" + posterUrl;
    }

    public String getVoteAvg(){
        return voteAvg;
    }

    public String getPlotSyn(){
        return plotSyn;
    }

    public String getMovieId(){return movieId; }
}
