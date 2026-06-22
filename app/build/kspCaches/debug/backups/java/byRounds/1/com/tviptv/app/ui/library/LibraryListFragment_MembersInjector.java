package com.tviptv.app.ui.library;

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
public final class LibraryListFragment_MembersInjector implements MembersInjector<LibraryListFragment> {
  private final Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider;

  public LibraryListFragment_MembersInjector(
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    this.continueWatchingNavigatorProvider = continueWatchingNavigatorProvider;
  }

  public static MembersInjector<LibraryListFragment> create(
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    return new LibraryListFragment_MembersInjector(continueWatchingNavigatorProvider);
  }

  @Override
  public void injectMembers(LibraryListFragment instance) {
    injectContinueWatchingNavigator(instance, continueWatchingNavigatorProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.library.LibraryListFragment.continueWatchingNavigator")
  public static void injectContinueWatchingNavigator(LibraryListFragment instance,
      ContinueWatchingNavigator continueWatchingNavigator) {
    instance.continueWatchingNavigator = continueWatchingNavigator;
  }
}
