package com.tviptv.app.data.repository;

import com.tviptv.app.data.local.dao.CategoryDao;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.EpgDao;
import com.tviptv.app.data.local.dao.FavoriteDao;
import com.tviptv.app.data.local.dao.LastWatchedDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.prefs.CredentialStore;
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
public final class SourceRepository_Factory implements Factory<SourceRepository> {
  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<FavoriteDao> favoriteDaoProvider;

  private final Provider<LastWatchedDao> lastWatchedDaoProvider;

  private final Provider<EpgDao> epgDaoProvider;

  private final Provider<CredentialStore> credentialStoreProvider;

  public SourceRepository_Factory(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<FavoriteDao> favoriteDaoProvider, Provider<LastWatchedDao> lastWatchedDaoProvider,
      Provider<EpgDao> epgDaoProvider, Provider<CredentialStore> credentialStoreProvider) {
    this.sourceDaoProvider = sourceDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.channelDaoProvider = channelDaoProvider;
    this.favoriteDaoProvider = favoriteDaoProvider;
    this.lastWatchedDaoProvider = lastWatchedDaoProvider;
    this.epgDaoProvider = epgDaoProvider;
    this.credentialStoreProvider = credentialStoreProvider;
  }

  @Override
  public SourceRepository get() {
    return newInstance(sourceDaoProvider.get(), categoryDaoProvider.get(), channelDaoProvider.get(), favoriteDaoProvider.get(), lastWatchedDaoProvider.get(), epgDaoProvider.get(), credentialStoreProvider.get());
  }

  public static SourceRepository_Factory create(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<FavoriteDao> favoriteDaoProvider, Provider<LastWatchedDao> lastWatchedDaoProvider,
      Provider<EpgDao> epgDaoProvider, Provider<CredentialStore> credentialStoreProvider) {
    return new SourceRepository_Factory(sourceDaoProvider, categoryDaoProvider, channelDaoProvider, favoriteDaoProvider, lastWatchedDaoProvider, epgDaoProvider, credentialStoreProvider);
  }

  public static SourceRepository newInstance(SourceDao sourceDao, CategoryDao categoryDao,
      ChannelDao channelDao, FavoriteDao favoriteDao, LastWatchedDao lastWatchedDao, EpgDao epgDao,
      CredentialStore credentialStore) {
    return new SourceRepository(sourceDao, categoryDao, channelDao, favoriteDao, lastWatchedDao, epgDao, credentialStore);
  }
}
