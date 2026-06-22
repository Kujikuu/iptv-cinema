package com.tviptv.app.data.player;

import com.tviptv.app.data.local.dao.EpgDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.prefs.CredentialStore;
import com.tviptv.app.data.xtream.XtreamApi;
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
public final class PlayerEpgRepository_Factory implements Factory<PlayerEpgRepository> {
  private final Provider<EpgDao> epgDaoProvider;

  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<CredentialStore> credentialStoreProvider;

  private final Provider<XtreamApi> xtreamApiProvider;

  public PlayerEpgRepository_Factory(Provider<EpgDao> epgDaoProvider,
      Provider<SourceDao> sourceDaoProvider, Provider<CredentialStore> credentialStoreProvider,
      Provider<XtreamApi> xtreamApiProvider) {
    this.epgDaoProvider = epgDaoProvider;
    this.sourceDaoProvider = sourceDaoProvider;
    this.credentialStoreProvider = credentialStoreProvider;
    this.xtreamApiProvider = xtreamApiProvider;
  }

  @Override
  public PlayerEpgRepository get() {
    return newInstance(epgDaoProvider.get(), sourceDaoProvider.get(), credentialStoreProvider.get(), xtreamApiProvider.get());
  }

  public static PlayerEpgRepository_Factory create(Provider<EpgDao> epgDaoProvider,
      Provider<SourceDao> sourceDaoProvider, Provider<CredentialStore> credentialStoreProvider,
      Provider<XtreamApi> xtreamApiProvider) {
    return new PlayerEpgRepository_Factory(epgDaoProvider, sourceDaoProvider, credentialStoreProvider, xtreamApiProvider);
  }

  public static PlayerEpgRepository newInstance(EpgDao epgDao, SourceDao sourceDao,
      CredentialStore credentialStore, XtreamApi xtreamApi) {
    return new PlayerEpgRepository(epgDao, sourceDao, credentialStore, xtreamApi);
  }
}
