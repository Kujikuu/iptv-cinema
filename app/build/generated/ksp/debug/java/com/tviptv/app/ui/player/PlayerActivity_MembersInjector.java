package com.tviptv.app.ui.player;

import com.tviptv.app.data.prefs.AppPreferences;
import com.tviptv.app.domain.repository.IptvRepositoryFactory;
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
public final class PlayerActivity_MembersInjector implements MembersInjector<PlayerActivity> {
  private final Provider<IptvRepositoryFactory> repositoryFactoryProvider;

  private final Provider<AppPreferences> appPreferencesProvider;

  public PlayerActivity_MembersInjector(Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    this.repositoryFactoryProvider = repositoryFactoryProvider;
    this.appPreferencesProvider = appPreferencesProvider;
  }

  public static MembersInjector<PlayerActivity> create(
      Provider<IptvRepositoryFactory> repositoryFactoryProvider,
      Provider<AppPreferences> appPreferencesProvider) {
    return new PlayerActivity_MembersInjector(repositoryFactoryProvider, appPreferencesProvider);
  }

  @Override
  public void injectMembers(PlayerActivity instance) {
    injectRepositoryFactory(instance, repositoryFactoryProvider.get());
    injectAppPreferences(instance, appPreferencesProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.player.PlayerActivity.repositoryFactory")
  public static void injectRepositoryFactory(PlayerActivity instance,
      IptvRepositoryFactory repositoryFactory) {
    instance.repositoryFactory = repositoryFactory;
  }

  @InjectedFieldSignature("com.tviptv.app.ui.player.PlayerActivity.appPreferences")
  public static void injectAppPreferences(PlayerActivity instance, AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }
}
