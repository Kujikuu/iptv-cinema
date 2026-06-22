package com.tviptv.app.data.cache;

import com.tviptv.app.domain.repository.IptvRepositoryFactory;
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
public final class SeriesEpisodesLoader_Factory implements Factory<SeriesEpisodesLoader> {
  private final Provider<IptvRepositoryFactory> repositoryFactoryProvider;

  private final Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider;

  public SeriesEpisodesLoader_Factory(Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider) {
    this.repositoryFactoryProvider = repositoryFactoryProvider;
    this.seriesEpisodesCacheProvider = seriesEpisodesCacheProvider;
  }

  @Override
  public SeriesEpisodesLoader get() {
    return newInstance(repositoryFactoryProvider.get(), seriesEpisodesCacheProvider.get());
  }

  public static SeriesEpisodesLoader_Factory create(
      Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider) {
    return new SeriesEpisodesLoader_Factory(repositoryFactoryProvider, seriesEpisodesCacheProvider);
  }

  public static SeriesEpisodesLoader newInstance(IptvRepositoryFactory repositoryFactory,
      SeriesEpisodesCache seriesEpisodesCache) {
    return new SeriesEpisodesLoader(repositoryFactory, seriesEpisodesCache);
  }
}
