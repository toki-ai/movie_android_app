package com.example.domain.usecase;

import android.util.Log;

import androidx.paging.PagingData;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import io.reactivex.rxjava3.core.Flowable;

public class GetMoviesPagedUseCase {
    private final MovieRepository movieRepository;

    public GetMoviesPagedUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Flowable<PagingData<Movie>> execute(String type) {
        Log.d("KKKK", "lll");
        return movieRepository.getMovies(type);
    }
}