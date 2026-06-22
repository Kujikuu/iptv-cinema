package com.tviptv.app.domain.repository;

import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.m3u.M3uRepository;
import com.tviptv.app.data.xtream.XtreamRepository;
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
public final class IptvRepositoryFactory_Factory implements Factory<IptvRepositoryFactory> {
  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<M3uRepository> m3uRepositoryProvider;

  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  public IptvRepositoryFactory_Factory(Provider<SourceDao> sourceDaoProvider,
      Provider<M3uRepository> m3uRepositoryProvider,
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.sourceDaoProvider = sourceDaoProvider;
    this.m3uRepositoryProvider = m3uRepositoryProvider;
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  @Override
  public IptvRepositoryFactory get() {
    return newInstance(sourceDaoProvider.get(), m3uRepositoryProvider.get(), xtreamRepositoryProvider.get());
  }

  public static IptvRepositoryFactory_Factory create(Provider<SourceDao> sourceDaoProvider,
      Provider<M3uRepository> m3uRepositoryProvider,
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new IptvRepositoryFactory_Factory(sourceDaoProvider, m3uRepositoryProvider, xtreamRepositoryProvider);
  }

  public static IptvRepositoryFactory newInstance(SourceDao sourceDao, M3uRepository m3uRepository,
      XtreamRepository xtreamRepository) {
    return new IptvRepositoryFactory(sourceDao, m3uRepository, xtreamRepository);
  }
}
