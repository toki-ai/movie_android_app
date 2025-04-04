package com.example.data.source.remote.paging;

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
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Movie>> loadSingle(@NonNull LoadParams<Integer> params) {
        // Start at page 1 if not defined.
        Integer nextPageNumber = params.getKey();
        if (nextPageNumber == null) {
            nextPageNumber = 1;
        }

        return loadMoviesForPage(nextPageNumber)
                .subscribeOn(Schedulers.io())
                .map(this::toLoadResult)  // Convert response to LoadResult
                .onErrorReturn(LoadResult.Error::new);  // Handle errors
    }

    private Single<List<Movie>> loadMoviesForPage(int page) {
        String category = settingPreference.getCategory();
        return apiService.getMoviesByCategory(category, apiKey, page)
                .flatMap(response -> favoriteDao.getFavoriteMovies()
                        .map(favorites -> {
                            List<Integer> favoriteIds = favorites.stream()
                                    .map(FavoriteMovieEntity::getId)
                                    .collect(Collectors.toList());

                            return response.getMovies().stream()
                                    .map(dto -> {
                                        boolean isFavorite = favoriteIds.contains(dto.getId());
                                        return mapper.map(dto, isFavorite);
                                    })
                                    .filter(movie -> {
                                        try {
                                            int year = Integer.parseInt(movie.getReleaseDate().substring(0, 4));
                                            return year >= settingPreference.getMinYear() &&
                                                    year <= settingPreference.getMaxYear();
                                        } catch (Exception e) {
                                            return false;
                                        }
                                    })
                                    .filter(movie -> movie.getVoteAverage() >= settingPreference.getMinRating())
                                    .collect(Collectors.toList());
                        })
                );
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
