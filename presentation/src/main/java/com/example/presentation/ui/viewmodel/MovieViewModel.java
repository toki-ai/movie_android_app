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
import com.example.data.preference.SettingPreference;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MovieViewModel extends ViewModel {
    private Flowable<PagingData<Movie>> moviesFlowable;
    private final GetMoviesPagedUseCase getMoviesPagedUseCase;
    private final AddFavoriteMovieUseCase addFavoriteMovieUseCase;
    private final RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase;
    private final SettingPreference settingPreference;

    private final BehaviorSubject<String> categorySubject = BehaviorSubject.createDefault("popular");

    @Inject
    public MovieViewModel(GetMoviesPagedUseCase getMoviesPagedUseCase,
                          AddFavoriteMovieUseCase addFavoriteMovieUseCase,
                          RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase,
                          SettingPreference settingPreference) {
        this.getMoviesPagedUseCase = getMoviesPagedUseCase;
        this.addFavoriteMovieUseCase = addFavoriteMovieUseCase;
        this.removeFavoriteMovieUseCase = removeFavoriteMovieUseCase;
        this.settingPreference = settingPreference;

        moviesFlowable = categorySubject
                .toFlowable(BackpressureStrategy.LATEST)
                .switchMap(category -> {
                    Log.d("MovieViewModel", "Executing getMoviesPagedUseCase with category: " + category);
                    return getMoviesPagedUseCase.execute(category);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void refreshMovies() {
        categorySubject.onNext(settingPreference.getCategory());
    }

    public Flowable<PagingData<Movie>> getMovies() {
        return moviesFlowable;
    }
    private final MutableLiveData<Movie> favoriteChangeLiveData = new MutableLiveData<>();

    public void toggleFavorite(Movie movie) {
        boolean newFavoriteState = !movie.isFavorite();
        movie.setFavorite(newFavoriteState);
        if (newFavoriteState) {
            addFavorite(movie);
        } else {
            removeFavorite(movie);
        }
    }

    @SuppressLint("CheckResult")
    private void addFavorite(Movie movie) {
        addFavoriteMovieUseCase.execute(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d("MovieViewModel", "Add favorite succeeded: " + movie.getTitle());
                            notifyFavoriteChange(movie);
                        },
                        throwable -> Log.e("AddFavorite", "Error: " + throwable.getMessage())
                );
    }

    @SuppressLint("CheckResult")
    private void removeFavorite(Movie movie) {
        removeFavoriteMovieUseCase.execute(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Log.d("MovieViewModel", "Remove favorite succeeded: " + movie.getTitle());
                            notifyFavoriteChange(movie);
                        },
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