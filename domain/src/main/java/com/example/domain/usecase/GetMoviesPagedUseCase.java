package com.example.domain.usecase;

import android.util.Log;

import androidx.paging.PagingData;

import com.example.domain.entity.Movie;
import com.example.domain.repository.MovieRepository;

import io.reactivex.rxjava3.core.Flowable;

public class GetMoviesPagedUseCase {
    private final MovieRepository movieRepository;

    public GetMoviesPagedUseCase(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public Flowable<PagingData<Movie>> execute(String type, String query) {
        return movieRepository.getMovies(type,query);
    }

    public Flowable<PagingData<Movie>> execute(String type) {
        return movieRepository.getMovies(type, null);
    }
//    public Flowable<PagingData<Movie>> execute(String category, String query) {
//        MoviePagingSource pagingSource = new MoviePagingSource(apiService, apiKey, favoriteDao, settingPreference, query);
//        return Flowable.just(PagingData.from(pagingSource));
//    }
//
//    public Flowable<PagingData<Movie>> execute(String category) {
//        return execute(category, null); // Gọi với query = null cho chế độ thông thường
//    }
}