package com.tviptv.app.di;

import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.dao.ChannelDao;
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
public final class AppModule_ProvideChannelDaoFactory implements Factory<ChannelDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideChannelDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ChannelDao get() {
    return provideChannelDao(databaseProvider.get());
  }

  public static AppModule_ProvideChannelDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideChannelDaoFactory(databaseProvider);
  }

  public static ChannelDao provideChannelDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideChannelDao(database));
  }
}
