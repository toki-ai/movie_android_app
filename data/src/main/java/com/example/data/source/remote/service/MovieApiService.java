package com.example.data.source.remote.service;


import com.example.data.source.remote.model.MovieResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("movie/popular")
    Single<MovieResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int page);
}
