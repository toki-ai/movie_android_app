package com.example.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.example.data.mapper.FavoriteEntityToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.paging.MoviePagingSource;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;
import com.example.data.preference.SettingPreference;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MovieRepositoryImpl implements MovieRepository {
    private final MovieApiService apiService;
    private final FavoriteMovieDao favoriteDao;
    private final String apiKey;
    private final FavoriteEntityToMovieMapper entityToMovieMapper;
    private final SettingPreference settingPreference;
    private final BehaviorSubject<String> categorySubject;
    private final Flowable<PagingData<Movie>> pagingDataFlowable;

    public MovieRepositoryImpl(MovieApiService apiService, FavoriteMovieDao favoriteDao, String apiKey, Context context) {
        this.apiService = apiService;
        this.favoriteDao = favoriteDao;
        this.apiKey = apiKey;
        this.entityToMovieMapper = new FavoriteEntityToMovieMapper();
        this.settingPreference = new SettingPreference(context);
        this.categorySubject = BehaviorSubject.createDefault(settingPreference.getCategory());

        pagingDataFlowable = categorySubject
                .toFlowable(BackpressureStrategy.LATEST)
                .switchMap(category -> {
                    Log.d("MovieRepository", "SwitchMap triggered for category: " + category);
                    Pager<Integer, Movie> pager = new Pager<>(
                            new PagingConfig(20),
                            () -> new MoviePagingSource(apiService, apiKey, favoriteDao, settingPreference)
                    );
                    return PagingRx.getFlowable(pager);
                });
    }

    @Override
    public Flowable<PagingData<Movie>> getMovies(String type) {
        settingPreference.setCategory(type);
        categorySubject.onNext(type);
        return pagingDataFlowable;
    }

    @Override
    public Single<List<Movie>> getFavoriteMovies() {
        return favoriteDao.getFavoriteMovies()
                .map(entities -> {
                    List<Movie> movies = new ArrayList<>();
                    for (FavoriteMovieEntity entity : entities) {
                        movies.add(entityToMovieMapper.map(entity));
                    }
                    return movies;
                });
    }

    @Override
    public Single<Void> addToFavorites(Movie movie) {
        FavoriteMovieEntity entity = new FavoriteMovieEntity(
                movie.getId(),
                movie.isAdult(),
                movie.getBackdropPath(),
                movie.getVoteAverage(),
                movie.getReleaseDate(),
                movie.getOverview(),
                movie.getPosterPath(),
                movie.getTitle()
        );
        return favoriteDao.insertFavorite(entity)
                .map(result -> {
                    if (result > 0) {
                        movie.setFavorite(true);
                        return null;
                    }
                    throw new Exception("Failed to add to favorites");
                });
    }

    @Override
    public Single<Void> removeFromFavorites(Movie movie) {
        return favoriteDao.deleteFavorite(movie.getId())
                .map(result -> {
                    if (result > 0) {
                        movie.setFavorite(false);
                        return null;
                    }
                    throw new Exception("Failed to remove from favorites");
                });
    }
}