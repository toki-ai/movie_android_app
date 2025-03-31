package com.example.domain.repository;

import com.example.domain.entity.Movie;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import androidx.paging.PagingData;

import java.util.List;

public interface MovieRepository {
    Flowable<PagingData<Movie>> getMovies();
    Single<List<Movie>> getFavoriteMovies();
    Single<Void> addToFavorites(Movie movie);
    Single<Void> removeFromFavorites(Movie movie);
}