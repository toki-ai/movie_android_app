package com.example.presentation.di;

import android.content.Context;
import com.example.data.preference.SettingPreference;
import com.example.data.repository.MovieRepositoryImpl;
import com.example.data.repository.UserRepositoryImpl;
import com.example.data.source.local.AppDatabase;
import com.example.data.source.local.dao.FavoriteMovieDao;
import com.example.data.source.remote.firebase.FirebaseUserDataSource;
import com.example.data.source.remote.service.MovieApiService;
import com.example.domain.repository.MovieRepository;
import com.example.domain.repository.UserRepository;
import com.example.domain.usecase.AddFavoriteMovieUseCase;
import com.example.domain.usecase.GetFavoriteMoviesUseCase;
import com.example.domain.usecase.GetMovieDetailUseCase;
import com.example.domain.usecase.GetMoviesPagedUseCase;
import com.example.domain.usecase.GetUserUseCase;
import com.example.domain.usecase.RemoveFavoriteMovieUseCase;
import com.example.domain.usecase.SaveUserUseCase;
import com.example.presentation.ui.viewmodel.FavoriteViewModel;
import com.example.presentation.ui.viewmodel.MovieDetailViewModel;
import com.example.presentation.ui.viewmodel.MovieViewModel;
import com.example.presentation.ui.viewmodel.UserViewModel;

import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
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
        return new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/")
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
    public MovieRepository provideMovieRepository(MovieApiService apiService, FavoriteMovieDao favoriteDao, String apiKey, Context context) {
        return new MovieRepositoryImpl(apiService, favoriteDao, apiKey, context);
    }

    @Provides
    @Singleton
    GetMoviesPagedUseCase provideGetMoviesPagedUseCase(MovieRepository repository) {
        return new GetMoviesPagedUseCase(repository);
    }

    @Provides
    @Singleton
    GetMovieDetailUseCase provideGetMovieDetailUseCase(MovieRepository repository){
        return new GetMovieDetailUseCase(repository);
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
    public MovieViewModel provideMovieViewModel(
            GetMoviesPagedUseCase getMoviesPagedUseCase,
            AddFavoriteMovieUseCase addFavoriteMovieUseCase,
            RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase,
            SettingPreference settingPreference
    ) {
        return new MovieViewModel(
                getMoviesPagedUseCase,
                addFavoriteMovieUseCase,
                removeFavoriteMovieUseCase,
                settingPreference
        );
    }

    @Provides
    @Singleton
    public MovieDetailViewModel provideMovieDetailViewModel(GetMovieDetailUseCase getMovieDetailUseCase) {
        return new MovieDetailViewModel(getMovieDetailUseCase);
    }

    @Provides
    @Singleton
    public FavoriteViewModel provideFavoriteViewModel(GetFavoriteMoviesUseCase useCase, RemoveFavoriteMovieUseCase removeFavoriteMovieUseCase) {
        return new FavoriteViewModel(useCase, removeFavoriteMovieUseCase);
    }

    @Provides
    @Singleton
    public UserViewModel provideUserViewModel(GetUserUseCase getUserUseCase, SaveUserUseCase saveUserUseCase) {
        return new UserViewModel(getUserUseCase, saveUserUseCase);
    }

    @Provides
    FirebaseUserDataSource provideFirebaseUserDataSource() {
        return new FirebaseUserDataSource();
    }

    @Provides
    UserRepository provideUserRepository(FirebaseUserDataSource dataSource) {
        return new UserRepositoryImpl(dataSource);
    }

    @Provides
    GetUserUseCase provideGetUserUseCase(UserRepository repository) {
        return new GetUserUseCase(repository);
    }

    @Provides
    SaveUserUseCase provideSaveUserUseCase(UserRepository repository) {
        return new SaveUserUseCase(repository);
    }

    @Provides
    @Singleton
    public SettingPreference provideSettingPreference(Context context) {
        return new SettingPreference(context);
    }
}
