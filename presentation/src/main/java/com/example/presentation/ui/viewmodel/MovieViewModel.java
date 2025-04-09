package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.PagingData;

import com.example.domain.entity.Movie;
import com.example.domain.usecase.AddFavoriteMovieUseCase;
import com.example.domain.usecase.GetMovieDetailUseCase;
import com.example.domain.usecase.GetMoviesPagedUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;
import com.example.data.preference.SettingPreference;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MovieViewModel extends ViewModel {
    private final Flowable<PagingData<Movie>> moviesFlowable;
    private final GetMoviesPagedUseCase getMoviesPagedUseCase;
    private final AddFavoriteMovieUseCase addFavoriteMovieUseCase;
    private final RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase;
    private final GetMovieDetailUseCase getMovieDetailUseCase;
    private final SettingPreference settingPreference;

    private final BehaviorSubject<String> categorySubject = BehaviorSubject.createDefault("popular");
    private final BehaviorSubject<String> querySubject = BehaviorSubject.createDefault("");
    private final MutableLiveData<Movie> favoriteChangeLiveData = new MutableLiveData<>();

    private static class Pair {
        final String first;
        final String second;

        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    @Inject
    public MovieViewModel(GetMoviesPagedUseCase getMoviesPagedUseCase,
                          AddFavoriteMovieUseCase addFavoriteMovieUseCase,
                          RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase,
                          GetMovieDetailUseCase getMovieDetailUseCase,
                          SettingPreference settingPreference) {
        this.getMoviesPagedUseCase = getMoviesPagedUseCase;
        this.addFavoriteMovieUseCase = addFavoriteMovieUseCase;
        this.removeFavoriteMovieUseCase = removeFavoriteMovieUseCase;
        this.getMovieDetailUseCase = getMovieDetailUseCase;
        this.settingPreference = settingPreference;

        moviesFlowable = BehaviorSubject.combineLatest(categorySubject, querySubject, (category, query) -> new Pair(category, query))
                .toFlowable(BackpressureStrategy.LATEST)
                .switchMap(pair -> {
                    String category = pair.first;
                    String query = pair.second;
                    Log.d("MovieViewModel", "Executing getMoviesPagedUseCase with category: " + category + ", query: " + query);
                    return getMoviesPagedUseCase.execute(category, query);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void refreshMovies() {
        categorySubject.onNext(settingPreference.getCategory());
        querySubject.onNext("");
    }

    public void searchMovies(String query) {
        querySubject.onNext(query != null ? query : "");
    }

    public Flowable<PagingData<Movie>> getMovies() {
        return moviesFlowable;
    }

    public Single<Movie> getMovieDetail(int movieId) {
        return getMovieDetailUseCase.execute(movieId);
    }

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
                        () -> notifyFavoriteChange(movie),
                        throwable -> Log.e("AddFavorite", "Error: " + throwable.getMessage())
                );
    }

    @SuppressLint("CheckResult")
    private void removeFavorite(Movie movie) {
        removeFavoriteMovieUseCase.execute(movie)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> notifyFavoriteChange(movie),
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