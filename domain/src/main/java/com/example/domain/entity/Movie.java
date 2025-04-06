package com.example.domain.entity;


import com.example.domain.utils.Constants;

import java.util.List;
import java.util.Objects;

public class Movie {
    private int id;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private double voteAverage;
    private String backdropPath;
    private boolean adult;
    private boolean isFavorite;
    private List<CastCrew> credits;

    public Movie() {}

    public Movie(int id, String title, String overview, String releaseDate, double voteAverage, String backdropPath, String posterPath, boolean adult, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.backdropPath = backdropPath;
        this.posterPath = posterPath;
        this.adult = adult;
        this.isFavorite = isFavorite;
    }

    public Movie(int id, String title, String overview, String releaseDate, double voteAverage, String backdropPath, String posterPath, boolean adult, boolean isFavorite, List<CastCrew> credits) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.backdropPath = backdropPath;
        this.posterPath = posterPath;
        this.adult = adult;
        this.isFavorite = isFavorite;
        this.credits = credits;
    }
    public String getPosterPathUrl() {
        return Constants.IMAGE_BASE_URL + posterPath;
    }

    public String getBackdropPathUrl() {
        return Constants.IMAGE_BASE_URL + backdropPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate.replace("-", "/");
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getReleaseYear() {
        return releaseDate.split("-")[0];
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public List<CastCrew> getCredits() {
        return credits;
    }

    public void setCredits(List<CastCrew> credits) {
        this.credits = credits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id == movie.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}