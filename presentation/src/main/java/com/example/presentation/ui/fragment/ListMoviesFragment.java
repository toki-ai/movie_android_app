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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.domain.entity.Movie;
import com.example.presentation.di.MyApplication;
import com.example.presentation.databinding.FragmentListMoviesBinding;
import com.example.presentation.ui.adapter.MovieAdapter;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.ui.viewmodel.SharedViewModel;

import javax.inject.Inject;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ListMoviesFragment extends Fragment {
    private static final String TAG = "ListMoviesFragment";

    private FragmentListMoviesBinding binding;
    private MovieAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    MovieViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Injecting dependencies");
        MyApplication.getAppComponent().inject(this);
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
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        adapter = new MovieAdapter(false, viewModel, NavHostFragment.findNavController(this), MovieAdapter.TYPE.List);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        sharedViewModel.getIsGridLiveData().observe(getViewLifecycleOwner(), isGrid -> updateViewMode(isGrid));
        binding.recyclerView.setAdapter(adapter);
        viewModel.refreshMovies();
        disposables.add(
                viewModel.getMovies()
                        .subscribe(
                                pagingData -> {
                                    Log.d(TAG, "Received PagingData");
                                    adapter.submitData(getViewLifecycleOwner().getLifecycle(), pagingData);
                                    adapter.notifyDataSetChanged();
                                    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                                        @Override
                                        public void onItemRangeInserted(int positionStart, int itemCount) {
                                            Log.d(TAG, "Page loaded with " + adapter.getItemCount() + " total items so far");
                                        }
                                    });
                                },
                                throwable -> {
                                    Log.e(TAG, "Error: " + throwable.getMessage());
                                    Toast.makeText(requireContext(), "Error loading movies: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                        )
        );

        viewModel.getFavoriteChangeLiveData().observe(getViewLifecycleOwner(), changedMovie -> {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Movie movie = adapter.peek(i);
                if (movie != null && movie.getId() == changedMovie.getId()) {
                    movie.setFavorite(changedMovie.isFavorite());
                    adapter.notifyItemChanged(i);
                    break;
                }
            }
        });
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
        disposables.clear();
        binding = null;
    }
}