package com.example.data.mapper;

import com.example.data.source.local.entity.FavoriteMovieEntity;
import com.example.domain.entity.Movie;

public class FavoriteEntityToMovieMapper {
    public Movie map(FavoriteMovieEntity entity) {
        return new Movie(
                entity.getId(),
                entity.getTitle(),
                entity.getOverview(),
                entity.getReleaseDate(),
                entity.getVoteAverage(),
                entity.getBackdropPath(),
                entity.getPosterPath(),
                entity.isAdult(),
                true
        );
    }
}