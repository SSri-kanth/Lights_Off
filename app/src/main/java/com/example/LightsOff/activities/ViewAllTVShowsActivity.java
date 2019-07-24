package com.example.LightsOff.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.LightsOff.R;
import com.example.LightsOff.adapters.TVShowBriefsSmallAdapter;
import com.example.LightsOff.network.ApiClient;
import com.example.LightsOff.network.ApiInterface;
import com.example.LightsOff.network.tvshows.PopularTVShowsResponse;
import com.example.LightsOff.network.tvshows.TVShowBrief;
import com.example.LightsOff.network.tvshows.TopRatedTVShowsResponse;
import com.example.LightsOff.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAllTVShowsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<TVShowBrief> mTVShows;
    private TVShowBriefsSmallAdapter mTVShowsAdapter;

    private int mTVShowType;

    private boolean pagesOver = false;
    private int presentPage = 1;
    private boolean loading = true;
    private int previousTotal = 0;
    private int visibleThreshold = 5;

    private Call<PopularTVShowsResponse> mPopularTVShowsCall;
    private Call<TopRatedTVShowsResponse> mTopRatedTVShowsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_tvshows);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent receivedIntent = getIntent();
        mTVShowType = receivedIntent.getIntExtra(Constants.VIEW_ALL_TV_SHOWS_TYPE, -1);

        if (mTVShowType == -1) finish();

        switch (mTVShowType) {
            case Constants.POPULAR_TV_SHOWS_TYPE:
                setTitle(R.string.popular_tv_shows);
                break;
            case Constants.TOP_RATED_TV_SHOWS_TYPE:
                setTitle(R.string.top_rated_tv_shows);
                break;
        }

        mRecyclerView =  findViewById(R.id.recycler_view_view_all);
        mTVShows = new ArrayList<>();
        mTVShowsAdapter = new TVShowBriefsSmallAdapter(ViewAllTVShowsActivity.this, mTVShows);
        mRecyclerView.setAdapter(mTVShowsAdapter);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(ViewAllTVShowsActivity.this, 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                int visibleItemCount = gridLayoutManager.getChildCount();
                int totalItemCount = gridLayoutManager.getItemCount();
                int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                    loadTVShows(mTVShowType);
                    loading = true;
                }

            }
        });

        loadTVShows(mTVShowType);

    }

    @Override
    protected void onStart() {
        super.onStart();

        mTVShowsAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mPopularTVShowsCall != null) mPopularTVShowsCall.cancel();
        if (mTopRatedTVShowsCall != null) mTopRatedTVShowsCall.cancel();
    }

    private void loadTVShows(int tvShowType) {
        if (pagesOver) return;

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        switch (tvShowType) {
            case Constants.POPULAR_TV_SHOWS_TYPE:
                mPopularTVShowsCall = apiService.getPopularTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
                mPopularTVShowsCall.enqueue(new Callback<PopularTVShowsResponse>() {
                    @Override
                    public void onResponse(Call<PopularTVShowsResponse> call, Response<PopularTVShowsResponse> response) {
                        if (!response.isSuccessful()) {
                            mPopularTVShowsCall = call.clone();
                            mPopularTVShowsCall.enqueue(this);
                            return;
                        }

                        if (response.body() == null) return;
                        if (response.body().getResults() == null) return;

                        for (TVShowBrief tvShowBrief : response.body().getResults()) {
                            if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                                mTVShows.add(tvShowBrief);
                        }
                        mTVShowsAdapter.notifyDataSetChanged();
                        if (response.body().getPage() == response.body().getTotalPages())
                            pagesOver = true;
                        else
                            presentPage++;
                    }

                    @Override
                    public void onFailure(Call<PopularTVShowsResponse> call, Throwable t) {

                    }
                });
                break;
            case Constants.TOP_RATED_TV_SHOWS_TYPE:
                mTopRatedTVShowsCall = apiService.getTopRatedTVShows(getResources().getString(R.string.MOVIE_DB_API_KEY), presentPage);
                mTopRatedTVShowsCall.enqueue(new Callback<TopRatedTVShowsResponse>() {
                    @Override
                    public void onResponse(Call<TopRatedTVShowsResponse> call, Response<TopRatedTVShowsResponse> response) {
                        if (!response.isSuccessful()) {
                            mTopRatedTVShowsCall = call.clone();
                            mTopRatedTVShowsCall.enqueue(this);
                            return;
                        }

                        if (response.body() == null) return;
                        if (response.body().getResults() == null) return;

                        for (TVShowBrief tvShowBrief : response.body().getResults()) {
                            if (tvShowBrief != null && tvShowBrief.getName() != null && tvShowBrief.getPosterPath() != null)
                                mTVShows.add(tvShowBrief);
                        }
                        mTVShowsAdapter.notifyDataSetChanged();
                        if (response.body().getPage() == response.body().getTotalPages())
                            pagesOver = true;
                        else
                            presentPage++;
                    }

                    @Override
                    public void onFailure(Call<TopRatedTVShowsResponse> call, Throwable t) {

                    }
                });
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
