package com.tviptv.app.data.platform;

import android.content.Context;
import com.tviptv.app.data.repository.SourceRepository;
import com.tviptv.app.data.repository.WatchHistoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class WatchNextPublisher_Factory implements Factory<WatchNextPublisher> {
  private final Provider<Context> contextProvider;

  private final Provider<SourceRepository> sourceRepositoryProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  public WatchNextPublisher_Factory(Provider<Context> contextProvider,
      Provider<SourceRepository> sourceRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.sourceRepositoryProvider = sourceRepositoryProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
  }

  @Override
  public WatchNextPublisher get() {
    return newInstance(contextProvider.get(), sourceRepositoryProvider.get(), watchHistoryRepositoryProvider.get());
  }

  public static WatchNextPublisher_Factory create(Provider<Context> contextProvider,
      Provider<SourceRepository> sourceRepositoryProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider) {
    return new WatchNextPublisher_Factory(contextProvider, sourceRepositoryProvider, watchHistoryRepositoryProvider);
  }

  public static WatchNextPublisher newInstance(Context context, SourceRepository sourceRepository,
      WatchHistoryRepository watchHistoryRepository) {
    return new WatchNextPublisher(context, sourceRepository, watchHistoryRepository);
  }
}
