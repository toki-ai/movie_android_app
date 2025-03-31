package com.example.data.source.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorites")
public class FavoriteMovieEntity {
    @PrimaryKey
    private int id;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private double voteAverage;
    private String backdropPath;
    private boolean adult;

    public FavoriteMovieEntity(int id, boolean adult, String backdropPath, double voteAverage, String releaseDate, String overview, String posterPath, String title) {
        this.id = id;
        this.adult = adult;
        this.backdropPath = backdropPath;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.overview = overview;
        this.posterPath = posterPath;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public boolean isFavorite() {
        return true;
    }

    public boolean isAdult() {
        return adult;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getTitle() {
        return title;
    }
}