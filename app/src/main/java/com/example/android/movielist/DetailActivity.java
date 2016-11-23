package com.example.android.movielist;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.movielist.data.FavoritesContract;
import com.example.android.movielist.data.FavoritesContract.FavoriteEntry;
import com.example.android.movielist.data.FavoritesDbHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;
import static android.R.attr.name;
import static android.R.attr.rating;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.view.View.GONE;
import static com.example.android.movielist.data.FavoritesContract.FavoriteEntry.COLUMN_MOVIE_TITLE;

/**
 * Created by Rob on 11/9/2016.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private static String BASE_URL = "https://api.themoviedb.org/3/movie/";

    private static String API = "/videos?api_key=7c14a6a8397181fb121e60bbdf0cd991";
    private static String REVIEW_API = "/reviews?api_key=7c14a6a8397181fb121e60bbdf0cd991";

    private static final int LOAD_FAVORITE = 0;

    private Uri mCurrentMovieUri;

    private TrailerAdapter mAdapter;
    private ReviewAdapter mReviewAdapter;

    private FavoritesDbHelper mHelper;

    private String movieId;

    private TextView mMovieTitleTV;
    private TextView mMoviePlotTV;
    private TextView mMovieVoteTV;
    private TextView mMovieDateTV;
    private ImageView mMoviePosterIV;
    private Button mFavoriteButton;
    private Button mUnfavoriteButton;
    private LinearLayout mFavoriteLayout;
    private LinearLayout mUnfavoriteLayout;

    private ArrayList<TrailerObject> mTrailers;
    private ArrayList<ReviewObject> mReviews;

    private ListView mTrailerLV;
    private ListView mReviewLV;

    MovieObject selectedMovie;
    String favoriteId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_detail);

        mAdapter = new TrailerAdapter(this, new ArrayList<TrailerObject>());

        mMovieTitleTV = (TextView) findViewById(R.id.movieDetailsTitle);
        mMovieDateTV = (TextView) findViewById(R.id.movieDetailsDate);
        mMoviePlotTV = (TextView) findViewById(R.id.movieDetailsPlot);
        mMovieVoteTV = (TextView) findViewById(R.id.movieDetailsVote);
        mMoviePosterIV = (ImageView) findViewById(R.id.movieDetailsPoster);
        mTrailerLV = (ListView) findViewById(R.id.trailerListView);
        mReviewLV = (ListView) findViewById(R.id.reviewListView);
        mFavoriteButton = (Button) findViewById(R.id.favoriteButton);
        mFavoriteLayout = (LinearLayout) findViewById(R.id.favoriteLayout);
        mUnfavoriteButton = (Button) findViewById(R.id.unfavoriteButton);
        mUnfavoriteLayout = (LinearLayout) findViewById(R.id.unfavoriteLayout);

        //get intent from MainActivity
        Intent intent = getIntent();
        mCurrentMovieUri = intent.getData();


        //get selected movie
        selectedMovie = (MovieObject) intent.getParcelableExtra("selectedMovie");

        //assign movie data to views
        mMovieTitleTV.setText(selectedMovie.getMovieTitle());
        mMovieDateTV.setText(selectedMovie.getReleaseDate());
        mMoviePlotTV.setText(selectedMovie.getPlotSyn());
        mMovieVoteTV.setText(selectedMovie.getVoteAvg());
        Picasso.with(this).load(selectedMovie.getPosterUrl()).into(mMoviePosterIV);

        movieId = selectedMovie.getMovieId();

        //check favorites
        checkFavorites();

        mTrailerLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                TrailerObject selectedTrailer = (TrailerObject) adapterView.getItemAtPosition(i);
                intent.setData(Uri.parse(selectedTrailer.getTrailerUrl()));
                startActivity(intent);
            }
        });

        CheckConnection();
    }

    //try creating a list of all favorite movies
    public void checkFavorites() {
        // Get the instance of the database
        mHelper = new FavoritesDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();

        //Look in database Move Id column for the selected movie's id
        String[] projection = {FavoriteEntry._ID, FavoriteEntry.COLUMN_MOVIE_ID};
        String selection = FavoriteEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = {selectedMovie.getMovieId()};

        Cursor cursor = db.query(FavoriteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        try {
            // See if cursor returns an object, if so, turn off favorite button
            if (cursor.moveToFirst()) {
                mFavoriteLayout.setVisibility(View.GONE);
                mUnfavoriteLayout.setVisibility(View.VISIBLE);
                int idColumn = cursor.getColumnIndex(FavoriteEntry._ID);
                favoriteId = cursor.getString(idColumn);
            }

        } catch (SQLiteException e) {
            Log.d("SQL Error", e.getMessage());
            return;
        } finally {
            //release all your resources
            cursor.close();
            db.close();
        }
        return;
    }

    public void FavoriteMovie_Clicked(View view) {
        //make sure all fields are filled out
//        if (TextUtils.isEmpty(mNameEditText.getText())) {
//            Toast.makeText(this, "Name Required", Toast.LENGTH_SHORT).show();
//            return;
//        }

        //get all data from fields
        String movieId = selectedMovie.getMovieId();
        String movieTitle = selectedMovie.getMovieTitle();
        String moviePlot = selectedMovie.getPlotSyn();
        String moviePoster = selectedMovie.getPosterUrl();
        String movieRelease = selectedMovie.getReleaseDate();
        String movieVote = selectedMovie.getVoteAvg();

        //put all values into ContentValues
        ContentValues values = new ContentValues();
        values.put(FavoriteEntry.COLUMN_MOVIE_ID, movieId);
        values.put(COLUMN_MOVIE_TITLE, movieTitle);
        if (null != moviePoster && moviePoster.length() > 0) {
            int endIndex = moviePoster.lastIndexOf("/");
            if (endIndex != -1) {
                String posterSuburl = moviePoster.substring(endIndex);
                values.put(FavoriteEntry.COLUMN_MOVIE_POSTER, posterSuburl);
            }
        }
        values.put(FavoriteEntry.COLUMN_MOVIE_RELEASED, movieRelease);
        values.put(FavoriteEntry.COLUMN_MOVIE_RATING, movieVote);
        values.put(FavoriteEntry.COLUMN_MOVIE_PLOT, moviePlot);

        //if new product, insert values to new row and show Toast, otherwise, update product row
        Uri newUri = getContentResolver().insert(FavoriteEntry.CONTENT_URI, values);
        if (newUri != null) {
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            mFavoriteLayout.setVisibility(GONE);
            mUnfavoriteLayout.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Error adding to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    public void UnfavoriteMovie_Clicked(View view) {
        mHelper = new FavoritesDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();

        //Look in database Move Id column for the selected movie's id
        String whereClause = FavoriteEntry.COLUMN_MOVIE_ID + "=?";
        String[] whereArgs = {selectedMovie.getMovieId()};

        db.delete(FavoriteEntry.TABLE_NAME, whereClause, whereArgs);

        mUnfavoriteLayout.setVisibility(View.GONE);
        mFavoriteLayout.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    //check to see if there's internet connection
    public void CheckConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            //build the string for the URL request with base, sort choice, and api
            String REQUEST_URL = BASE_URL + movieId + API;
            String REVIEW_URL = BASE_URL + movieId + REVIEW_API;

            //start Async task
            DetailsAsyncTask task = new DetailsAsyncTask();
            task.execute(REQUEST_URL);

            //set response to adapter
            mAdapter = new TrailerAdapter(this, new ArrayList<TrailerObject>());
            mTrailerLV.setAdapter(mAdapter);

            //start Async task for Reviews
            ReviewsAsyncTask reviewTask = new ReviewsAsyncTask();
            reviewTask.execute(REVIEW_URL);

            //set up reviews in adapter
            mReviewAdapter = new ReviewAdapter(this, new ArrayList<ReviewObject>());
            mReviewLV.setAdapter(mReviewAdapter);

        } else {
            //if no internet connection, display message
            // mGridView.setVisibility(GONE);
            // mEmptyTextView.setText(R.string.noConn);
        }
    }


    private class DetailsAsyncTask extends AsyncTask<String, Void, List<TrailerObject>> {

        @Override
        protected List<TrailerObject> doInBackground(String... urls) {
            //Create Url object
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            //get movie list with url
            List<TrailerObject> result = fetchTrailers(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<TrailerObject> trailers) {
//            mAdapter.clear();

            //add found movies to the gridview
            if (trailers != null && !trailers.isEmpty()) {
                mTrailers = new ArrayList<>();
                mTrailers.addAll(trailers);
                mAdapter.addAll(trailers);
                mTrailerLV.setVisibility(View.VISIBLE);

            } else {
                //if none found, display no movies found text
//                mEmptyTextView.setText(R.string.noMovies);
                mTrailerLV.setVisibility(GONE);
            }
        }
    }

    //create URL out of string

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "ERROR with creating URL", exception);
            return null;
        }
        return url;
    }

    //get the json string from url
    public static List<TrailerObject> fetchTrailers(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        List<TrailerObject> trailers = extractFeatureFromJson(jsonResponse);
        return trailers;
    }

    //get the movie objects from the json
    public static List<TrailerObject> extractFeatureFromJson(String trailerJson) {
        if (TextUtils.isEmpty(trailerJson)) {
            return null;
        }

        List<TrailerObject> trailers = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(trailerJson);

            JSONArray resultArray = baseJsonResponse.getJSONArray("results");

            // For each trailer in the array, add it to the ArrayList
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject currentTrailer = resultArray.getJSONObject(i);

                // Extract out data
                String youtubeTrailerKey = currentTrailer.getString("key");
                String site = currentTrailer.getString("site");

                String trailerUrl;
                if (site.equals("YouTube")) {
                    trailerUrl = "https://www.youtube.com/watch?v=" + youtubeTrailerKey;
                } else {
                    trailerUrl = null;
                }
                String number = String.valueOf(i + 1);
                // Create a Trailer object
                TrailerObject trailerObject = new TrailerObject(number, trailerUrl);
                trailers.add(trailerObject);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
        //return trailer list
        return trailers;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Http response code: " + urlConnection.getResponseCode());
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem getting json");

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private class ReviewsAsyncTask extends AsyncTask<String, Void, List<ReviewObject>> {

        @Override
        protected List<ReviewObject> doInBackground(String... urls) {
            //Create Url object
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            //get movie list with url
            List<ReviewObject> result = fetchReviews(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<ReviewObject> reviews) {
//            mAdapter.clear();

            //TODO update this with reviews
            if (reviews != null && !reviews.isEmpty()) {
                mReviews = new ArrayList<>();
                mReviews.addAll(reviews);
                mReviewAdapter.addAll(reviews);
                mReviewLV.setVisibility(View.VISIBLE);

            } else {
                //if none found, display no movies found text
//                mEmptyTextView.setText(R.string.noMovies);
                mTrailerLV.setVisibility(GONE);
            }
        }
    }

    public static List<ReviewObject> fetchReviews(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        List<ReviewObject> reviews = extractReviewsFromJson(jsonResponse);
        return reviews;
    }

    //get the movie objects from the json
    public static List<ReviewObject> extractReviewsFromJson(String reviewJson) {
        if (TextUtils.isEmpty(reviewJson)) {
            return null;
        }

        List<ReviewObject> reviews = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(reviewJson);

            JSONArray resultArray = baseJsonResponse.getJSONArray("results");

            // TODO extract reviews
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject currentReview = resultArray.getJSONObject(i);

                // Extract out data
                String reviewId = currentReview.getString("id");
                String author = currentReview.getString("author");
                String content = currentReview.getString("content");
                String url = currentReview.getString("url");

                // Create a Trailer object
                ReviewObject reviewObject = new ReviewObject(reviewId, author, content, url);
                reviews.add(reviewObject);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
        //return trailer list
        return reviews;
    }
}
