package com.example.domain.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Reminder {

    private int movieId;
    private long timestamp;
    private Movie movieDto;

    public Reminder() {
    }

    public Reminder(int movieId, long timestamp) {
        this.movieId = movieId;
        this.timestamp = timestamp;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Movie getMovieDto() {
        return movieDto;
    }

    public void setMovieDto(Movie movieDto) {
        this.movieDto = movieDto;
    }

    public String getMovieTitle() {
        return movieDto.getTitle();
    }

    public String getMovieReleaseYear() {
        return movieDto.getReleaseYear();
    }

    public double getMovieVoteAverage() {
        return movieDto.getVoteAverage();
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getMoviePosterPathUrl() {
        return movieDto.getPosterPathUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder that = (Reminder) o;
        return movieId == that.movieId && timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, timestamp);
    }
}