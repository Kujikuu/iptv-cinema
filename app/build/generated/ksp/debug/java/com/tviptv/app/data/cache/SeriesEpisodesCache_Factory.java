package com.tviptv.app.data.cache;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SeriesEpisodesCache_Factory implements Factory<SeriesEpisodesCache> {
  @Override
  public SeriesEpisodesCache get() {
    return newInstance();
  }

  public static SeriesEpisodesCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SeriesEpisodesCache newInstance() {
    return new SeriesEpisodesCache();
  }

  private static final class InstanceHolder {
    private static final SeriesEpisodesCache_Factory INSTANCE = new SeriesEpisodesCache_Factory();
  }
}
