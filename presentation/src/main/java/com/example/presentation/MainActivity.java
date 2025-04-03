package com.example.presentation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.presentation.databinding.ActivityMainBinding;
import com.example.presentation.databinding.NavHeaderBinding;
import com.example.presentation.ui.adapter.ViewPagerAdapter;
import com.example.presentation.ui.fragment.ListMoviesFragment;
import com.example.presentation.ui.viewmodel.UserViewModel;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavHeaderBinding headerBinding;
    private boolean isGridMode = false;
    private UserViewModel userViewModel;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).getAppComponent().inject(this);
        userViewModel = new ViewModelProvider(this, viewModelFactory).get(UserViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setupViewPager();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        binding.toolbarIconBurger.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
        });

        headerBinding = NavHeaderBinding.bind(navigationView.getHeaderView(0));
        headerBinding.setViewModel(userViewModel);
        headerBinding.setLifecycleOwner(this);

        userViewModel.getIsEditModeLiveData().observe(this, isEditMode -> {
            headerBinding.setIsEditMode(isEditMode);
        });
        userViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        headerBinding.reminderShortList.setLayoutManager(new LinearLayoutManager(this));

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
                            tab.setIcon(R.drawable.icon_nav_home);
                            break;
                        case 1:
                            tab.setText("Favorites");
                            tab.setIcon(R.drawable.icon_nav_favorite);
                            break;
                        case 2:
                            tab.setText("Settings");
                            tab.setIcon(R.drawable.icon_nav_settings);
                            break;
                        case 3:
                            tab.setText("About");
                            tab.setIcon(R.drawable.icon_nav_about);
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