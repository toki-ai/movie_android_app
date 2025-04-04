package com.example.data.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingPreference {
    private static final String PREF_NAME = "movie_settings";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_SORT_BY = "sort_by";
    private static final String KEY_MIN_YEAR = "min_year";
    private static final String KEY_MAX_YEAR = "max_year";
    private static final String KEY_MIN_RATING = "min_rating";
    private static final String KEY_PAGES_PER_LOAD = "pages_per_load";

    private final SharedPreferences preferences;

    public SettingPreference(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setCategory(String category) {
        preferences.edit().putString(KEY_CATEGORY, category).apply();
    }

    public String getCategory() {
        return preferences.getString(KEY_CATEGORY, "popular");
    }

    public void setSortBy(String sortBy) {
        preferences.edit().putString(KEY_SORT_BY, sortBy).apply();
    }

    public String getSortBy() {
        return preferences.getString(KEY_SORT_BY, "rating");
    }

    public void setYearRange(int minYear, int maxYear) {
        preferences.edit()
                .putInt(KEY_MIN_YEAR, minYear)
                .putInt(KEY_MAX_YEAR, maxYear)
                .apply();
    }

    public int getMinYear() {
        return preferences.getInt(KEY_MIN_YEAR, 1900);
    }

    public int getMaxYear() {
        return preferences.getInt(KEY_MAX_YEAR, 2024);
    }

    public void setMinRating(float minRating) {
        preferences.edit().putFloat(KEY_MIN_RATING, minRating).apply();
    }

    public float getMinRating() {
        return preferences.getFloat(KEY_MIN_RATING, 0.0f);
    }

    public void setPagesPerLoad(int pages) {
        preferences.edit().putInt(KEY_PAGES_PER_LOAD, pages).apply();
    }

    public int getPagesPerLoad() {
        return preferences.getInt(KEY_PAGES_PER_LOAD, 1);
    }
}