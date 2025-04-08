package com.example.domain.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Reminder { ;
    private int movieId;
    private String movieTitle;
    private String posterUrl;
    private String year;
    private float rating;
    private long reminderTime;

    public Reminder(int movieId, String movieTitle, String posterUrl, String year, float rating, long reminderTime) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.year = year;
        this.rating = rating;
        this.reminderTime = reminderTime;
    }

    public int getMovieId() { return movieId; }
    public String getMovieTitle() { return movieTitle; }
    public String getPosterUrl() { return posterUrl; }
    public String getYear() { return year; }
    public float getRating() { return rating; }
    public long getReminderTime() { return reminderTime; }

    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(reminderTime));
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder that = (Reminder) o;
        return movieId == that.movieId && reminderTime == that.reminderTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, reminderTime);
    }
}