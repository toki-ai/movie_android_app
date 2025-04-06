package com.example.domain.repository;

import com.example.domain.entity.Movie;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import androidx.paging.PagingData;

import java.util.List;

public interface MovieRepository {
    Flowable<PagingData<Movie>> getMovies(String type);
    Single<List<Movie>> getFavoriteMovies();
    Completable addToFavorites(Movie movie);
    Completable removeFromFavorites(Movie movie);

    Single<Movie> getMovieDetail(int movieId);
    //Completable addMovieToFavorite(Movie movie);
    //
    //    Completable deleteMovieById(int movieId);
    //
    //    Single<Boolean> isFavoriteMovie(int movieId);
}