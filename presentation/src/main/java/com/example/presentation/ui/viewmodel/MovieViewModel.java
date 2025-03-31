package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;

import com.example.domain.entity.Movie;
import com.example.domain.usecase.AddFavoriteMovieUseCase;
import com.example.domain.usecase.GetMoviesPagedUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

public class MovieViewModel extends ViewModel {
    private final Flowable<PagingData<Movie>> moviesFlowable;
    private final AddFavoriteMovieUseCase addFavoriteMovieUseCase;
    private final RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase;
    private final GetMoviesPagedUseCase getMoviesPagedUseCase;

    private final MutableLiveData<Movie> favoriteChangeLiveData = new MutableLiveData<>();

    public MovieViewModel(GetMoviesPagedUseCase getMoviesPagedUseCase,
                          AddFavoriteMovieUseCase addFavoriteMovieUseCase,
                          RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase) {
        this.getMoviesPagedUseCase = getMoviesPagedUseCase;
        this.addFavoriteMovieUseCase = addFavoriteMovieUseCase;
        this.removeFavoriteMovieUseCase = removeFavoriteMovieUseCase;

        moviesFlowable = getMoviesPagedUseCase.execute();
        androidx.paging.rxjava3.PagingRx.cachedIn(moviesFlowable, androidx.lifecycle.ViewModelKt.getViewModelScope(this));
    }

    public Flowable<PagingData<Movie>> getMovies() {
        return moviesFlowable;
    }

    public void toggleFavorite(Movie movie) {
        boolean newFavoriteState = !movie.isFavorite();
        movie.setFavorite(newFavoriteState);
        notifyFavoriteChange(movie);
        if (newFavoriteState) {
            addFavorite(movie);
        } else {
            removeFavorite(movie);
        }
    }

    private void addFavorite(Movie movie) {
        addFavoriteMovieUseCase.execute(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> notifyFavoriteChange(movie),
                        throwable -> Log.e("AddFavorite", "Error: " + throwable.getMessage())
                );
    }

    private void removeFavorite(Movie movie) {
        removeFavoriteMovieUseCase.execute(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> notifyFavoriteChange(movie),
                        throwable -> Log.e("RemoveFavorite", "Error: " + throwable.getMessage())
                );
    }

    private void notifyFavoriteChange(Movie movie) {
        favoriteChangeLiveData.setValue(movie);
    }

    public LiveData<Movie> getFavoriteChangeLiveData() {
        return favoriteChangeLiveData;
    }
}