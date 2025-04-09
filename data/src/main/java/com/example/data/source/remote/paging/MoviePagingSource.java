package com.example.data.source.remote.paging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.example.data.mapper.MovieDtoToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.model.MovieResponse;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.Movie;
import com.example.data.preference.SettingPreference;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MoviePagingSource extends RxPagingSource<Integer, Movie> {
    private static final String TAG = "MoviePagingSource";

    private final MovieApiService apiService;
    private final String apiKey;
    private final MovieDtoToMovieMapper mapper;
    private final FavoriteMovieDao favoriteDao;
    private final SettingPreference settingPreference;
    private final String query;
    private int totalPages = 0;

    public MoviePagingSource(MovieApiService apiService, String apiKey,
                             FavoriteMovieDao favoriteDao, SettingPreference settingPreference, String query) {
        this.apiService = apiService;
        this.apiKey = apiKey;
        this.favoriteDao = favoriteDao;
        this.mapper = new MovieDtoToMovieMapper();
        this.settingPreference = settingPreference;
        this.query = query;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Movie>> loadSingle(@NonNull LoadParams<Integer> params) {
        Integer currentPage = params.getKey();
        if (currentPage == null) {
            currentPage = 1;
        }

        Integer finalCurrentPage = currentPage;
        Integer finalCurrentPage1 = currentPage;
        return loadMoviesForPage(currentPage)
                .subscribeOn(Schedulers.io())
                .map(movies -> toLoadResult(movies, finalCurrentPage))
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Error loading page " + finalCurrentPage1 + ": " + throwable.getMessage());
                    return new LoadResult.Error(throwable);
                });
    }

    private Single<List<Movie>> loadMoviesForPage(int page) {
        String category = settingPreference.getCategory();
        float minRating = settingPreference.getMinRating();
        String sortBy = settingPreference.getSortBy();
        int minYear = settingPreference.getMinYear();

        Single<MovieResponse> movieResponseSingle;
        if (query != null && !query.isEmpty()) {
            Log.d(TAG, "Searching movies with query: " + query + ", page: " + page);
            movieResponseSingle = apiService.searchMovies(apiKey, query, page);
        } else {
            Log.d(TAG, "Loading movies for page " + page + ", category: " + category);
            movieResponseSingle = apiService.getMoviesByCategory(category, apiKey, page);
        }

        return movieResponseSingle
                .flatMap(response -> {
                    totalPages = response.getTotalPages();
                    Log.d(TAG, "Page " + page + ": API returned " + response.getMovies().size() + " movies, total pages: " + totalPages);
                    return favoriteDao.getFavoriteMovies()
                            .map(favorites -> {
                                List<Integer> favoriteIds = favorites.stream()
                                        .map(FavoriteMovieEntity::getId)
                                        .collect(Collectors.toList());

                                List<Movie> movies = response.getMovies().stream()
                                        .map(dto -> mapper.map(dto, favoriteIds.contains(dto.getId())))
                                        .collect(Collectors.toList());


                                movies = movies.stream()
                                        .filter(movie -> movie.getVoteAverage() >= minRating)
                                        .collect(Collectors.toList());

                                movies = movies.stream()
                                        .filter(movie -> Integer.parseInt(movie.getReleaseYear()) >= minYear)
                                        .collect(Collectors.toList());

                                Comparator<Movie> comparator;
                                if ("rating".equals(sortBy)) {
                                    comparator = Comparator.comparingDouble(Movie::getVoteAverage).reversed();
                                } else {
                                    comparator = Comparator
                                            .comparing(Movie::getReleaseDate, Comparator.nullsLast(Comparator.naturalOrder()))
                                            .reversed();
                                }

                                movies.sort(comparator);

                                Log.d(TAG, "Page " + page + ": After filter and sort, " + movies.size() + " movies remain");

                                return movies;
                            });
                });
    }

    private LoadResult<Integer, Movie> toLoadResult(@NonNull List<Movie> movies, int currentPage) {
        Integer prevKey = currentPage > 1 ? currentPage - 1 : null;
        Integer nextKey = currentPage < totalPages ? currentPage + 1 : null;

        Log.d(TAG, "Page " + currentPage + ": Loaded " + movies.size() + " movies, prevKey: " + prevKey + ", nextKey: " + nextKey);
        return new LoadResult.Page(
                movies, prevKey, nextKey,
                LoadResult.Page.COUNT_UNDEFINED, LoadResult.Page.COUNT_UNDEFINED
        );
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Movie> state) {
        Integer anchorPosition = state.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }
        LoadResult.Page<Integer, Movie> anchorPage = state.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }
        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }
        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }
        return null;
    }
}