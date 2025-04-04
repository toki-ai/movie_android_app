package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.example.domain.entity.Movie;
import com.example.domain.usecase.AddFavoriteMovieUseCase;
import com.example.domain.usecase.GetMoviesPagedUseCase;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;
import com.example.data.preference.SettingPreference;
import com.example.domain.usecase.SaveUserUseCase;
import com.example.presentation.MyApplication;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.CompletableSubject;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class MovieViewModel extends ViewModel {
    private Flowable<PagingData<Movie>> moviesFlowable;
    private final GetMoviesPagedUseCase getMoviesPagedUseCase;
    private final AddFavoriteMovieUseCase addFavoriteMovieUseCase;
    private final RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase;
    private final SettingPreference settingPreference;

    private final CompletableSubject refreshTrigger = CompletableSubject.create();

    @Inject
    public MovieViewModel(GetMoviesPagedUseCase getMoviesPagedUseCase,
                          AddFavoriteMovieUseCase addFavoriteMovieUseCase,
                          RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase,
                          SettingPreference settingPreference) {
        this.getMoviesPagedUseCase = getMoviesPagedUseCase;
        this.addFavoriteMovieUseCase = addFavoriteMovieUseCase;
        this.removeFavoriteMovieUseCase = removeFavoriteMovieUseCase;
        this.settingPreference = settingPreference;

        moviesFlowable = refreshTrigger
                .toFlowable() // Convert Completable to Flowable
                .switchMap(ignored -> getMoviesPagedUseCase.execute(settingPreference.getCategory()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        PagingRx.cachedIn(moviesFlowable, ViewModelKt.getViewModelScope(this));

        refreshMovies(); // Trigger initial load
    }

    public void refreshMovies() {
        refreshTrigger.onComplete(); // Signal refresh without a value
    }
    private final MutableLiveData<Movie> favoriteChangeLiveData = new MutableLiveData<>();
    //private final PublishSubject<Void> refreshTrigger = PublishSubject.create();

    public Flowable<PagingData<Movie>> getMovies() {
        return moviesFlowable;
    }

//    public void refreshMovies() {
//        refreshTrigger.onNext(null);
//    }

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
                        result -> notifyFavoriteChange(movie),
                        throwable -> Log.e("AddFavorite", "Error: " + throwable.getMessage())
                );
    }

    @SuppressLint("CheckResult")
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