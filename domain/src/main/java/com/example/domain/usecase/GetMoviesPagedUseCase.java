package com.example.domain.usecase;

import androidx.paging.PagingData;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import io.reactivex.rxjava3.core.Flowable;

public class GetMoviesPagedUseCase {
    private final MovieRepository repository;

    public GetMoviesPagedUseCase(MovieRepository repository) {
        this.repository = repository;
    }

    public Flowable<PagingData<Movie>> execute() {
        return repository.getMovies();
    }
}