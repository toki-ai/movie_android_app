package com.example.data.source.remote.paging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;
import androidx.paging.PagingSource.LoadResult;
import androidx.paging.PagingSource.LoadParams;

import com.example.data.mapper.MovieDtoToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.model.MovieResponse;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.Movie;
import com.example.data.preference.SettingPreference;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoviePagingSource extends RxPagingSource<Integer, Movie> {
    private static final String TAG = "MoviePagingSource";

    private final MovieApiService apiService;
    private final String apiKey;
    private final MovieDtoToMovieMapper mapper;
    private final FavoriteMovieDao favoriteDao;
    private final SettingPreference settingPreference;

    public MoviePagingSource(MovieApiService apiService, String apiKey,
                             FavoriteMovieDao favoriteDao, SettingPreference settingPreference) {
        this.apiService = apiService;
        this.apiKey = apiKey;
        this.favoriteDao = favoriteDao;
        this.mapper = new MovieDtoToMovieMapper();
        this.settingPreference = settingPreference;
        Log.d("TAGTAG", "HEHE");
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Movie>> loadSingle(@NonNull LoadParams<Integer> params) {
        Integer nextPageNumber = params.getKey();
        if (nextPageNumber == null) {
            nextPageNumber = 1;
        }

        Log.d(TAG, "loadSingle called for page: " + nextPageNumber);
        return loadMoviesForPage(nextPageNumber)
                .subscribeOn(Schedulers.io())
                .map(this::toLoadResult)
                .doOnSuccess(result -> Log.d(TAG, "LoadResult created: " + (result instanceof LoadResult.Page ? "Page" : "Error")))
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Error in loadSingle: " + throwable.getMessage());
                    return new LoadResult.Error(throwable);
                });
    }

    private Single<List<Movie>> loadMoviesForPage(int page) {
        String category = settingPreference.getCategory();
        Log.d(TAG, "Loading movies for page " + page + ", category: " + category);
        return apiService.getMoviesByCategory(category, apiKey, page)
                .flatMap(response -> {
                    int apiElementCount = response.getMovies().size();
                    Log.d(TAG, "Page " + page + ": API returned " + apiElementCount + " elements");
                    return favoriteDao.getFavoriteMovies()
                            .map(favorites -> {
                                List<Integer> favoriteIds = favorites.stream()
                                        .map(FavoriteMovieEntity::getId)
                                        .collect(Collectors.toList());

                                return response.getMovies().stream()
                                        .map(dto -> {
                                            boolean isFavorite = favoriteIds.contains(dto.getId());
                                            return mapper.map(dto, isFavorite);
                                        })
                                        .collect(Collectors.toList());
                            });
                });
    }

    private LoadResult<Integer, Movie> toLoadResult(@NonNull List<Movie> movies) {
        Integer prevKey = null;
        Integer nextKey = movies.isEmpty() ? null : 1;
        return new LoadResult.Page(
                movies, prevKey, nextKey,
                LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED
        );
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Movie> pagingState) {
        return null;
    }
}