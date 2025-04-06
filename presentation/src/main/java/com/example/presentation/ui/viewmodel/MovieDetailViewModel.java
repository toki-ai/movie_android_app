package com.example.presentation.ui.viewmodel;


import androidx.lifecycle.ViewModel;

import com.example.domain.entity.Movie;
import com.example.domain.usecase.GetMovieDetailUseCase;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Single;


public class MovieDetailViewModel extends ViewModel {
    private final GetMovieDetailUseCase getMovieDetailUseCase;

    @Inject
    public MovieDetailViewModel(GetMovieDetailUseCase getMovieDetailUseCase){
        this.getMovieDetailUseCase = getMovieDetailUseCase;
    }

    public Single<Movie> getMovieDetail(int movieId) {
        return getMovieDetailUseCase.execute(movieId);
    }
}