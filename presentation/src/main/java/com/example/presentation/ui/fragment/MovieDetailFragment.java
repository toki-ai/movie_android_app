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

import com.example.presentation.databinding.FragmentMovieDetailBinding;
import com.example.presentation.di.MyApplication;
import com.example.presentation.ui.viewmodel.MovieDetailViewModel;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MovieDetailFragment extends Fragment {
    private FragmentMovieDetailBinding binding;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    MovieDetailViewModel viewModel;

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
                                        //castCrewAdapter.submitList(movie.getCastCrew());
                                    },
                                    throwable -> {
                                        Log.e("Movie Detail", "Error: " + throwable.getMessage());
                                        Toast.makeText(requireContext(), "Error loading movies: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                            )
            );

//            viewModel.getMovieDetail(movieId).observe(getViewLifecycleOwner(), movie -> {
//                if (movie != null) {
//                    binding.setMovie(movie);
//                    binding.favoriteButton.setText(movie.isFavorite() ? "Remove from Favorites" : "Add to Favorites");
//                }
//            });
//
//            // Xử lý nút Favorite
//            binding.favoriteButton.setOnClickListener(v -> {
//                viewModel.toggleFavorite(movieId);
//                viewModel.getMovieDetail(movieId).observe(getViewLifecycleOwner(), movie -> {
//                    binding.favoriteButton.setText(movie.isFavorite() ? "Remove from Favorites" : "Add to Favorites");
//                });
//            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}