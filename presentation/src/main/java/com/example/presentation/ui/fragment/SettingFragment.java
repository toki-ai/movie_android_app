package com.example.presentation.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import com.example.data.preference.SettingPreference;
import com.example.presentation.R;
import com.example.presentation.ui.viewmodel.MovieViewModel;

import javax.inject.Inject;

public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SettingPreference settingPreference;
    @Inject
    MovieViewModel movieViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Khởi tạo các Preference và đặt giá trị ban đầu từ SettingPreference
        settingPreference = new SettingPreference(requireContext());

        // Đặt giá trị ban đầu cho các Preference
        ListPreference categoryPref = findPreference("category");
        if (categoryPref != null) {
            categoryPref.setValue(settingPreference.getCategory());
        }

        ListPreference sortByPref = findPreference("sort_by");
        if (sortByPref != null) {
            sortByPref.setValue(settingPreference.getSortBy());
        }

        SeekBarPreference minYearPref = findPreference("min_year");
        if (minYearPref != null) {
            minYearPref.setValue(settingPreference.getMinYear());
        }

        SeekBarPreference maxYearPref = findPreference("max_year");
        if (maxYearPref != null) {
            maxYearPref.setValue(settingPreference.getMaxYear());
        }

        SeekBarPreference minRatingPref = findPreference("min_rating");
        if (minRatingPref != null) {
            minRatingPref.setValue((int) settingPreference.getMinRating());
        }

        SeekBarPreference pagesPerLoadPref = findPreference("pages_per_load");
        if (pagesPerLoadPref != null) {
            pagesPerLoadPref.setValue(settingPreference.getPagesPerLoad());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "category":
                String category = sharedPreferences.getString(key, "popular");
                settingPreference.setCategory(category);
                //movieViewModel.refreshMovies();
                break;
            case "sort_by":
                String sortBy = sharedPreferences.getString(key, "rating");
                settingPreference.setSortBy(sortBy);
                //movieViewModel.refreshMovies();
                break;
            case "min_year":
            case "max_year":
                int minYear = sharedPreferences.getInt("min_year", 1900);
                int maxYear = sharedPreferences.getInt("max_year", 2024);
                settingPreference.setYearRange(minYear, maxYear);
                //movieViewModel.refreshMovies();
                break;
            case "min_rating":
                int minRating = sharedPreferences.getInt(key, 0);
                settingPreference.setMinRating(minRating);
                //movieViewModel.refreshMovies();
                break;
            case "pages_per_load":
                int pagesPerLoad = sharedPreferences.getInt(key, 1);
                settingPreference.setPagesPerLoad(pagesPerLoad);
                //movieViewModel.refreshMovies();
                break;
        }
    }
}