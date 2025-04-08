package com.example.presentation.ui.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.domain.entity.Movie;
import com.example.presentation.R;
import com.example.presentation.databinding.FragmentMovieDetailBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.adapter.CastCrewAdapter;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.ui.viewmodel.SharedViewModel;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieDetailFragment extends Fragment {
    private FragmentMovieDetailBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private CastCrewAdapter castCrewAdapter;

    @Inject
    MovieViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyApplication.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMovieDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        castCrewAdapter = new CastCrewAdapter();
        binding.detailCrewList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.detailCrewList.setAdapter(castCrewAdapter);
        binding.setViewModel(viewModel);
        Bundle args = getArguments();
        if (args != null) {
            int movieId = args.getInt("arg_movie_id", -1);
            String movieTitle = args.getString("arg_movie_title", "");

            disposables.add(
                    viewModel.getMovieDetail(movieId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    movie -> {
                                        binding.setMovie(movie);
                                        Log.d("TAGTAG", String.valueOf(movie.getCredits().size()));
                                        castCrewAdapter.submitList(movie.getCredits());
                                    },
                                    throwable -> {
                                        Log.e("Movie Detail", "Error: " + throwable.getMessage());
                                        Toast.makeText(requireContext(), "Error loading movies: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                            )
            );
        }

        viewModel.getFavoriteChangeLiveData().observe(getViewLifecycleOwner(), changedMovie -> {
            Movie currentMovie = binding.getMovie();
            if (currentMovie != null) {
                currentMovie.setFavorite(changedMovie.isFavorite());
                binding.setMovie(currentMovie);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}