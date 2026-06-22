package com.tviptv.app.data.cache;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CategoryPrefetcher_Factory implements Factory<CategoryPrefetcher> {
  private final Provider<CategoryContentLoader> contentLoaderProvider;

  private final Provider<CategoryChannelsCache> categoryChannelsCacheProvider;

  private final Provider<Context> contextProvider;

  public CategoryPrefetcher_Factory(Provider<CategoryContentLoader> contentLoaderProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<Context> contextProvider) {
    this.contentLoaderProvider = contentLoaderProvider;
    this.categoryChannelsCacheProvider = categoryChannelsCacheProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public CategoryPrefetcher get() {
    return newInstance(contentLoaderProvider.get(), categoryChannelsCacheProvider.get(), contextProvider.get());
  }

  public static CategoryPrefetcher_Factory create(
      Provider<CategoryContentLoader> contentLoaderProvider,
      Provider<CategoryChannelsCache> categoryChannelsCacheProvider,
      Provider<Context> contextProvider) {
    return new CategoryPrefetcher_Factory(contentLoaderProvider, categoryChannelsCacheProvider, contextProvider);
  }

  public static CategoryPrefetcher newInstance(CategoryContentLoader contentLoader,
      CategoryChannelsCache categoryChannelsCache, Context context) {
    return new CategoryPrefetcher(contentLoader, categoryChannelsCache, context);
  }
}
