package com.example.LightsOff.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.LightsOff.R;
import com.example.LightsOff.adapters.TVShowCastAdapter;
import com.example.LightsOff.adapters.VideoAdapter;
import com.example.LightsOff.broadcastreceivers.ConnectivityBroadcastReceiver;
import com.example.LightsOff.network.ApiClient;
import com.example.LightsOff.network.ApiInterface;
import com.example.LightsOff.network.tvshows.Genre;
import com.example.LightsOff.network.tvshows.Network;
import com.example.LightsOff.network.tvshows.TVShow;
import com.example.LightsOff.network.tvshows.TVShowCastBrief;
import com.example.LightsOff.network.tvshows.TVShowCreditsResponse;
import com.example.LightsOff.network.videos.Video;
import com.example.LightsOff.network.videos.VideosResponse;
import com.example.LightsOff.utils.Constants;
import com.example.LightsOff.utils.Favourite;
import com.example.LightsOff.utils.NetworkConnection;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TVShowDetailActivity extends AppCompatActivity {

    private int mTVShowId;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

    private ConstraintLayout mTVShowTabLayout;
    private ImageView mPosterImageView;
    private int mPosterHeight;
    private int mPosterWidth;
    private AVLoadingIndicatorView mPosterProgressBar;
    private ImageView mBackdropImageView;
    private int mBackdropHeight;
    private int mBackdropWidth;
    private AVLoadingIndicatorView mBackdropProgressBar;
    private TextView mTitleTextView;
    private TextView mGenreTextView;
    private TextView mYearTextView;
    private ImageButton mBackImageButton;
    private ImageButton mFavImageButton;

    private LinearLayout mRatingLayout;
    private TextView mRatingTextView;

    private TextView mOverviewTextView;
    private TextView mOverviewReadMoreTextView;
    private LinearLayout mDetailsLayout;
    private TextView mDetailsTextView;

    private TextView mVideosTextView;
    private RecyclerView mVideosRecyclerView;
    private List<Video> mVideos;
    private VideoAdapter mVideosAdapter;

    private View mHorizontalLine;

    private TextView mCastTextView;
    private RecyclerView mCastRecyclerView;
    private List<TVShowCastBrief> mCasts;
    private TVShowCastAdapter mCastAdapter;


    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;
    private boolean isActivityLoaded;
    private Call<TVShow> mTVShowDetailsCall;
    private Call<VideosResponse> mVideosCall;
    private Call<TVShowCreditsResponse> mTVShowCreditsCall;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvshow_detail);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle("");

        Intent receivedIntent = getIntent();
        mTVShowId = receivedIntent.getIntExtra(Constants.TV_SHOW_ID, -1);

        if (mTVShowId == -1) finish();

        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        mAppBarLayout = findViewById(R.id.app_bar);

        mPosterWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.25);
        mPosterHeight = (mPosterWidth);
        mBackdropWidth = getResources().getDisplayMetrics().widthPixels;
        mBackdropHeight = (mBackdropWidth / 2);

        mTVShowTabLayout = findViewById(R.id.layout_toolbar_tv_show);
        mTVShowTabLayout.getLayoutParams().height = mBackdropHeight +  (mPosterHeight);

        mPosterImageView =  findViewById(R.id.image_view_poster);
        mPosterImageView.getLayoutParams().width = mPosterWidth;
        mPosterImageView.getLayoutParams().height = mPosterHeight;
        mPosterProgressBar = findViewById(R.id.progress_bar_poster);
        mPosterProgressBar.setVisibility(View.GONE);

        mBackdropImageView = findViewById(R.id.image_view_backdrop);
        mBackdropImageView.getLayoutParams().height = mBackdropHeight;
        mBackdropProgressBar = findViewById(R.id.progress_bar_backdrop);
        mBackdropProgressBar.setVisibility(View.GONE);

        mTitleTextView = findViewById(R.id.text_view_title_tv_show_detail);
        mGenreTextView = findViewById(R.id.text_view_genre_tv_show_detail);
        mYearTextView = findViewById(R.id.text_view_year_tv_show_detail);

        mBackImageButton = findViewById(R.id.image_button_back_tv_show_detail);
        mBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mFavImageButton = findViewById(R.id.image_button_fav_tv_show_detail);

        mRatingLayout = findViewById(R.id.layout_rating_tv_show_detail);
        mRatingTextView = findViewById(R.id.text_view_rating_tv_show_detail);

        mOverviewTextView = findViewById(R.id.text_view_overview_tv_show_detail);
        mOverviewReadMoreTextView = findViewById(R.id.text_view_read_more_tv_show_detail);
        mDetailsLayout = findViewById(R.id.layout_details_tv_show_detail);
        mDetailsTextView = findViewById(R.id.text_view_details_tv_show_detail);

        mVideosTextView = findViewById(R.id.text_view_trailer_tv_show_detail);
        mVideosRecyclerView = findViewById(R.id.recycler_view_trailers_tv_show_detail);
        (new LinearSnapHelper()).attachToRecyclerView(mVideosRecyclerView);
        mVideos = new ArrayList<>();
        mVideosAdapter = new VideoAdapter(TVShowDetailActivity.this, mVideos);
        mVideosRecyclerView.setAdapter(mVideosAdapter);
        mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(TVShowDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

        mHorizontalLine = findViewById(R.id.view_horizontal_line);

        mCastTextView = findViewById(R.id.text_view_cast_tv_show_detail);
        mCastRecyclerView = findViewById(R.id.recycler_view_cast_tv_show_detail);
        mCasts = new ArrayList<>();
        mCastAdapter = new TVShowCastAdapter(TVShowDetailActivity.this, mCasts);
        mCastRecyclerView.setAdapter(mCastAdapter);
        mCastRecyclerView.setLayoutManager(new LinearLayoutManager(TVShowDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));

        if (NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isActivityLoaded && !NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            mConnectivitySnackbar = Snackbar.make(mTitleTextView, R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(new ConnectivityBroadcastReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionConnected() {
                    mConnectivitySnackbar.dismiss();
                    isActivityLoaded = true;
                    loadActivity();
                    isBroadcastReceiverRegistered = false;
                    unregisterReceiver(mConnectivityBroadcastReceiver);
                }
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isActivityLoaded && NetworkConnection.isConnected(TVShowDetailActivity.this)) {
            isActivityLoaded = true;
            loadActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            isBroadcastReceiverRegistered = false;
            unregisterReceiver(mConnectivityBroadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTVShowDetailsCall != null) mTVShowDetailsCall.cancel();
        if (mVideosCall != null) mVideosCall.cancel();
        if (mTVShowCreditsCall != null) mTVShowCreditsCall.cancel();
    }

    private void loadActivity() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        mPosterProgressBar.setVisibility(View.VISIBLE);
        mBackdropProgressBar.setVisibility(View.VISIBLE);

        mTVShowDetailsCall = apiService.getTVShowDetails(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mTVShowDetailsCall.enqueue(new Callback<TVShow>() {
            @Override
            public void onResponse(Call<TVShow> call, final Response<TVShow> response) {
                if (!response.isSuccessful()) {
                    mTVShowDetailsCall = call.clone();
                    mTVShowDetailsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;

                mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (appBarLayout.getTotalScrollRange() + verticalOffset == 0) {
                            if (response.body().getName() != null)
                                mCollapsingToolbarLayout.setTitle(response.body().getName());
                            else
                                mCollapsingToolbarLayout.setTitle("");
                            mToolbar.setVisibility(View.VISIBLE);
                        } else {
                            mCollapsingToolbarLayout.setTitle("");
                            mToolbar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

                Glide.with(getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_1280 + response.body().getPosterPath())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                mPosterProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mPosterProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mPosterImageView);

                Glide.with(getApplicationContext()).load(Constants.IMAGE_LOADING_BASE_URL_1280 + response.body().getBackdropPath())
                        .asBitmap()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(new RequestListener<String, Bitmap>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                                mBackdropProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                mBackdropProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(mBackdropImageView);

                if (response.body().getName() != null)
                    mTitleTextView.setText(response.body().getName());
                else
                    mTitleTextView.setText("");

                setGenres(response.body().getGenres());

                setYear(response.body().getFirstAirDate());

                mFavImageButton.setVisibility(View.VISIBLE);
                setImageButtons(response.body().getId(), response.body().getPosterPath(), response.body().getName(), response.body().getHomepage());

                if (response.body().getVoteAverage() != null && response.body().getVoteAverage() != 0) {
                    mRatingLayout.setVisibility(View.VISIBLE);
                    mRatingTextView.setText(String.format("%.1f", response.body().getVoteAverage()));
                }

                if (response.body().getOverview() != null && !response.body().getOverview().trim().isEmpty()) {
                    mOverviewReadMoreTextView.setVisibility(View.VISIBLE);
                    mOverviewTextView.setText(response.body().getOverview());
                    mOverviewReadMoreTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOverviewTextView.setMaxLines(Integer.MAX_VALUE);
                            mDetailsLayout.setVisibility(View.VISIBLE);
                            mOverviewReadMoreTextView.setVisibility(View.GONE);
                        }
                    });
                } else {
                    mOverviewTextView.setText("");
                }

                setDetails(response.body().getFirstAirDate(), response.body().getEpisodeRunTime(), response.body().getStatus(), response.body().getOriginCountries(), response.body().getNetworks());

                setVideos();

                mHorizontalLine.setVisibility(View.VISIBLE);

                setCasts();

            }

            @Override
            public void onFailure(Call<TVShow> call, Throwable t) {

            }
        });
    }

    private void setGenres(List<Genre> genresList) {
        String genres = "";
        if (genresList != null) {
            for (int i = 0; i < genresList.size(); i++) {
                if (genresList.get(i) == null) continue;
                if (i == genresList.size() - 1) {
                    genres = genres.concat(genresList.get(i).getGenreName());
                } else {
                    genres = genres.concat(genresList.get(i).getGenreName() + ", ");
                }
            }
        }
        mGenreTextView.setText(genres);
    }

    private void setYear(String firstAirDateString) {
        if (firstAirDateString != null && !firstAirDateString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            try {
                Date firstAirDate = sdf1.parse(firstAirDateString);
                mYearTextView.setText(sdf2.format(firstAirDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            mYearTextView.setText("");
        }
    }

    private void setImageButtons(final Integer tvShowId, final String posterPath, final String tvShowName, final String homepage) {
        if (tvShowId == null) return;
        if (Favourite.isTVShowFav(TVShowDetailActivity.this, tvShowId)) {
            mFavImageButton.setTag(Constants.TAG_FAV);
            mFavImageButton.setImageResource(R.mipmap.ic_favorite_white_24dp);
        } else {
            mFavImageButton.setTag(Constants.TAG_NOT_FAV);
            mFavImageButton.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
        }
        mFavImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if ((int) mFavImageButton.getTag() == Constants.TAG_FAV) {
                    Favourite.removeTVShowFromFav(TVShowDetailActivity.this, tvShowId);
                    mFavImageButton.setTag(Constants.TAG_NOT_FAV);
                    mFavImageButton.setImageResource(R.mipmap.ic_favorite_border_white_24dp);
                } else {
                    Favourite.addTVShowToFav(TVShowDetailActivity.this, tvShowId, posterPath, tvShowName);
                    mFavImageButton.setTag(Constants.TAG_FAV);
                    mFavImageButton.setImageResource(R.mipmap.ic_favorite_white_24dp);
                }
            }
        });
    }

    private void setDetails(String firstAirDateString, List<Integer> runtime, String status, List<String> originCountries, List<Network> networks) {
        String detailsString = "";

        if (firstAirDateString != null && !firstAirDateString.trim().isEmpty()) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat sdf2 = new SimpleDateFormat("MMM d, yyyy");
            try {
                Date releaseDate = sdf1.parse(firstAirDateString);
                detailsString += sdf2.format(releaseDate) + "\n";
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            detailsString = "-\n";
        }

        if (runtime != null && !runtime.isEmpty() && runtime.get(0) != 0) {
            if (runtime.get(0) < 60) {
                detailsString += runtime.get(0) + " min(s)" + "\n";
            } else {
                detailsString += runtime.get(0) / 60 + " hr " + runtime.get(0) % 60 + " mins" + "\n";
            }
        } else {
            detailsString += "-\n";
        }

        if (status != null && !status.trim().isEmpty()) {
            detailsString += status + "\n";
        } else {
            detailsString += "-\n";
        }

        String originCountriesString = "";
        if (originCountries != null && !originCountries.isEmpty()) {
            for (String country : originCountries) {
                if (country == null || country.trim().isEmpty()) continue;
                originCountriesString += country + ", ";
            }
            if (!originCountriesString.isEmpty())
                detailsString += originCountriesString.substring(0, originCountriesString.length() - 2) + "\n";
            else
                detailsString += "-\n";
        } else {
            detailsString += "-\n";
        }

        String networksString = "";
        if (networks != null && !networks.isEmpty()) {
            for (Network network : networks) {
                if (network == null || network.getName() == null || network.getName().isEmpty())
                    continue;
                networksString += network.getName() + ", ";
            }
            if (!networksString.isEmpty())
                detailsString += networksString.substring(0, networksString.length() - 2);
            else
                detailsString += "-\n";
        } else {
            detailsString += "-\n";
        }

        mDetailsTextView.setText(detailsString);
    }

    private void setVideos() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mVideosCall = apiService.getTVShowVideos(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mVideosCall.enqueue(new Callback<VideosResponse>() {
            @Override
            public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                if (!response.isSuccessful()) {
                    mVideosCall = call.clone();
                    mVideosCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getVideos() == null) return;

                for (Video video : response.body().getVideos()) {
                    if (video != null && video.getSite() != null && video.getSite().equals("YouTube") && video.getType() != null && video.getType().equals("Trailer"))
                        mVideos.add(video);
                }
                if (!mVideos.isEmpty())
                    mVideosTextView.setVisibility(View.VISIBLE);
                mVideosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<VideosResponse> call, Throwable t) {

            }
        });
    }

    private void setCasts() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mTVShowCreditsCall = apiService.getTVShowCredits(mTVShowId, getResources().getString(R.string.MOVIE_DB_API_KEY));
        mTVShowCreditsCall.enqueue(new Callback<TVShowCreditsResponse>() {
            @Override
            public void onResponse(Call<TVShowCreditsResponse> call, Response<TVShowCreditsResponse> response) {
                if (!response.isSuccessful()) {
                    mTVShowCreditsCall = call.clone();
                    mTVShowCreditsCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getCasts() == null) return;

                for (TVShowCastBrief castBrief : response.body().getCasts()) {
                    if (castBrief != null && castBrief.getName() != null)
                        mCasts.add(castBrief);
                }

                if (!mCasts.isEmpty())
                    mCastTextView.setVisibility(View.VISIBLE);
                mCastAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<TVShowCreditsResponse> call, Throwable t) {

            }
        });
    }

}
