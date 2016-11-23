package com.example.android.movielist;

/**
 * Created by rsteller on 11/18/2016.
 */

public class TrailerObject {

    private String trailerNumber;
    private String trailerUrl;

    public TrailerObject(String number, String link) {
        trailerNumber = number;
        trailerUrl = link;
    }

    public String getTrailerNumber() {
        return trailerNumber;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }
}
