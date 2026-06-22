package com.tviptv.app.ui.categories;

import com.tviptv.app.data.cache.CategoryChannelsCache;
import com.tviptv.app.data.cache.CategoryContentLoader;
import com.tviptv.app.data.local.dao.ChannelDao;
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
public final class CategoryDetailViewModel_Factory implements Factory<CategoryDetailViewModel> {
  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<CategoryChannelsCache> categoryChannelsCacheProvider;

  private final Provider<CategoryContentLoader> contentLoaderProvider;

  public CategoryDetailViewModel_Factory(Provider<ChannelDao> channelDaoProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<CategoryContentLoader> contentLoaderProvider) {
    this.channelDaoProvider = channelDaoProvider;
    this.categoryChannelsCacheProvider = categoryChannelsCacheProvider;
    this.contentLoaderProvider = contentLoaderProvider;
  }

  @Override
  public CategoryDetailViewModel get() {
    return newInstance(channelDaoProvider.get(), categoryChannelsCacheProvider.get(), contentLoaderProvider.get());
  }

  public static CategoryDetailViewModel_Factory create(Provider<ChannelDao> channelDaoProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<CategoryContentLoader> contentLoaderProvider) {
    return new CategoryDetailViewModel_Factory(channelDaoProvider, categoryChannelsCacheProvider, contentLoaderProvider);
  }

  public static CategoryDetailViewModel newInstance(ChannelDao channelDao,
      CategoryChannelsCache categoryChannelsCache, CategoryContentLoader contentLoader) {
    return new CategoryDetailViewModel(channelDao, categoryChannelsCache, contentLoader);
  }
}
