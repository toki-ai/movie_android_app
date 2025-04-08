package com.example.presentation;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.presentation.databinding.ActivityMainBinding;
import com.example.presentation.databinding.NavHeaderBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.ReminderAdapter;
import com.example.presentation.ui.adapter.ViewPagerAdapter;
import com.example.presentation.ui.viewmodel.ReminderViewModel;
import com.example.presentation.ui.viewmodel.SharedViewModel;
import com.example.presentation.ui.viewmodel.UserViewModel;
import com.example.presentation.util.Constant;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private List<NavController> navControllers = new ArrayList<>(Collections.nCopies(4, null));
    private AppBarConfiguration appBarConfiguration;
    private ReminderAdapter reminderShortAdapter;

    @Inject
    UserViewModel userViewModel;
    @Inject
    ReminderViewModel reminderViewModel;

    private Bitmap profileImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getAppComponent().inject(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userViewModel.setContext(this);
        setupViewPager();
        setUpToolbar();
        setUpDrawer();
        setupBackPressedHandler();
        setupReminderShortList();
        userViewModel.loadUser();

    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(3);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) ->
                tab.setText(Constant.TAB_TITLE_LIST.get(position)).setIcon(Constant.TAB_ICON_LIST.get(position))
        ).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
                updateNavController(tab.getPosition());
                syncActionBarWithNavController(tab.getPosition());
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
            }
        });

        // Khởi tạo NavController cho tab đầu tiên
        updateNavController(0);
        syncActionBarWithNavController(0);
    }

    private void updateNavController(int position) {
        if (navControllers.get(position) == null) {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentByTag("f" + position);
            if (navHostFragment != null) {
                navControllers.set(position, navHostFragment.getNavController());
            }
        }
    }

    private void syncActionBarWithNavController(int position) {
        NavController currentNavController = navControllers.get(position);
        if (currentNavController != null) {
            // Sử dụng constructor mới thay vì Builder
            appBarConfiguration = new AppBarConfiguration.Builder(currentNavController.getGraph())
                    .setOpenableLayout(binding.drawerLayout) // Thay setDrawerLayout bằng setOpenableLayout
                    .build();
            NavigationUI.setupWithNavController(binding.toolbar, currentNavController, appBarConfiguration);
        }
    }

    public void setUpToolbar() {
        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.toolbarIconList.setOnClickListener(v -> sharedViewModel.toggleGridMode());

        sharedViewModel.getIsGridLiveData().observe(this, isGrid -> binding.toolbarIconList.setImageResource(
                isGrid ? R.drawable.icon_toolbar_list : R.drawable.icon_toolbar_grid
        ));
    }

    public void setUpDrawer() {
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
            userViewModel.saveProfile(getProfileImageBitmap());
        });

        headerBinding.reminderBtnShow.setOnClickListener(v -> {
            int currentTab = binding.viewPager.getCurrentItem();
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
                Toast.makeText(this, "NavController is null for tab " + currentTab, Toast.LENGTH_SHORT).show();
            }
        });

        headerBinding.reminderShortList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        String currentBirthday = userViewModel.getUserProfile().birthday.get();
        if (currentBirthday != null && !currentBirthday.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                calendar.setTime(sdf.parse(currentBirthday));
            } catch (Exception e) {

            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    userViewModel.getUserProfile().birthday.set(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void showMediaPopup() {
        PopupMenu popupMenu = new PopupMenu(this, headerBinding.profileAvatar);
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

    private void setupReminderShortList() {
        reminderShortAdapter = new ReminderAdapter(reminderViewModel, binding.getRoot(), null); // navController không cần thiết ở đây
        reminderShortAdapter.setMaxItems(2);
        headerBinding.reminderShortList.setAdapter(reminderShortAdapter);

        reminderViewModel.getAllReminders().observe(this, reminders -> {
            reminderShortAdapter.setReminders(reminders);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        int currentItem = binding.viewPager.getCurrentItem();
        NavController currentNavController = navControllers.get(currentItem);
        if (currentNavController != null && appBarConfiguration != null) {
            return NavigationUI.navigateUp(currentNavController, appBarConfiguration) || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    public Bitmap getProfileImageBitmap() {
        return profileImageBitmap;
    }


    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        headerBinding.profileAvatar.setImageBitmap(profileImageBitmap);
                        Log.d("MainActivity", "Gallery image loaded into Bitmap");
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error loading gallery image", e);
                        Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileImageBitmap = (Bitmap) Objects.requireNonNull(result.getData().getExtras()).get("data");
                    if (profileImageBitmap != null) {
                        headerBinding.profileAvatar.setImageBitmap(profileImageBitmap);
                        Log.d("MainActivity", "Camera image loaded into Bitmap");
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    cameraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                } else {
                    Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                }
            });
}