package com.tviptv.app.ui.player;

import com.tviptv.app.data.cache.SeriesEpisodesLoader;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.FavoriteDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.player.PlayerEpgRepository;
import com.tviptv.app.data.prefs.AppPreferences;
import com.tviptv.app.data.repository.WatchHistoryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<FavoriteDao> favoriteDaoProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<PlayerEpgRepository> epgRepositoryProvider;

  private final Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public PlayerViewModel_Factory(Provider<ChannelDao> channelDaoProvider,
      Provider<SourceDao> sourceDaoProvider, Provider<FavoriteDao> favoriteDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<PlayerEpgRepository> epgRepositoryProvider,
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.channelDaoProvider = channelDaoProvider;
    this.sourceDaoProvider = sourceDaoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.epgRepositoryProvider = epgRepositoryProvider;
    this.seriesEpisodesLoaderProvider = seriesEpisodesLoaderProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(channelDaoProvider.get(), sourceDaoProvider.get(), favoriteDaoProvider.get(), watchHistoryRepositoryProvider.get(), epgRepositoryProvider.get(), seriesEpisodesLoaderProvider.get(), appPreferencesProvider.get());
  }

  public static PlayerViewModel_Factory create(Provider<ChannelDao> channelDaoProvider,
      Provider<SourceDao> sourceDaoProvider, Provider<FavoriteDao> favoriteDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<PlayerEpgRepository> epgRepositoryProvider,
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new PlayerViewModel_Factory(channelDaoProvider, sourceDaoProvider, favoriteDaoProvider, watchHistoryRepositoryProvider, epgRepositoryProvider, seriesEpisodesLoaderProvider, appPreferencesProvider);
  }

  public static PlayerViewModel newInstance(ChannelDao channelDao, SourceDao sourceDao,
      FavoriteDao favoriteDao, WatchHistoryRepository watchHistoryRepository,
      PlayerEpgRepository epgRepository, SeriesEpisodesLoader seriesEpisodesLoader,
      AppPreferences appPreferences) {
    return new PlayerViewModel(channelDao, sourceDao, favoriteDao, watchHistoryRepository, epgRepository, seriesEpisodesLoader, appPreferences);
  }
}
