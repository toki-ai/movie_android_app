package com.example.domain.usecase;

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
        return movieRepository.getMovies(type);
    }
}