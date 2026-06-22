package com.tviptv.app.di;

import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.dao.LastWatchedDao;
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
public final class AppModule_ProvideLastWatchedDaoFactory implements Factory<LastWatchedDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideLastWatchedDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LastWatchedDao get() {
    return provideLastWatchedDao(databaseProvider.get());
  }

  public static AppModule_ProvideLastWatchedDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideLastWatchedDaoFactory(databaseProvider);
  }

  public static LastWatchedDao provideLastWatchedDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideLastWatchedDao(database));
  }
}
