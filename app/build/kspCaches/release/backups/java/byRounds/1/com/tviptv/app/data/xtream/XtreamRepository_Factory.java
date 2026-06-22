package com.tviptv.app.data.xtream;

import com.tviptv.app.data.local.dao.CategoryDao;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.player.PlayerEpgRepository;
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
public final class XtreamRepository_Factory implements Factory<XtreamRepository> {
  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<CredentialStore> credentialStoreProvider;

  private final Provider<XtreamApi> xtreamApiProvider;

  private final Provider<PlayerEpgRepository> epgRepositoryProvider;

  public XtreamRepository_Factory(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<CredentialStore> credentialStoreProvider, Provider<XtreamApi> xtreamApiProvider,
      Provider<PlayerEpgRepository> epgRepositoryProvider) {
    this.sourceDaoProvider = sourceDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.channelDaoProvider = channelDaoProvider;
    this.credentialStoreProvider = credentialStoreProvider;
    this.xtreamApiProvider = xtreamApiProvider;
    this.epgRepositoryProvider = epgRepositoryProvider;
  }

  @Override
  public XtreamRepository get() {
    return newInstance(sourceDaoProvider.get(), categoryDaoProvider.get(), channelDaoProvider.get(), credentialStoreProvider.get(), xtreamApiProvider.get(), epgRepositoryProvider.get());
  }

  public static XtreamRepository_Factory create(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<CredentialStore> credentialStoreProvider, Provider<XtreamApi> xtreamApiProvider,
      Provider<PlayerEpgRepository> epgRepositoryProvider) {
    return new XtreamRepository_Factory(sourceDaoProvider, categoryDaoProvider, channelDaoProvider, credentialStoreProvider, xtreamApiProvider, epgRepositoryProvider);
  }

  public static XtreamRepository newInstance(SourceDao sourceDao, CategoryDao categoryDao,
      ChannelDao channelDao, CredentialStore credentialStore, XtreamApi xtreamApi,
      PlayerEpgRepository epgRepository) {
    return new XtreamRepository(sourceDao, categoryDao, channelDao, credentialStore, xtreamApi, epgRepository);
  }
}
