package com.tviptv.app.ui.browse;

import com.tviptv.app.data.cache.SeriesEpisodesLoader;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.repository.WatchHistoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ContinueWatchingNavigator_Factory implements Factory<ContinueWatchingNavigator> {
  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider;

  public ContinueWatchingNavigator_Factory(Provider<ChannelDao> channelDaoProvider,
      Provider<SourceDao> sourceDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    this.channelDaoProvider = channelDaoProvider;
    this.sourceDaoProvider = sourceDaoProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.seriesEpisodesLoaderProvider = seriesEpisodesLoaderProvider;
  }

  @Override
  public ContinueWatchingNavigator get() {
    return newInstance(channelDaoProvider.get(), sourceDaoProvider.get(), watchHistoryRepositoryProvider.get(), seriesEpisodesLoaderProvider.get());
  }

  public static ContinueWatchingNavigator_Factory create(Provider<ChannelDao> channelDaoProvider,
      Provider<SourceDao> sourceDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    return new ContinueWatchingNavigator_Factory(channelDaoProvider, sourceDaoProvider, watchHistoryRepositoryProvider, seriesEpisodesLoaderProvider);
  }

  public static ContinueWatchingNavigator newInstance(ChannelDao channelDao, SourceDao sourceDao,
      WatchHistoryRepository watchHistoryRepository, SeriesEpisodesLoader seriesEpisodesLoader) {
    return new ContinueWatchingNavigator(channelDao, sourceDao, watchHistoryRepository, seriesEpisodesLoader);
  }
}
