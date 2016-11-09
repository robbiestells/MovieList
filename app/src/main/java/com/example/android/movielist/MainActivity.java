package com.example.android.movielist;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

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

    private String sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEmptyTextView = (TextView) findViewById(R.id.empty_text_view);
        //get reference for spinner
        Spinner spinner = (Spinner) findViewById(R.id.sortSpinner);
        //set listener
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        mGridView = (GridView) findViewById(R.id.movieGridView);

        mAdapter = new MovieAdapter(this, new ArrayList<MovieObject>());
        if (savedInstanceState == null || !savedInstanceState.containsKey("key")) {
            //display nothing if there's no saved array
            mEmptyTextView.setText(R.string.noMovies);
            mGridView.setVisibility(GONE);
        } else {
            mMovieObjectList = savedInstanceState.getParcelableArrayList("key");
            mAdapter.addAll(mMovieObjectList);
            mGridView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovieObjectList != null) {
           outState.putParcelableArrayList("key", mMovieObjectList);
            super.onSaveInstanceState(outState);
        }
    }

    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long l) {
            //find which item was selected
            String selectedItem = parent.getItemAtPosition(pos).toString();
            sortBy = selectedItem.toLowerCase();
            CheckConnection();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            //do nothing
        }
    }
    public void CheckConnection(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){

            //build the string for the URL request with base, sort choice, and api
            String REQEST_URL = BASE_URL + sortBy + API;

            //start Async task
            MovieAsyncTask task = new MovieAsyncTask();
            task.execute(REQEST_URL);

            //set response to adapter
            mAdapter = new MovieAdapter(this, new ArrayList<MovieObject>());
            mGridView.setAdapter(mAdapter);
        } else {
            //if no internet connection, display message
            mGridView.setVisibility(GONE);
            mEmptyTextView.setText(R.string.noConn);
        }
    }

    private class MovieAsyncTask extends AsyncTask<String, Void, List<MovieObject>>{

        @Override
        protected List<MovieObject> doInBackground(String... urls) {
            //Create Url object
            if (urls.length < 1 || urls[0] == null){
                return null;
            }

            List<MovieObject> result = fetchMovies(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<MovieObject> movies) {
            mAdapter.clear();

            //add found movies to the gridview
            if (movies != null && !movies.isEmpty()){
                mMovieObjectList = new ArrayList<>();
                mMovieObjectList.addAll(movies);
                mAdapter.addAll(movies);
                mGridView.setVisibility(View.VISIBLE);
            } else{
                //display no movies found text
                mEmptyTextView.setText(R.string.noMovies);
                mGridView.setVisibility(GONE);
            }
        }
    }

    private static URL createUrl(String stringUrl){
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception){
            Log.e(LOG_TAG, "ERROR with creating URL", exception);
            return null;
        }
        return null;
    }

    public static List<MovieObject> fetchMovies(String requestUrl){
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

    public static List<MovieObject> extractFeatureFromJson(String movieJson) {
        if (TextUtils.isEmpty(movieJson)) {
            return null;
        }

        List<MovieObject> movies = new ArrayList<>();

        try {
            JSONObject baseJsonResponse = new JSONObject(movieJson);

            JSONArray resutlArray = baseJsonResponse.getJSONArray("results");

            // For each movie in the array, create a Book object and add it to the ArrayList
            for (int i = 0; i < resutlArray.length(); i++) {
                JSONObject currentMovie = resutlArray.getJSONObject(i);

//                JSONObject title = currentBook.getJSONObject("title");
                // Extract out data
                String title = currentMovie.getString("title");
                String release = currentMovie.getString("release_date");
                String plot = currentMovie.getString("overview");
                String poster = currentMovie.getString("poster_path");
                String rating = currentMovie.getString("vote_average");

                // Create a new MovieObject object
                MovieObject movieObject = new MovieObject(title, release, poster, rating, plot);
                movies.add(movieObject);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
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
}
