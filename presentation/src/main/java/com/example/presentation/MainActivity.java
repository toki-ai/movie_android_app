package com.example.presentation;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.presentation.databinding.ActivityMainBinding;
import com.example.presentation.databinding.NavHeaderBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.ViewPagerAdapter;
import com.example.presentation.ui.viewmodel.SharedViewModel;
import com.example.presentation.ui.viewmodel.UserViewModel;
import com.example.presentation.util.Constant;
import com.google.android.material.tabs.TabLayoutMediator;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavHeaderBinding headerBinding;

    @Inject
    UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getAppComponent().inject(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setUpToolbar();
        setUpDrawer();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setText(Constant.TAB_TITLE_LIST.get(position)).setIcon(Constant.TAB_ICON_LIST.get(position))
        ).attach();

        binding.viewPager.setOffscreenPageLimit(3);
    }

    public void setUpToolbar(){
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbarIconList.setOnClickListener(v -> sharedViewModel.toggleGridMode());

        sharedViewModel.getIsGridLiveData().observe(this , isGrid -> binding.toolbarIconList.setImageResource(
                isGrid ? R.drawable.icon_toolbar_list : R.drawable.icon_toolbar_grid
        ));
    }

    public void setUpDrawer(){
        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.toolbar, R.string.nav_open, R.string.nav_close);
        binding.drawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));
        headerBinding.setViewModel(userViewModel);
        headerBinding.setLifecycleOwner(this);

        userViewModel.getIsEditModeLiveData().observe(this, isEditMode -> headerBinding.setIsEditMode(isEditMode));
        userViewModel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        headerBinding.reminderShortList.setLayoutManager(new LinearLayoutManager(this));
    }
}