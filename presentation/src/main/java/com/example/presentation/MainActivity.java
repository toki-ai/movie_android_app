package com.example.presentation;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.presentation.databinding.ActivityMainBinding;
import com.example.presentation.ui.adapter.ViewPagerAdapter;
import com.example.presentation.ui.fragment.ListMoviesFragment;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private boolean isGridMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).getAppComponent().inject(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setupViewPager();

        binding.toolbarIconList.setOnClickListener(v -> {
            isGridMode = !isGridMode;
            updateToolbarIcon();
            notifyFragmentViewModeChanged();
        });

        updateToolbarIcon();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Movies");
                            break;
                        case 1:
                            tab.setText("Favorites");
                            break;
                        case 2:
                            tab.setText("Settings");
                            break;
                        case 3:
                            tab.setText("About");
                            break;
                    }
                }).attach();
    }

    private void updateToolbarIcon() {
        binding.toolbarIconList.setImageResource(isGridMode ? R.drawable.icon_toolbar_grid : R.drawable.icon_toolbar_list);
    }

    private void notifyFragmentViewModeChanged() {
        Fragment fragment = getSupportFragmentManager()
                .findFragmentByTag("f" + binding.viewPager.getCurrentItem());
        if (fragment instanceof ListMoviesFragment) {
            ((ListMoviesFragment) fragment).updateViewMode(isGridMode);
        }
    }
}