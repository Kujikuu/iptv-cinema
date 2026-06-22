package com.tviptv.app.ui.browse;

import com.tviptv.app.data.cache.CategoryChannelsCache;
import com.tviptv.app.data.cache.HomeFeedCache;
import com.tviptv.app.data.cache.SectionFeedCache;
import com.tviptv.app.data.cache.SeriesEpisodesCache;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.FavoriteDao;
import com.tviptv.app.data.platform.WatchNextPublisher;
import com.tviptv.app.data.prefs.AppPreferences;
import com.tviptv.app.data.repository.SourceRefreshPolicy;
import com.tviptv.app.data.repository.SourceRepository;
import com.tviptv.app.data.repository.WatchHistoryRepository;
import com.tviptv.app.domain.repository.IptvRepositoryFactory;
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
public final class BrowseViewModel_Factory implements Factory<BrowseViewModel> {
  private final Provider<SourceRepository> sourceRepositoryProvider;

  private final Provider<IptvRepositoryFactory> repositoryFactoryProvider;

  private final Provider<SourceRefreshPolicy> sourceRefreshPolicyProvider;

  private final Provider<SectionFeedCache> sectionFeedCacheProvider;

  private final Provider<CategoryChannelsCache> categoryChannelsCacheProvider;

  private final Provider<HomeFeedCache> homeFeedCacheProvider;

  private final Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider;

  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<FavoriteDao> favoriteDaoProvider;

  private final Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  private final Provider<WatchNextPublisher> watchNextPublisherProvider;

  public BrowseViewModel_Factory(Provider<SourceRepository> sourceRepositoryProvider,
      Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<SourceRefreshPolicy> sourceRefreshPolicyProvider,
      Provider<SectionFeedCache> sectionFeedCacheProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<HomeFeedCache> homeFeedCacheProvider,
      Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider,
      Provider<ChannelDao> channelDaoProvider, Provider<FavoriteDao> favoriteDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider,
      Provider<WatchNextPublisher> watchNextPublisherProvider) {
    this.sourceRepositoryProvider = sourceRepositoryProvider;
    this.repositoryFactoryProvider = repositoryFactoryProvider;
    this.sourceRefreshPolicyProvider = sourceRefreshPolicyProvider;
    this.sectionFeedCacheProvider = sectionFeedCacheProvider;
    this.categoryChannelsCacheProvider = categoryChannelsCacheProvider;
    this.homeFeedCacheProvider = homeFeedCacheProvider;
    this.seriesEpisodesCacheProvider = seriesEpisodesCacheProvider;
    this.channelDaoProvider = channelDaoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.watchHistoryRepositoryProvider = watchHistoryRepositoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
    this.watchNextPublisherProvider = watchNextPublisherProvider;
  }

  @Override
  public BrowseViewModel get() {
    return newInstance(sourceRepositoryProvider.get(), repositoryFactoryProvider.get(), sourceRefreshPolicyProvider.get(), sectionFeedCacheProvider.get(), categoryChannelsCacheProvider.get(), homeFeedCacheProvider.get(), seriesEpisodesCacheProvider.get(), channelDaoProvider.get(), favoriteDaoProvider.get(), watchHistoryRepositoryProvider.get(), appPreferencesProvider.get(), watchNextPublisherProvider.get());
  }

  public static BrowseViewModel_Factory create(Provider<SourceRepository> sourceRepositoryProvider,
      Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<SourceRefreshPolicy> sourceRefreshPolicyProvider,
      Provider<SectionFeedCache> sectionFeedCacheProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<HomeFeedCache> homeFeedCacheProvider,
      Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider,
      Provider<ChannelDao> channelDaoProvider, Provider<FavoriteDao> favoriteDaoProvider,
      Provider<WatchHistoryRepository> watchHistoryRepositoryProvider,
      Provider<AppPreferences> appPreferencesProvider,
      Provider<WatchNextPublisher> watchNextPublisherProvider) {
    return new BrowseViewModel_Factory(sourceRepositoryProvider, repositoryFactoryProvider, sourceRefreshPolicyProvider, sectionFeedCacheProvider, categoryChannelsCacheProvider, homeFeedCacheProvider, seriesEpisodesCacheProvider, channelDaoProvider, favoriteDaoProvider, watchHistoryRepositoryProvider, appPreferencesProvider, watchNextPublisherProvider);
  }

  public static BrowseViewModel newInstance(SourceRepository sourceRepository,
      IptvRepositoryFactory repositoryFactory, SourceRefreshPolicy sourceRefreshPolicy,
      SectionFeedCache sectionFeedCache, CategoryChannelsCache categoryChannelsCache,
      HomeFeedCache homeFeedCache, SeriesEpisodesCache seriesEpisodesCache, ChannelDao channelDao,
      FavoriteDao favoriteDao, WatchHistoryRepository watchHistoryRepository,
      AppPreferences appPreferences, WatchNextPublisher watchNextPublisher) {
    return new BrowseViewModel(sourceRepository, repositoryFactory, sourceRefreshPolicy, sectionFeedCache, categoryChannelsCache, homeFeedCache, seriesEpisodesCache, channelDao, favoriteDao, watchHistoryRepository, appPreferences, watchNextPublisher);
  }
}
