package com.tviptv.app.data.repository;

import com.tviptv.app.data.prefs.AppPreferences;
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
public final class SourceRefreshPolicy_Factory implements Factory<SourceRefreshPolicy> {
  private final Provider<AppPreferences> appPreferencesProvider;

  public SourceRefreshPolicy_Factory(Provider<AppPreferences> appPreferencesProvider) {
    this.appPreferencesProvider = appPreferencesProvider;
  }

  @Override
  public SourceRefreshPolicy get() {
    return newInstance(appPreferencesProvider.get());
  }

  public static SourceRefreshPolicy_Factory create(
      Provider<AppPreferences> appPreferencesProvider) {
    return new SourceRefreshPolicy_Factory(appPreferencesProvider);
  }

  public static SourceRefreshPolicy newInstance(AppPreferences appPreferences) {
    return new SourceRefreshPolicy(appPreferences);
  }
}
