package com.example.presentation;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.data.preference.SettingPreference;
import com.example.presentation.databinding.ActivityMainBinding;
import com.example.presentation.databinding.NavHeaderBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.ReminderAdapter;
import com.example.presentation.ui.adapter.ViewPagerAdapter;
import com.example.presentation.ui.viewmodel.FavoriteViewModel;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.example.presentation.ui.viewmodel.SharedViewModel;
import com.example.presentation.ui.viewmodel.UserViewModel;
import com.example.presentation.util.Constant;
import com.example.presentation.util.SpacingItemDecoration;
import com.example.presentation.util.StyleConfig;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.Manifest;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavHeaderBinding headerBinding;
    private final List<NavController> navControllers = new ArrayList<>(Collections.nCopies(4, null));
    private AppBarConfiguration appBarConfiguration;
    private ReminderAdapter reminderShortAdapter;
    private SharedViewModel sharedViewModel;

    @Inject
    UserViewModel userViewModel;
    @Inject
    ReminderViewModel reminderViewModel;
    @Inject
    FavoriteViewModel favoriteViewModel;
    @Inject
    MovieViewModel movieViewModel;
    @Inject
    SettingPreference settingPreference;

    private Bitmap profileImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getAppComponent().inject(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userViewModel.setContext(this);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        setupViewPagerAndToolbarTitleAndVisibleIcon();
        setUpToolbarGridMode();
        setUpDrawer();
        setupBackPressedHandler();

        userViewModel.loadUser();
        favoriteViewModel.loadFavoriteMovies();

        observeToolbarTitle();
        updateToolbarIconVisibility(0);

        userViewModel.getImageLiveData().observe(this, image -> {
            if (image != null && !image.startsWith("http")) {
                Bitmap bitmap = userViewModel.getProfileImageBitmap();
                if (bitmap != null) {
                    headerBinding.profileAvatar.setImageBitmap(bitmap);
                }
            }
        });
    }

    private void observeToolbarTitle() {
        sharedViewModel.getToolbarTitle().observe(this, title -> {
            binding.toolbarTitle.setText(title);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search movies...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                movieViewModel.searchMovies(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                movieViewModel.searchMovies(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                movieViewModel.refreshMovies();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int currentTab = binding.viewPager.getCurrentItem();
        NavController currentNavController = navControllers.get(currentTab);
        boolean shouldShowMenu = false;

        if (currentNavController != null) {
            NavDestination currentDestination = currentNavController.getCurrentDestination();
            if (currentDestination != null) {
                int destinationId = currentDestination.getId();
                shouldShowMenu = (currentTab == 0 && destinationId == R.id.listMoviesFragment);
            }
        }

        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(shouldShowMenu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            int currentItem = binding.viewPager.getCurrentItem();
            NavController currentNavController = navControllers.get(currentItem);

            if (currentNavController != null && currentNavController.getCurrentDestination() != null) {
                int destinationId = currentNavController.getCurrentDestination().getId();
                if (destinationId != R.id.listMoviesFragment && destinationId != R.id.favoriteFragment &&
                        destinationId != R.id.settingFragment && destinationId != R.id.aboutFragment) {
                    return currentNavController.navigateUp();
                }
            }

            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }

        String selectedCategory = null;
        if (itemId == R.id.category_popular) {
            selectedCategory = "popular";
        } else if (itemId == R.id.category_top_rated) {
            selectedCategory = "top_rated";
        } else if (itemId == R.id.category_upcoming) {
            selectedCategory = "upcoming";
        } else if (itemId == R.id.category_now_playing) {
            selectedCategory = "now_playing";
        }

        if (selectedCategory != null) {
            settingPreference.setCategory(selectedCategory);
            movieViewModel.refreshMovies();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPagerAndToolbarTitleAndVisibleIcon() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            @SuppressLint("InflateParams") View customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            TextView tabText = customView.findViewById(R.id.tab_text);
            ImageView tabIcon = customView.findViewById(R.id.tab_icon);
            TextView tabBadge = customView.findViewById(R.id.tab_badge);

            tabText.setText(Constant.TAB_TITLE_LIST.get(position));
            tabIcon.setImageResource(Constant.TAB_ICON_LIST.get(position));
            tab.setCustomView(customView);

            if (position == 1) {
                favoriteViewModel.getFavoriteCountLiveData().observe(this, count -> {
                    if (count != null && count > 0) {
                        String displayText = count > 99 ? "99+" : String.valueOf(count);
                        tabBadge.setText(displayText);
                        tabBadge.setVisibility(View.VISIBLE);
                    } else {
                        tabBadge.setVisibility(View.GONE);
                    }
                });
            }
        }).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
                updateNavController(tab.getPosition());
                syncActionBarWithNavController(tab.getPosition());
                updateToolbarIconVisibility(tab.getPosition());
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
                updateNavController(position);
                syncActionBarWithNavController(position);
                invalidateOptionsMenu();
            }
        });

        for (int i = 0; i < adapter.getItemCount(); i++) {
            updateNavController(i);
        }

        updateNavController(0);
        syncActionBarWithNavController(0);
    }

    private void updateToolbarIconVisibility(int position) {
        if (position == 0) {
            binding.toolbarIconList.setVisibility(View.VISIBLE);
        } else {
            binding.toolbarIconList.setVisibility(View.GONE);
        }
    }

    private void updateNavController(int position) {
        NavController currentNavController = navControllers.get(position);
        if (currentNavController == null) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + position);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                navControllers.set(position, navController);
                navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                    updateToolbarTitleForFragment(destination, arguments);
                    updateToolbarIconVisibilityForFragment(destination);
                    updateToolbarIconForFragment(destination);
                    invalidateOptionsMenu();
                });
            } else {
                Log.e("NavDebug", "NavHostFragment is null for tag: f" + position);
            }
        } else {
            NavDestination currentDestination = currentNavController.getCurrentDestination();
            if (currentDestination != null) {
                updateToolbarTitleForFragment(currentDestination, currentNavController.getCurrentBackStackEntry() != null ? currentNavController.getCurrentBackStackEntry().getArguments() : null);
                updateToolbarIconForFragment(currentDestination);
            }
        }
    }

    private void updateToolbarTitleForFragment(NavDestination destination, Bundle arguments) {
        int destinationId = destination.getId();
        String title;
        if (destinationId == R.id.listMoviesFragment) {
            title = "List Movies";
        } else if (destinationId == R.id.favoriteFragment) {
            title = "Favourite";
        } else if (destinationId == R.id.settingFragment) {
            title = "Settings";
        } else if (destinationId == R.id.aboutFragment) {
            title = "About";
        } else if (destinationId == R.id.reminderFragment) {
            title = "Reminder";
        } else if (destinationId == R.id.movieDetailFragment) {
            if (arguments != null && arguments.containsKey("arg_movie_title")) {
                title = arguments.getString("arg_movie_title");
                title = title != null ? title : "Movie Detail";
            } else {
                title = "Movie Detail";
            }
        } else {
            title = "Movie Title";
        }
        sharedViewModel.setToolbarTitle(title);
    }

    private void updateToolbarIconVisibilityForFragment(NavDestination destination) {
        int destinationId = destination.getId();
        if (destinationId == R.id.listMoviesFragment) {
            binding.toolbarIconList.setVisibility(View.VISIBLE);
        } else {
            binding.toolbarIconList.setVisibility(View.GONE);
        }
    }

    private void updateToolbarIconForFragment(NavDestination destination) {
        int destinationId = destination.getId();
        if (destinationId != R.id.listMoviesFragment && destinationId != R.id.favoriteFragment &&
                destinationId != R.id.settingFragment && destinationId != R.id.aboutFragment) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        } else {
            Drawable profileIcon = ContextCompat.getDrawable(this, R.drawable.ic_profile);
            if (profileIcon != null) {
                getSupportActionBar().setHomeAsUpIndicator(profileIcon);
            }
        }
    }

    private void syncActionBarWithNavController(int position) {
        NavController currentNavController = navControllers.get(position);
        if (currentNavController != null) {
            appBarConfiguration = new AppBarConfiguration.Builder(currentNavController.getGraph())
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
        }
    }

    public void setUpToolbarGridMode() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            Drawable profileIcon = ContextCompat.getDrawable(this, R.drawable.ic_profile);
            if (profileIcon != null) {
                getSupportActionBar().setHomeAsUpIndicator(profileIcon);
            } else {
                Log.e("MainActivity", "Failed to load ic_profile drawable");
            }
        } else {
            Log.e("MainActivity", "ActionBar is null");
        }

        binding.toolbarIconList.setOnClickListener(v -> sharedViewModel.toggleGridMode());

        sharedViewModel.getIsGridLiveData().observe(this, isGrid -> binding.toolbarIconList.setImageResource(
                isGrid ? R.drawable.icon_toolbar_list : R.drawable.icon_toolbar_grid
        ));
    }

    public void setUpDrawer() {
        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        });

        headerBinding = NavHeaderBinding.bind(binding.navView.getHeaderView(0));
        headerBinding.setViewModel(userViewModel);
        headerBinding.setLifecycleOwner(this);

        userViewModel.getIsEditModeLiveData().observe(this, isEditMode -> headerBinding.setIsEditMode(isEditMode));

        headerBinding.profileBirthday.setOnClickListener(v -> {
            if (userViewModel.getIsEditModeLiveData().getValue() != null && userViewModel.getIsEditModeLiveData().getValue()) {
                showDatePickerDialog();
            }
        });

        headerBinding.profileAvatar.setOnClickListener(v -> {
            if (userViewModel.getIsEditModeLiveData().getValue() != null && userViewModel.getIsEditModeLiveData().getValue()) {
                showMediaPopup();
            }
        });

        headerBinding.profileBtnSave.setOnClickListener(v -> {
            userViewModel.setName(headerBinding.profileName.getText().toString());
            userViewModel.setEmail(headerBinding.profileMail.getText().toString());
            userViewModel.setBirthday(headerBinding.profileBirthday.getText().toString());
            userViewModel.setGender(headerBinding.radioMale.isChecked());
            if (profileImageBitmap != null) {
                userViewModel.setImage(userViewModel.bitmapToBase64(profileImageBitmap));
            }
            userViewModel.saveProfile(profileImageBitmap);
        });

        headerBinding.reminderBtnShow.setOnClickListener(v -> {
            int currentTab = binding.viewPager.getCurrentItem();
            updateNavController(currentTab);
            NavController navController = navControllers.get(currentTab);
            if (navController != null) {
                switch (currentTab) {
                    case 0:
                        navController.navigate(R.id.action_listMoviesFragment_to_reminderFragment);
                        break;
                    case 1:
                        navController.navigate(R.id.action_favoriteFragment_to_reminderFragment);
                        break;
                    case 2:
                        navController.navigate(R.id.action_settingFragment_to_reminderFragment);
                        break;
                    case 3:
                        navController.navigate(R.id.action_aboutFragment_to_reminderFragment);
                        break;
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                StyleConfig.returnToast(this, "NavController is null for tab " + currentTab);
            }
        });

        headerBinding.reminderShortList.setLayoutManager(new LinearLayoutManager(this));
        reminderShortAdapter = new ReminderAdapter(reminderViewModel, binding.getRoot(), null);
        reminderShortAdapter.setMaxItems(2);
        headerBinding.reminderShortList.setAdapter(reminderShortAdapter);

        reminderViewModel.getAllReminders().observe(this, reminders -> {
            reminderShortAdapter.setReminders(reminders);
        });

        headerBinding.reminderShortList.addItemDecoration(
                new SpacingItemDecoration((int) getResources().getDimension(R.dimen.reminder_item_spacing))
        );
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        String currentBirthday = userViewModel.getBirthdayLiveData().getValue();
        if (currentBirthday != null && !currentBirthday.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(currentBirthday));
            } catch (Exception e) {}
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.MyDatePickerDialogTheme,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    userViewModel.setBirthday(selectedDate);
                    headerBinding.profileBirthday.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showMediaPopup() {
        Context wrapper = new ContextThemeWrapper(this, R.style.MyPopupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, headerBinding.profileAvatar);
        popupMenu.getMenuInflater().inflate(R.menu.camera_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.camera_menu_camera) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                }
            } else if (itemId == R.id.camera_menu_gallery) {
                galleryLauncher.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
            }
            return true;
        });
        popupMenu.show();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    return;
                }

                int currentItem = binding.viewPager.getCurrentItem();
                NavController currentNavController = navControllers.get(currentItem);
                if (currentNavController != null && !currentNavController.popBackStack()) {
                    if (currentItem != 0) {
                        binding.viewPager.setCurrentItem(0);
                    } else {
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        int currentItem = binding.viewPager.getCurrentItem();
        NavController currentNavController = navControllers.get(currentItem);
        if (currentNavController != null && appBarConfiguration != null) {
            return currentNavController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        headerBinding.profileAvatar.setImageBitmap(profileImageBitmap);
                        userViewModel.setImage(userViewModel.bitmapToBase64(profileImageBitmap));
                    } catch (Exception e) {
                        StyleConfig.returnToast(this, "Error loading image: " + e.getMessage());
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageBitmap = (Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data");
                    if (profileImageBitmap != null) {
                        headerBinding.profileAvatar.setImageBitmap(profileImageBitmap);
                        userViewModel.setImage(userViewModel.bitmapToBase64(profileImageBitmap));
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                } else {
                    StyleConfig.returnToast(this, "Camera permission denied!");
                }
            });
}