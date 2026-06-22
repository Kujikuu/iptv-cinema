package com.tviptv.app.ui.search;

import com.tviptv.app.data.local.ChannelSearchHelper;
import com.tviptv.app.data.repository.SourceRepository;
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
public final class SearchViewModel_Factory implements Factory<SearchViewModel> {
  private final Provider<ChannelSearchHelper> channelSearchHelperProvider;

  private final Provider<SourceRepository> sourceRepositoryProvider;

  public SearchViewModel_Factory(Provider<ChannelSearchHelper> channelSearchHelperProvider,
      Provider<SourceRepository> sourceRepositoryProvider) {
    this.channelSearchHelperProvider = channelSearchHelperProvider;
    this.sourceRepositoryProvider = sourceRepositoryProvider;
  }

  @Override
  public SearchViewModel get() {
    return newInstance(channelSearchHelperProvider.get(), sourceRepositoryProvider.get());
  }

  public static SearchViewModel_Factory create(
      Provider<ChannelSearchHelper> channelSearchHelperProvider,
      Provider<SourceRepository> sourceRepositoryProvider) {
    return new SearchViewModel_Factory(channelSearchHelperProvider, sourceRepositoryProvider);
  }

  public static SearchViewModel newInstance(ChannelSearchHelper channelSearchHelper,
      SourceRepository sourceRepository) {
    return new SearchViewModel(channelSearchHelper, sourceRepository);
  }
}
