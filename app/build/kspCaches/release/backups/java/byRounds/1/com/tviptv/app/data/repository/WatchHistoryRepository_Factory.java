package com.tviptv.app.data.repository;

import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.LastWatchedDao;
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
public final class WatchHistoryRepository_Factory implements Factory<WatchHistoryRepository> {
  private final Provider<LastWatchedDao> lastWatchedDaoProvider;

  private final Provider<ChannelDao> channelDaoProvider;

  public WatchHistoryRepository_Factory(Provider<LastWatchedDao> lastWatchedDaoProvider,
      Provider<ChannelDao> channelDaoProvider) {
    this.lastWatchedDaoProvider = lastWatchedDaoProvider;
    this.channelDaoProvider = channelDaoProvider;
  }

  @Override
  public WatchHistoryRepository get() {
    return newInstance(lastWatchedDaoProvider.get(), channelDaoProvider.get());
  }

  public static WatchHistoryRepository_Factory create(
      Provider<LastWatchedDao> lastWatchedDaoProvider, Provider<ChannelDao> channelDaoProvider) {
    return new WatchHistoryRepository_Factory(lastWatchedDaoProvider, channelDaoProvider);
  }

  public static WatchHistoryRepository newInstance(LastWatchedDao lastWatchedDao,
      ChannelDao channelDao) {
    return new WatchHistoryRepository(lastWatchedDao, channelDao);
  }
}
