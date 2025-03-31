package com.example.presentation.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.entity.Movie;
import com.example.presentation.MyApplication;
import com.example.presentation.databinding.FragmentListMoviesBinding;
import com.example.presentation.ui.adapter.MovieAdapter;
import com.example.presentation.ui.viewmodel.MovieViewModel;

import javax.inject.Inject;

public class ListMoviesFragment extends Fragment {
    private FragmentListMoviesBinding binding;
    private MovieAdapter adapter;
    private MovieViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) requireActivity().getApplication()).getAppComponent().inject(this);
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MovieViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListMoviesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MovieAdapter(false, viewModel);
        binding.recyclerView.setAdapter(adapter);

        updateLayoutManager();

        viewModel.getMovies().subscribe(
                pagingData -> adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData),
                throwable -> {
                    Toast.makeText(requireContext(), "Error loading movies: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                }
        );

        viewModel.getFavoriteChangeLiveData().observe(getViewLifecycleOwner(), changedMovie -> {
            int position = findMoviePosition(changedMovie);
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }
        });
    }

    private int findMoviePosition(Movie changedMovie) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Movie movie = adapter.peek(i);
            if (movie != null && movie.getId() == changedMovie.getId()) {
                return i;
            }
        }
        return -1;
    }

    private void updateLayoutManager() {
        if (adapter.isGridMode()) {
            binding.recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        } else {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    public void updateViewMode(boolean isGridMode) {
        if (adapter.isGridMode() != isGridMode) {
            adapter.toggleViewMode();
            updateLayoutManager();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}