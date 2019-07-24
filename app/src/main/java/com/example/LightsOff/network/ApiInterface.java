package com.example.LightsOff.network;

import com.example.LightsOff.network.movies.Movie;
import com.example.LightsOff.network.movies.MovieCastsOfPersonResponse;
import com.example.LightsOff.network.movies.MovieCreditsResponse;
import com.example.LightsOff.network.movies.NowShowingMoviesResponse;
import com.example.LightsOff.network.movies.PopularMoviesResponse;
import com.example.LightsOff.network.movies.TopRatedMoviesResponse;
import com.example.LightsOff.network.people.Person;
import com.example.LightsOff.network.tvshows.PopularTVShowsResponse;
import com.example.LightsOff.network.tvshows.TVCastsOfPersonResponse;
import com.example.LightsOff.network.tvshows.TVShow;
import com.example.LightsOff.network.tvshows.TVShowCreditsResponse;
import com.example.LightsOff.network.tvshows.TopRatedTVShowsResponse;
import com.example.LightsOff.network.videos.VideosResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiInterface {

    @GET("movie/now_playing")
    Call<NowShowingMoviesResponse> getNowShowingMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region);

    @GET("movie/popular")
    Call<PopularMoviesResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region);

    @GET("movie/top_rated")
    Call<TopRatedMoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey, @Query("page") Integer page, @Query("region") String region);

    @GET("movie/{id}")
    Call<Movie> getMovieDetails(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<VideosResponse> getMovieVideos(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/credits")
    Call<MovieCreditsResponse> getMovieCredits(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("genre/movie/list")
    Call<com.example.LightsOff.network.movies.GenresList> getMovieGenresList(@Query("api_key") String apiKey);

    @GET("tv/popular")
    Call<PopularTVShowsResponse> getPopularTVShows(@Query("api_key") String apiKey, @Query("page") Integer page);

    @GET("tv/top_rated")
    Call<TopRatedTVShowsResponse> getTopRatedTVShows(@Query("api_key") String apiKey, @Query("page") Integer page);

    @GET("tv/{id}")
    Call<TVShow> getTVShowDetails(@Path("id") Integer tvShowId, @Query("api_key") String apiKey);

    @GET("tv/{id}/videos")
    Call<VideosResponse> getTVShowVideos(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("tv/{id}/credits")
    Call<TVShowCreditsResponse> getTVShowCredits(@Path("id") Integer movieId, @Query("api_key") String apiKey);

    @GET("genre/tv/list")
    Call<com.example.LightsOff.network.tvshows.GenresList> getTVShowGenresList(@Query("api_key") String apiKey);

    @GET("person/{id}")
    Call<Person> getPersonDetails(@Path("id") Integer personId, @Query("api_key") String apiKey);

    @GET("person/{id}/movie_credits")
    Call<MovieCastsOfPersonResponse> getMovieCastsOfPerson(@Path("id") Integer personId, @Query("api_key") String apiKey);

    @GET("person/{id}/tv_credits")
    Call<TVCastsOfPersonResponse> getTVCastsOfPerson(@Path("id") Integer personId, @Query("api_key") String apiKey);

}
