package com.tviptv.app.ui.categories;

import com.tviptv.app.data.cache.SeriesEpisodesLoader;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class CategoryDetailFragment_MembersInjector implements MembersInjector<CategoryDetailFragment> {
  private final Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider;

  public CategoryDetailFragment_MembersInjector(
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    this.seriesEpisodesLoaderProvider = seriesEpisodesLoaderProvider;
  }

  public static MembersInjector<CategoryDetailFragment> create(
      Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider) {
    return new CategoryDetailFragment_MembersInjector(seriesEpisodesLoaderProvider);
  }

  @Override
  public void injectMembers(CategoryDetailFragment instance) {
    injectSeriesEpisodesLoader(instance, seriesEpisodesLoaderProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.categories.CategoryDetailFragment.seriesEpisodesLoader")
  public static void injectSeriesEpisodesLoader(CategoryDetailFragment instance,
      SeriesEpisodesLoader seriesEpisodesLoader) {
    instance.seriesEpisodesLoader = seriesEpisodesLoader;
  }
}
