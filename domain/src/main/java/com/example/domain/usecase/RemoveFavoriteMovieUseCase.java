package com.example.domain.usecase;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;
import io.reactivex.rxjava3.core.Single;

public class RemoveFavoriteMovieUseCase {
    private final MovieRepository repository;

    public RemoveFavoriteMovieUseCase(MovieRepository repository) {
        this.repository = repository;
    }

    public Single<Void> execute(Movie movie) {
        return repository.removeFromFavorites(movie);
    }
}