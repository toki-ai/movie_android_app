package com.example.data.source.remote.paging;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import com.example.data.mapper.MovieDtoToMovieMapper;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.data.source.remote.model.MovieDto;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.entity.Movie;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MoviePagingSource extends RxPagingSource<Integer, Movie> {
    private final MovieApiService apiService;
    private final String apiKey;
    private final MovieDtoToMovieMapper mapper;
    private final FavoriteMovieDao favoriteDao;

    public MoviePagingSource(MovieApiService apiService, String apiKey, FavoriteMovieDao favoriteDao) {
        this.apiService = apiService;
        this.apiKey = apiKey;
        this.favoriteDao = favoriteDao;
        this.mapper = new MovieDtoToMovieMapper();
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Movie>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        int page = loadParams.getKey() != null ? loadParams.getKey() : 1;

        return apiService.getPopularMovies(apiKey, page)
                .subscribeOn(Schedulers.io())
                .flatMap(response -> {
                    List<MovieDto> dtos = response.getMovies();
                    return favoriteDao.getFavoriteMovies()
                            .map(favorites -> {
                                List<Integer> favoriteIds = new ArrayList<>();
                                for (FavoriteMovieEntity favorite : favorites) {
                                    favoriteIds.add(favorite.getId());
                                }

                                List<Movie> movies = new ArrayList<>();
                                for (MovieDto dto : dtos) {
                                    boolean isFavorite = favoriteIds.contains(dto.getId());
                                    movies.add(mapper.map(dto, isFavorite));
                                    Log.d("HIHI", "BAAAA");
                                }



                                Integer nextKey = (page < response.getTotalPages()) ? page + 1 : null;
                                Integer prevKey = (page == 1) ? null : page - 1;

                                return new LoadResult.Page<>(movies, prevKey, nextKey);
                            });
                });

    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Movie> pagingState) {
        return null;
    }

//    @Override
//    public Integer getRefreshKey(@NonNull PagingState<Integer, Movie> state) {
//        Integer anchorPosition = state.getAnchorPosition();
//        if (anchorPosition == null) {
//            return null;
//        }
//
//        LoadResult.Page<Integer, Movie> anchorPage = state.closestPageToPosition(anchorPosition);
//        if (anchorPage == null) {
//            return null;
//        }
//
//        Integer prevKey = anchorPage.getPrevKey();
//        if (prevKey != null) {
//            return new PagingState<>(state.getPages(), prevKey + 1, state.getConfig(), false);
//        }
//
//        Integer nextKey = anchorPage.getNextKey();
//        if (nextKey != null) {
//            return new PagingState<>(state.getPages(), nextKey - 1, state.getConfig(), false);
//        }
//
//        return null;
//    }
}