package com.example.presentation.di;

import android.content.Context;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.data.repository.MovieRepositoryImpl;
import com.example.data.source.local.AppDatabase;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.repository.MovieRepository;
import com.example.domain.usecase.AddFavoriteMovieUseCase;
import com.example.domain.usecase.GetFavoriteMoviesUseCase;
import com.example.domain.usecase.GetMoviesPagedUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;
import com.example.presentation.ui.viewmodel.FavoriteViewModel;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.util.ViewModelFactory;
import com.example.presentation.util.ViewModelKey;

import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {
    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    MovieApiService provideMovieApiService(Retrofit retrofit) {
        return retrofit.create(MovieApiService.class);
    }

    @Provides
    @Singleton
    AppDatabase provideAppDatabase(Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    FavoriteMovieDao provideFavoriteDao(AppDatabase database) {
        return database.favoriteDao();
    }

    @Provides
    @Singleton
    String provideApiKey() {
        return "e7631ffcb8e766993e5ec0c1f4245f93";
    }

    @Provides
    @Singleton
    MovieRepository provideMovieRepository(MovieApiService apiService, FavoriteMovieDao favoriteDao, String apiKey) {
        return new MovieRepositoryImpl(apiService, favoriteDao, apiKey);
    }

    @Provides
    @Singleton
    GetMoviesPagedUseCase provideGetMoviesPagedUseCase(MovieRepository repository) {
        return new GetMoviesPagedUseCase(repository);
    }

    @Provides
    @Singleton
    GetFavoriteMoviesUseCase provideGetFavoriteMoviesUseCase(MovieRepository repository) {
        return new GetFavoriteMoviesUseCase(repository);
    }

    @Provides
    @Singleton
    AddFavoriteMovieUseCase provideAddFavoriteMovieUseCase(MovieRepository repository) {
        return new AddFavoriteMovieUseCase(repository);
    }

    @Provides
    @Singleton
    RemoveFavoriteMovieUseCase provideRemoveFavoriteMovieUseCase(MovieRepository repository) {
        return new RemoveFavoriteMovieUseCase(repository);
    }

    @Provides
    @Singleton
    @IntoMap
    @ViewModelKey(MovieViewModel.class)
    ViewModel provideMovieViewModel(GetMoviesPagedUseCase getMoviesPagedUseCase,
                                    AddFavoriteMovieUseCase addFavoriteMovieUseCase,
                                    RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase) {
        return new MovieViewModel(getMoviesPagedUseCase, addFavoriteMovieUseCase, removeFavoriteMovieUseCase);
    }

    @Provides
    @Singleton
    @IntoMap
    @ViewModelKey(FavoriteViewModel.class)
    ViewModel provideFavoriteViewModel(GetFavoriteMoviesUseCase useCase, RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase) {
        return new FavoriteViewModel(useCase, removeFavoriteMovieUseCase);
    }

    @Provides
    @Singleton
    ViewModelProvider.Factory provideViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> viewModels) {
        return new ViewModelFactory(viewModels);
    }
}