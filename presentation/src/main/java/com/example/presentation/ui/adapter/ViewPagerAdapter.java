package com.example.presentation.ui.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.presentation.R;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int[] NAV_GRAPHS = {
            R.navigation.nav_list,
            R.navigation.nav_favorite,
            R.navigation.nav_setting,
            R.navigation.nav_about
    };

    public ViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @Override
    public Fragment createFragment(int position) {
        return NavHostFragment.create(NAV_GRAPHS[position]);
    }

    @Override
    public int getItemCount() {
        return NAV_GRAPHS.length;
    }
}