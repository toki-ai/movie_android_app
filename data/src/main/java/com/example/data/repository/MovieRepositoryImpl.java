package com.example.data.repository;

import com.example.data.mapper.FavoriteEntityToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.paging.MoviePagingSource;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MovieRepositoryImpl implements MovieRepository {
    private final MovieApiService apiService;
    private final FavoriteMovieDao favoriteDao;
    private final String apiKey;
    private final FavoriteEntityToMovieMapper entityToMovieMapper;

    public MovieRepositoryImpl(MovieApiService apiService, FavoriteMovieDao favoriteDao, String apiKey) {
        this.apiService = apiService;
        this.favoriteDao = favoriteDao;
        this.apiKey = apiKey;
        this.entityToMovieMapper = new FavoriteEntityToMovieMapper();
    }

    @Override
    public Flowable<PagingData<Movie>> getMovies() {
        Pager<Integer, Movie> pager = new Pager<>(
                new PagingConfig(20, 20, false, 20),
                () -> new MoviePagingSource(apiService, apiKey, favoriteDao)
        );
        return PagingRx.getFlowable(pager);
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
                .map(id -> null);
    }

    @Override
    public Single<Void> removeFromFavorites(Movie movie) {
        return favoriteDao.deleteFavorite(movie.getId())
                .map(count -> null);
    }
}