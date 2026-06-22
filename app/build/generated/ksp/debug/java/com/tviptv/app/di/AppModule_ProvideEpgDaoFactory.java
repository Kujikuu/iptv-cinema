package com.tviptv.app.di;

import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.dao.EpgDao;
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
public final class AppModule_ProvideEpgDaoFactory implements Factory<EpgDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideEpgDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public EpgDao get() {
    return provideEpgDao(databaseProvider.get());
  }

  public static AppModule_ProvideEpgDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideEpgDaoFactory(databaseProvider);
  }

  public static EpgDao provideEpgDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEpgDao(database));
  }
}
