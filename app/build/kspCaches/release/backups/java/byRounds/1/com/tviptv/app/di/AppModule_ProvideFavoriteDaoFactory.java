package com.tviptv.app.di;

import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.dao.FavoriteDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class AppModule_ProvideFavoriteDaoFactory implements Factory<FavoriteDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideFavoriteDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FavoriteDao get() {
    return provideFavoriteDao(databaseProvider.get());
  }

  public static AppModule_ProvideFavoriteDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideFavoriteDaoFactory(databaseProvider);
  }

  public static FavoriteDao provideFavoriteDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideFavoriteDao(database));
  }
}
