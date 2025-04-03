package com.example.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.presentation.MyApplication;
import com.example.presentation.databinding.FragmentFavoriteBinding;
import com.example.presentation.ui.adapter.MovieAdapter;
import com.example.presentation.ui.viewmodel.FavoriteViewModel;
import com.example.presentation.ui.viewmodel.MovieViewModel;

import javax.inject.Inject;

public class FavoriteFragment extends Fragment {
    private FragmentFavoriteBinding binding;
    private MovieAdapter adapter;
    private FavoriteViewModel favoriteViewModel;
    private MovieViewModel movieViewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) requireActivity().getApplication()).getAppComponent().inject(this);
        favoriteViewModel = new ViewModelProvider(this, viewModelFactory).get(FavoriteViewModel.class);
        movieViewModel = new ViewModelProvider(this, viewModelFactory).get(MovieViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MovieAdapter(false, movieViewModel);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);

        favoriteViewModel.getFavoriteMovies().observe(getViewLifecycleOwner(), movies -> {
            adapter.submitData(getViewLifecycleOwner().getLifecycle(), androidx.paging.PagingData.from(movies));
        });

        movieViewModel.getFavoriteChangeLiveData().observe(getViewLifecycleOwner(), changedMovie -> {
            favoriteViewModel.loadFavoriteMovies();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}