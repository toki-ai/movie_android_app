package com.example.domain.usecase;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import io.reactivex.rxjava3.core.Single;

public class GetMovieDetailUseCase {
    private final MovieRepository repository;

    public GetMovieDetailUseCase(MovieRepository repository) {
        this.repository = repository;
    }

    public Single<Movie> execute(int movieId) {
        return repository.getMovieDetail(movieId);
    }
}