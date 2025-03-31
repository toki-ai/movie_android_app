package com.example.presentation.ui.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.presentation.ui.fragment.AboutFragment;
import com.example.presentation.ui.fragment.FavoriteFragment;
import com.example.presentation.ui.fragment.ListMoviesFragment;
import com.example.presentation.ui.fragment.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ListMoviesFragment();
            case 1:
                return new FavoriteFragment();
            case 2:
                return new SettingFragment();
            case 3:
                return new AboutFragment();
            default:
                return new ListMoviesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}