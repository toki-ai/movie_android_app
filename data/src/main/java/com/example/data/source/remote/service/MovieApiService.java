package com.example.data.source.remote.service;

import com.example.data.source.remote.model.CastCrewDto;
import com.example.data.source.remote.model.CastCrewResponse;
import com.example.data.source.remote.model.MovieDto;
import com.example.data.source.remote.model.MovieResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("movie/{category}")
    Single<MovieResponse> getMoviesByCategory(
            @Path("category") String category,
            @Query("api_key") String apiKey,
            @Query("page") int page
    );

    @GET("movie/{movieId}")
    Single<MovieDto> getMovieDetail(
            @Path("movieId") int movieId,
            @Query("api_key") String apiKey
    );

    @GET("movie/{movieId}/credits")
    Single<CastCrewResponse> getMovieCredits(
            @Path("movieId") int movieId,
            @Query("api_key") String apiKey
    );
}
