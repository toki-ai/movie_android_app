package com.example.presentation.ui.viewmodel;

import android.annotation.SuppressLint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.domain.entity.Movie;
import com.example.domain.usecase.GetFavoriteMoviesUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;
import com.example.presentation.di.MyApplication;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FavoriteViewModel extends ViewModel {
    private final GetFavoriteMoviesUseCase getFavoriteMoviesUseCase;
    private final MutableLiveData<List<Movie>> favoriteMoviesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> favoriteCountLiveData = new MutableLiveData<>();
    @Inject
    public FavoriteViewModel(GetFavoriteMoviesUseCase getFavoriteMoviesUseCase,
                             RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase) {
        this.getFavoriteMoviesUseCase = getFavoriteMoviesUseCase;
        MyApplication.getAppComponent().inject(this);
        loadFavoriteMovies();
    }

    @SuppressLint("CheckResult")
    public void loadFavoriteMovies() {
        getFavoriteMoviesUseCase.execute()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        movies -> {
                            favoriteMoviesLiveData.setValue(movies);
                            favoriteCountLiveData.postValue(movies.size());
                        },
                        throwable -> {
                            favoriteCountLiveData.postValue(0);
                        }
                );
    }

    public LiveData<List<Movie>> getFavoriteMovies() {
        return favoriteMoviesLiveData;
    }

    public LiveData<Integer> getFavoriteCountLiveData() {
        return favoriteCountLiveData;
    }
}