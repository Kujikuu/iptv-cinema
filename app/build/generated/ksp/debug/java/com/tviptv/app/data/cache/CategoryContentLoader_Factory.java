package com.tviptv.app.data.cache;

import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.FavoriteDao;
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
public final class CategoryContentLoader_Factory implements Factory<CategoryContentLoader> {
  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<FavoriteDao> favoriteDaoProvider;

  private final Provider<CategoryChannelsCache> categoryChannelsCacheProvider;

  public CategoryContentLoader_Factory(Provider<ChannelDao> channelDaoProvider,
      Provider<FavoriteDao> favoriteDaoProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider) {
    this.channelDaoProvider = channelDaoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.categoryChannelsCacheProvider = categoryChannelsCacheProvider;
  }

  @Override
  public CategoryContentLoader get() {
    return newInstance(channelDaoProvider.get(), favoriteDaoProvider.get(), categoryChannelsCacheProvider.get());
  }

  public static CategoryContentLoader_Factory create(Provider<ChannelDao> channelDaoProvider,
      Provider<FavoriteDao> favoriteDaoProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider) {
    return new CategoryContentLoader_Factory(channelDaoProvider, favoriteDaoProvider, categoryChannelsCacheProvider);
  }

  public static CategoryContentLoader newInstance(ChannelDao channelDao, FavoriteDao favoriteDao,
      CategoryChannelsCache categoryChannelsCache) {
    return new CategoryContentLoader(channelDao, favoriteDao, categoryChannelsCache);
  }
}
