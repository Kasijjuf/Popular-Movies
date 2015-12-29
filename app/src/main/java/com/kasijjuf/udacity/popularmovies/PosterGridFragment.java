package com.kasijjuf.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class PosterGridFragment extends Fragment {

    private final String LOG_TAG = PosterGridFragment.class.getSimpleName();

    private static final int SORT_NOT_YET_SELECTED = -1;
    private static final int SORT_BY_POPULARITY = 1;
    private static final int SORT_BY_RATING = 2;

    private GridView mGridView;
    private ImageAdapter mPosterAdapter;
    private JSONArray mMoviesJsonArray;

    // Keys for Intent Extras
    static final String EXTRA_TITLE = "com.kasijjuf.udacity.popularmovies.EXTRA_TITLE";
    static final String EXTRA_POSTER_URL = "com.kasijjuf.udacity.popularmovies.EXTRA_POSTER_URL";
    static final String EXTRA_BACKDROP_URL = "com.kasijjuf.udacity.popularmovies.EXTRA_BACKDROP_URL";
    static final String EXTRA_SYNOPSIS = "com.kasijjuf.udacity.popularmovies.EXTRA_SYNOPSIS";
    static final String EXTRA_RATING = "com.kasijjuf.udacity.popularmovies.EXTRA_RATING";
    static final String EXTRA_RELEASE_DATE = "com.kasijjuf.udacity.popularmovies.EXTRA_RELEASE_DATE";

    // JSON Keys
    static final private String TMDB_JSON_RESULTS = "results";
    static final private String TMDB_JSON_TITLE = "title";
    static final private String TMDB_JSON_POSTER_PATH = "poster_path";
    static final private String TMDB_JSON_BACKDROP_PATH = "backdrop_path";
    static final private String TMDB_JSON_RATING = "vote_average";
    static final private String TMDB_JSON_RELEASE_DATE = "release_date";
    static final private String TMDB_JSON_SYNOPSIS = "overview";

    // TMDB Query URL Components
    static final private String TMDB_DISCOVER_MOVIES_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
    static final private String TMDB_POSTER_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w185/"; // FIXME Pull image resolution out of base URL
    static final private String TMDB_BACKDROP_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780/"; // FIXME Pull image resolution out of base URL
    static final private String TMDB_SORT_PARAM = "sort_by";
    static final private String TMDB_SORT_BY_MOST_POPULAR = "popularity.desc";
    static final private String TMDB_SORT_BY_HIGHEST_RATED = "vote_average.desc";
    static final private String TMDB_VOTE_COUNT_GREATER_OR_EQUAL_PARAM = "vote_count.gte";
    static final private String TMDB_VOTE_COUNT_VALUE = "700"; // Number decided by semi-arbitrary trial and error
    static final private String TMDB_API_KEY_PARAM = "api_key";
    static final private String TMDB_API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;

    public PosterGridFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // DEBUG Remove this line from final version
        Picasso.with(getContext()).setIndicatorsEnabled(true);

        mGridView = (GridView) rootView.findViewById(R.id.gridview_movies);

        /*// Might produce useful results now
        // DEBUG GridView attributes prior to assigning adapter
        Log.d(LOG_TAG, "GridView attributes prior to assigning adapter:");
        Log.d(LOG_TAG, "Actual column width is " + mGridView.getColumnWidth() + "dp");
        Log.d(LOG_TAG, "Requested column width is " + mGridView.getRequestedColumnWidth() + "dp");*/

        int posterSort = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getInt(getString(R.string.pref_poster_sort_key), SORT_NOT_YET_SELECTED); // HMMM Should this just default straight to SORT_BY_POPULARITY ?

        // TODO Maybe do more here. For now just default to SORT_BY_POPULARITY
        if (posterSort == SORT_NOT_YET_SELECTED) {
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit()
                    .putInt(getString(R.string.pref_poster_sort_key), SORT_BY_POPULARITY)
                    .commit();

            posterSort = SORT_BY_POPULARITY;
        }

        try {
            mMoviesJsonArray = new FetchSortedMoviesJsonTask().execute(posterSort).get();
            mPosterAdapter = new ImageAdapter(getActivity(), extractPosterUrlsFromJson());
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        mGridView.setAdapter(mPosterAdapter);

        /*// Might produce useful results now
        // DEBUG GridView attributes after assigning adapter
        Log.d(LOG_TAG, "GridView attributes after assigning adapter:");
        Log.d(LOG_TAG, "Actual column width is " + mGridView.getColumnWidth() + "dp");
        Log.d(LOG_TAG, "Requested column width is " + mGridView.getRequestedColumnWidth() + "dp");*/

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra(EXTRA_TITLE, extractTitleFromJson(position))
                        .putExtra(EXTRA_POSTER_URL, extractPosterUrlFromJson(position))
                        .putExtra(EXTRA_BACKDROP_URL, extractBackdropUrlFromJson(position))
                        .putExtra(EXTRA_SYNOPSIS, extractSynopsisFromJson(position))
                        .putExtra(EXTRA_RATING, extractRatingFromJson(position))
                        .putExtra(EXTRA_RELEASE_DATE, extractReleaseDateFromJson(position));
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_poster_grid, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // TODO Implement a check to see if selection is already the current setting

        switch (item.getItemId()) {
            case R.id.action_sort_by_most_popular:

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putInt(getString(R.string.pref_poster_sort_key), SORT_BY_POPULARITY)
                        .commit();

                updatePosterGrid();

                return true;

            case R.id.action_sort_by_highest_rated:

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putInt(getString(R.string.pref_poster_sort_key), SORT_BY_RATING)
                        .commit();

                updatePosterGrid();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updatePosterGrid() {
        int posterSort = PreferenceManager
                .getDefaultSharedPreferences(getActivity())
                .getInt(getString(R.string.pref_poster_sort_key), SORT_NOT_YET_SELECTED); // FIXME Add a sort param to the updatePosterGrid method

        try {
            mMoviesJsonArray = new FetchSortedMoviesJsonTask().execute(posterSort).get();
            mPosterAdapter = new ImageAdapter(getActivity(), extractPosterUrlsFromJson());
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        mGridView.setAdapter(mPosterAdapter);
    }



    // HMMM Is there a way to only accept Integer but not Integer[]
    // HMMM Is there a way to accept int instead of Integer
    public class FetchSortedMoviesJsonTask extends AsyncTask<Integer,Void,JSONArray> {

        private final String LOG_TAG = FetchSortedMoviesJsonTask.class.getSimpleName();

        @Override
        protected JSONArray doInBackground(Integer... params) {

            int sortParam = params[0];

            // Declared outside the try/catch block so
            // they can be closed in the finally block
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain raw JSON response as a string
            String rawJsonStr = null;

            try {

                Uri.Builder uriBuilder = Uri.parse(TMDB_DISCOVER_MOVIES_BASE_URL).buildUpon();

                switch (sortParam) {
                    case SORT_BY_POPULARITY:
                        uriBuilder.appendQueryParameter(TMDB_SORT_PARAM,
                                TMDB_SORT_BY_MOST_POPULAR);
                        break;

                    case SORT_BY_RATING:
                        uriBuilder.appendQueryParameter(TMDB_SORT_PARAM,
                            TMDB_SORT_BY_HIGHEST_RATED)
                                .appendQueryParameter(TMDB_VOTE_COUNT_GREATER_OR_EQUAL_PARAM,
                                        TMDB_VOTE_COUNT_VALUE);
                        break;

                    default:
                        // TODO Write some sort of "This should never happen" Toast
                }

                Uri builtUri = uriBuilder.appendQueryParameter(TMDB_API_KEY_PARAM, TMDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request and make the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                StringBuilder builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // builder for debugging.
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    // Empty stream. Nothing to parse.
                    return null;
                }
                rawJsonStr = builder.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error retrieving TMDB data");
                // If the code didn't successfully get the movie data, there's no point in
                // attempting to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) { // HMMM Why is this final?
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return new JSONObject(rawJsonStr).getJSONArray(TMDB_JSON_RESULTS);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }
    }



    // JSON Extraction Methods
    private String[] extractPosterUrlsFromJson() throws JSONException {

        String[] moviePosterUrls = new String[mMoviesJsonArray.length()];

        for (int i = 0; i < mMoviesJsonArray.length(); i++) {

            moviePosterUrls[i] = TMDB_POSTER_IMAGE_BASE_URL + mMoviesJsonArray
                    .getJSONObject(i).getString(TMDB_JSON_POSTER_PATH);
        }
        return moviePosterUrls;
    }

    private String extractReleaseDateFromJson(int index) {
        try {
            return mMoviesJsonArray.getJSONObject(index).getString(TMDB_JSON_RELEASE_DATE);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    private String extractRatingFromJson(int index) {
        try {
            return mMoviesJsonArray.getJSONObject(index).getString(TMDB_JSON_RATING);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    private String extractSynopsisFromJson(int index) {
        try {
            return mMoviesJsonArray.getJSONObject(index).getString(TMDB_JSON_SYNOPSIS);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    private String extractBackdropUrlFromJson(int index) {
        try {
            return TMDB_BACKDROP_IMAGE_BASE_URL + mMoviesJsonArray.getJSONObject(index)
                    .getString(TMDB_JSON_BACKDROP_PATH);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    private String extractPosterUrlFromJson(int index) {
        try {
            return TMDB_POSTER_IMAGE_BASE_URL + mMoviesJsonArray.getJSONObject(index)
                    .getString(TMDB_JSON_POSTER_PATH);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }

    private String extractTitleFromJson(int index) {
        try {
            return mMoviesJsonArray.getJSONObject(index).getString(TMDB_JSON_TITLE);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return null;
        }
    }
}
