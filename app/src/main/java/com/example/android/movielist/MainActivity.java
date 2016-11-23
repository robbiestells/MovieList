package com.example.android.movielist;

import android.app.Activity;
import android.content.ContentUris;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.movielist.data.FavoritesContract;
import com.example.android.movielist.data.FavoritesContract.FavoriteEntry;
import com.example.android.movielist.data.FavoritesCursorAdapter;
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

import static android.R.attr.data;
import static android.R.attr.id;
import static android.os.Build.VERSION_CODES.M;
import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //base URL to build API calls
    //sample url http://api.themoviedb.org/3/movie/popular?api_key=7c14a6a8397181fb121e60bbdf0cd991
    private static String BASE_URL = "http://api.themoviedb.org/3/movie/";

    private static String API = "?api_key=7c14a6a8397181fb121e60bbdf0cd991";

    private MovieAdapter mAdapter;

    private TextView mEmptyTextView;

    private GridView mGridView;

    private ArrayList<MovieObject> mMovieObjectList;

    FavoritesDbHelper mHelper;

    private String sortBy;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //when item is selected from menu, start getting movie list
        if (id == R.id.action_popularity) {
            sortBy = getString(R.string.popular);
            CheckConnection();
        } else if (id == R.id.action_top) {
            sortBy = getString(R.string.top_rated);
            CheckConnection();
        } else if (id == R.id.action_favorite) {
            mAdapter.clear();
            sortBy = getString(R.string.favorite);
            getFavoriteMovies();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyTextView = (TextView) findViewById(R.id.empty_text_view);
        mGridView = (GridView) findViewById(R.id.movieGridView);

        mAdapter = new MovieAdapter(this, new ArrayList<MovieObject>());

        //check to see if movies have already been found
        if (savedInstanceState == null || !savedInstanceState.containsKey("key")) {
            //display empty message if there's no saved array
            mEmptyTextView.setText(R.string.noMovies);
            mGridView.setVisibility(GONE);
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("selection")) {
            sortBy = getString(R.string.popular);
            //start process of loading movies
            CheckConnection();
        } else {
            if (savedInstanceState.getString("selection").equals(getString(R.string.popular))) {
                sortBy = getString(R.string.popular);
                CheckConnection();
            } else if (savedInstanceState.getString("selection").equals(getString(R.string.top_rated))) {
                sortBy = getString(R.string.top_rated);
                CheckConnection();
            } else {
                mAdapter.clear();
                sortBy = getString(R.string.favorite);
                getFavoriteMovies();
            }
        }

        //set click listener for the gridview and pass Movie object to details page
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                MovieObject selectedMovie = (MovieObject) adapterView.getItemAtPosition(i);
                intent.putExtra("selectedMovie", selectedMovie);

                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovieObjectList != null) {
            outState.putParcelableArrayList("key", mMovieObjectList);
            outState.putString("selection", sortBy);
            super.onSaveInstanceState(outState);
        }
    }

    //check to see if there's internet connection
    public void CheckConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            //build the string for the URL request with base, sort choice, and api
            String REQUEST_URL = BASE_URL + sortBy + API;

            //start Async task
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute(REQUEST_URL);

            //set response to adapter
            mAdapter = new MovieAdapter(this, new ArrayList<MovieObject>());
            mGridView.setAdapter(mAdapter);
        } else {
            //if no internet connection, display message
            mGridView.setVisibility(GONE);
            mEmptyTextView.setText(R.string.noConn);
        }
    }

    private class MovieAsyncTask extends AsyncTask<String, Void, List<MovieObject>> {

        @Override
        protected List<MovieObject> doInBackground(String... urls) {
            //Create Url object
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            //get movie list with url
            List<MovieObject> result = fetchMovies(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<MovieObject> movies) {
            mAdapter.clear();

            //add found movies to the gridview
            if (movies != null && !movies.isEmpty()) {
                mMovieObjectList = new ArrayList<>();
                mMovieObjectList.addAll(movies);
                mAdapter.addAll(movies);
                mGridView.setVisibility(View.VISIBLE);
            } else {
                //if none found, display no movies found text
                mEmptyTextView.setText(R.string.noMovies);
                mGridView.setVisibility(GONE);
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

    //get the json response from url
    public static List<MovieObject> fetchMovies(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }
        List<MovieObject> movies = extractFeatureFromJson(jsonResponse);
        return movies;
    }

    //get the movie objects from the json
    public static List<MovieObject> extractFeatureFromJson(String movieJson) {
        if (TextUtils.isEmpty(movieJson)) {
            return null;
        }

        List<MovieObject> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(movieJson);

            JSONArray resultArray = baseJsonResponse.getJSONArray("results");

            // For each movie in the array, create a Movie object and add it to the ArrayList
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject currentMovie = resultArray.getJSONObject(i);

                // Extract out data
                String title = currentMovie.getString("title");
                String release = currentMovie.getString("release_date");
                String plot = currentMovie.getString("overview");
                String poster = currentMovie.getString("poster_path");
                String rating = currentMovie.getString("vote_average");
                String movieId = currentMovie.getString("id");

                // Create a new Movie object
                MovieObject movieObject = new MovieObject(title, release, poster, rating, plot, movieId);

                //add movies to list
                movies.add(movieObject);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
        //return movie list
        return movies;
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

    //try creating a list of all favorite movies
    public void getFavoriteMovies() {
        // Get the instance of the database
        mHelper = new FavoritesDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();

        //get the cursor
        String[] projection = {
                FavoriteEntry._ID,
                FavoriteEntry.COLUMN_MOVIE_ID,
                FavoriteEntry.COLUMN_MOVIE_PLOT,
                FavoriteEntry.COLUMN_MOVIE_POSTER,
                FavoriteEntry.COLUMN_MOVIE_RATING,
                FavoriteEntry.COLUMN_MOVIE_RELEASED,
                FavoriteEntry.COLUMN_MOVIE_TITLE
        };

        //query database for favorites
        Cursor cursor = db.query(FavoriteEntry.TABLE_NAME, projection, null, null, null, null, null);

        //ImageView posterIV = (ImageView) findViewById(R.id.posterImage);

        List<MovieObject> movieList = new ArrayList<MovieObject>();

        try {
            // loop through all rows and add to list
            if (cursor.moveToFirst()) {
                do {
                    //get column indexes
                    int nameColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_TITLE);
                    int plotColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_PLOT);
                    int posterColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_POSTER);
                    int ratingColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_RATING);
                    int releaseColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_RELEASED);
                    int idColumnIndex = cursor.getColumnIndex(FavoriteEntry.COLUMN_MOVIE_ID);

                    //get data
                    String name = cursor.getString(nameColumnIndex);
                    String plot = cursor.getString(plotColumnIndex);
                    String poster = cursor.getString(posterColumnIndex);
                    String rating = cursor.getString(ratingColumnIndex);
                    String release = cursor.getString(releaseColumnIndex);
                    String id = cursor.getString(idColumnIndex);

                    // Create a new Movie object
                    MovieObject movieObject = new MovieObject(name, release, poster, rating, plot, id);

                    // Adding movie to list
                    movieList.add(movieObject);
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {
            Log.d("SQL Error", e.getMessage());
            return;
        } finally {
            //release all  resources
            cursor.close();
            db.close();
        }

        //add found movies to the gridview
        mAdapter = new MovieAdapter(this, new ArrayList<MovieObject>());
        mGridView.setAdapter(mAdapter);
        mAdapter.clear();

        if (movieList != null && !movieList.isEmpty()) {
            mMovieObjectList = new ArrayList<>();
            mMovieObjectList.addAll(movieList);
            mAdapter.addAll(movieList);
            mGridView.setVisibility(View.VISIBLE);
        } else {
            //if none found, display no movies found text
            mEmptyTextView.setText(R.string.noFavorites);
            mGridView.setVisibility(GONE);
        }

        return;
    }
}
