package com.tviptv.app.ui.details;

import com.tviptv.app.data.cache.SeriesEpisodesLoader;
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
public final class SeriesDetailsViewModel_Factory implements Factory<SeriesDetailsViewModel> {
  private final Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider;

  public SeriesDetailsViewModel_Factory(
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    this.seriesEpisodesLoaderProvider = seriesEpisodesLoaderProvider;
  }

  @Override
  public SeriesDetailsViewModel get() {
    return newInstance(seriesEpisodesLoaderProvider.get());
  }

  public static SeriesDetailsViewModel_Factory create(
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    return new SeriesDetailsViewModel_Factory(seriesEpisodesLoaderProvider);
  }

  public static SeriesDetailsViewModel newInstance(SeriesEpisodesLoader seriesEpisodesLoader) {
    return new SeriesDetailsViewModel(seriesEpisodesLoader);
  }
}
