package com.tviptv.app.ui.settings;

import com.tviptv.app.data.prefs.AppPreferences;
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
public final class SettingsFragment_MembersInjector implements MembersInjector<SettingsFragment> {
  private final Provider<AppPreferences> appPreferencesProvider;

  public SettingsFragment_MembersInjector(Provider<AppPreferences> appPreferencesProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
  }

  public static MembersInjector<SettingsFragment> create(
      Provider<AppPreferences> appPreferencesProvider) {
    return new SettingsFragment_MembersInjector(appPreferencesProvider);
  }

  @Override
  public void injectMembers(SettingsFragment instance) {
    injectAppPreferences(instance, appPreferencesProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.settings.SettingsFragment.appPreferences")
  public static void injectAppPreferences(SettingsFragment instance,
      AppPreferences appPreferences) {
    instance.appPreferences = appPreferences;
  }
}
