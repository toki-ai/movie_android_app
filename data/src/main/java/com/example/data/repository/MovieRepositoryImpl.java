package com.example.data.repository;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.example.data.mapper.CastCrewDtoToCastCrewMapper;
import com.example.data.mapper.FavoriteEntityToMovieMapper;
import com.example.data.mapper.MovieDtoToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.model.CastCrewDto;
import com.example.data.source.remote.paging.MoviePagingSource;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.CastCrew;
import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;
import com.example.data.preference.SettingPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class MovieRepositoryImpl implements MovieRepository {
    private final MovieApiService apiService;
    private final FavoriteMovieDao favoriteDao;
    private final String apiKey;
    private final FavoriteEntityToMovieMapper entityToMovieMapper;
    private final CastCrewDtoToCastCrewMapper dtoToCastCrewMapper;
    private final MovieDtoToMovieMapper dtoToMovieMapper;
    private final SettingPreference settingPreference;

    public MovieRepositoryImpl(MovieApiService apiService, FavoriteMovieDao favoriteDao, String apiKey, Context context) {
        this.apiService = apiService;
        this.favoriteDao = favoriteDao;
        this.apiKey = apiKey;
        this.entityToMovieMapper = new FavoriteEntityToMovieMapper();
        this.dtoToMovieMapper = new MovieDtoToMovieMapper();
        this.dtoToCastCrewMapper = new CastCrewDtoToCastCrewMapper();
        this.settingPreference = new SettingPreference(context);
    }

    @Override
    public Flowable<PagingData<Movie>> getMovies(String type, String query) {
        if (type != null && !type.isEmpty()) {
            settingPreference.setCategory(type);
        }
        int pagesPerLoad = settingPreference.getPagesPerLoad();
        int itemsPerPage = 20;
        int pageSize = pagesPerLoad * itemsPerPage;

        Pager<Integer, Movie> pager = new Pager<>(
                new PagingConfig(pageSize),
                () -> new MoviePagingSource(apiService, apiKey, favoriteDao, settingPreference, query)
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
    public Completable addToFavorites(Movie movie) {
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
                .flatMapCompletable(result -> {
                    if (result > 0) {
                        movie.setFavorite(true);
                        return Completable.complete();
                    }
                    return Completable.error(new Exception("Failed to add to favorites"));
                });
    }

    @Override
    public Completable removeFromFavorites(Movie movie) {
        return favoriteDao.deleteFavorite(movie.getId())
                .flatMapCompletable(result -> {
                    if (result > 0) {
                        movie.setFavorite(false);
                        return Completable.complete();
                    }
                    return Completable.error(new Exception("Failed to remove from favorites"));
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public Single<Movie> getMovieDetail(int movieId) {
        return Single.zip(
                apiService.getMovieDetail(movieId, apiKey),
                apiService.getMovieCredits(movieId, apiKey),
                (detail, credits) -> {
                    List<CastCrew> castList = new ArrayList<>();
                    List<CastCrew> crewList = new ArrayList<>();
                    for (CastCrewDto cast : credits.getCast()) {
                        castList.add(dtoToCastCrewMapper.mapCast(cast));
                    }
                    for (CastCrewDto crew : credits.getCrew()) {
                        crewList.add(dtoToCastCrewMapper.mapCrew(crew));
                    }
                    AtomicBoolean isFavorite = new AtomicBoolean(false);
                    getFavoriteMovies()
                            .subscribe(favoriteMovies -> {
                                for (Movie m : favoriteMovies) {
                                    if (m.getId() == detail.getId()) {
                                        isFavorite.set(true);
                                    }
                                }
                            });

                    return dtoToMovieMapper.mapDetail(detail, isFavorite.get(), castList, crewList);
                }
        );
    }
}