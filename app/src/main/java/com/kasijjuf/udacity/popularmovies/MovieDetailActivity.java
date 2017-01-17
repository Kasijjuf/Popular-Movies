package com.kasijjuf.udacity.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent intent = getIntent();
        String title = intent.getStringExtra(PosterGridFragment.EXTRA_TITLE);
        String posterUrl = intent.getStringExtra(PosterGridFragment.EXTRA_POSTER_URL);
        String backdropUrl = intent.getStringExtra(PosterGridFragment.EXTRA_BACKDROP_URL);
        String ratingStr = intent.getStringExtra(PosterGridFragment.EXTRA_RATING);
        String releaseDateStr = intent.getStringExtra(PosterGridFragment.EXTRA_RELEASE_DATE);
        String synopsisStr = intent.getStringExtra(PosterGridFragment.EXTRA_SYNOPSIS);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(title);

        // Backdrop
        ImageView backdrop = (ImageView) findViewById(R.id.backdrop);
        backdrop.setContentDescription(title);
        Picasso.with(this).load(backdropUrl).into(backdrop);

        // Poster
        ImageView poster = (ImageView) findViewById(R.id.poster);
        Picasso.with(this).load(posterUrl).into(poster);

        // Rating
        TextView rating = (TextView) findViewById(R.id.rating);
        rating.setText(ratingStr);

        // Release date
        TextView releaseDate = (TextView) findViewById(R.id.release_date);
        releaseDate.setText(releaseDateStr);

        // Synopsis
        TextView synopsis = (TextView) findViewById(R.id.synopsis);
        synopsis.setText(synopsisStr);
    }
}
