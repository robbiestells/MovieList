package com.example.android.movielist;

/**
 * Created by rsteller on 11/22/2016.
 */

public class ReviewObject {

    private String reviewId;
    private String reviewAuthor;
    private String reviewContent;
    private String reviewUrl;

    public ReviewObject(String id, String author, String review, String link){
        reviewId = id;
        reviewAuthor = author;
        reviewContent = review;
        reviewUrl = link;
    }

    public String getReviewId(){return reviewId;}
    public String getReviewAuthor(){return reviewAuthor;}
    public String getReviewContent(){return reviewContent;}
    public String getReviewUrl(){return reviewUrl;}
}
