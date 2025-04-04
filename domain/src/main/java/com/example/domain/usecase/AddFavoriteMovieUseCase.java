package com.example.domain.usecase;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class AddFavoriteMovieUseCase {
    private final MovieRepository repository;

    public AddFavoriteMovieUseCase(MovieRepository repository) {
        this.repository = repository;
    }

    public Completable execute(Movie movie) {
        return repository.addToFavorites(movie);
    }
}