package com.tviptv.app.di;

import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.dao.SourceDao;
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
public final class AppModule_ProvideSourceDaoFactory implements Factory<SourceDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideSourceDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SourceDao get() {
    return provideSourceDao(databaseProvider.get());
  }

  public static AppModule_ProvideSourceDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideSourceDaoFactory(databaseProvider);
  }

  public static SourceDao provideSourceDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSourceDao(database));
  }
}
