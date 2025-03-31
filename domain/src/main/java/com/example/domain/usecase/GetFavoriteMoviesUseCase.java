package com.example.domain.usecase;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

public class GetFavoriteMoviesUseCase {
    private final MovieRepository repository;

    public GetFavoriteMoviesUseCase(MovieRepository repository) {
        this.repository = repository;
    }

    public Single<List<Movie>> execute() {
        return repository.getFavoriteMovies();
    }
}