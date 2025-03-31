package com.example.data.mapper;

import com.example.data.source.remote.model.MovieDto;
import com.example.domain.entity.Movie;

public class MovieDtoToMovieMapper {
    public Movie map(MovieDto dto, boolean isFavorite) {
        //(int id, String title, String overview, String releaseDate, double voteAverage,
        // String backdropPath, String posterPath, boolean adult, isFavorite) {
        return new Movie(
                dto.getId(),
                dto.getTitle(),
                dto.getOverview(),
                dto.getReleaseDate(),
                dto.getVoteAverage(),
                dto.getBackdropPath(),
                dto.getPosterPath(),
                dto.isAdult(),
                isFavorite
        );
    }
}