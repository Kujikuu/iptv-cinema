package com.tviptv.app.ui.categories;

import com.tviptv.app.data.cache.CategoryPrefetcher;
import com.tviptv.app.ui.browse.ContinueWatchingNavigator;
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
public final class CategoryGridFragment_MembersInjector implements MembersInjector<CategoryGridFragment> {
  private final Provider<CategoryPrefetcher> categoryPrefetcherProvider;

  private final Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider;

  public CategoryGridFragment_MembersInjector(
      Provider<CategoryPrefetcher> categoryPrefetcherProvider,
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    this.categoryPrefetcherProvider = categoryPrefetcherProvider;
    this.continueWatchingNavigatorProvider = continueWatchingNavigatorProvider;
  }

  public static MembersInjector<CategoryGridFragment> create(
      Provider<CategoryPrefetcher> categoryPrefetcherProvider,
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    return new CategoryGridFragment_MembersInjector(categoryPrefetcherProvider, continueWatchingNavigatorProvider);
  }

  @Override
  public void injectMembers(CategoryGridFragment instance) {
    injectCategoryPrefetcher(instance, categoryPrefetcherProvider.get());
    injectContinueWatchingNavigator(instance, continueWatchingNavigatorProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.categories.CategoryGridFragment.categoryPrefetcher")
  public static void injectCategoryPrefetcher(CategoryGridFragment instance,
      CategoryPrefetcher categoryPrefetcher) {
    instance.categoryPrefetcher = categoryPrefetcher;
  }

  @InjectedFieldSignature("com.tviptv.app.ui.categories.CategoryGridFragment.continueWatchingNavigator")
  public static void injectContinueWatchingNavigator(CategoryGridFragment instance,
      ContinueWatchingNavigator continueWatchingNavigator) {
    instance.continueWatchingNavigator = continueWatchingNavigator;
  }
}
