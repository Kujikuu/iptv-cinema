package com.tviptv.app.ui.launcher;

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
public final class LauncherHomeFragment_MembersInjector implements MembersInjector<LauncherHomeFragment> {
  private final Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider;

  public LauncherHomeFragment_MembersInjector(
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    this.continueWatchingNavigatorProvider = continueWatchingNavigatorProvider;
  }

  public static MembersInjector<LauncherHomeFragment> create(
      Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider) {
    return new LauncherHomeFragment_MembersInjector(continueWatchingNavigatorProvider);
  }

  @Override
  public void injectMembers(LauncherHomeFragment instance) {
    injectContinueWatchingNavigator(instance, continueWatchingNavigatorProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.launcher.LauncherHomeFragment.continueWatchingNavigator")
  public static void injectContinueWatchingNavigator(LauncherHomeFragment instance,
      ContinueWatchingNavigator continueWatchingNavigator) {
    instance.continueWatchingNavigator = continueWatchingNavigator;
  }
}
