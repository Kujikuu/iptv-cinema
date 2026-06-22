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
public final class HomeFeedCache_Factory implements Factory<HomeFeedCache> {
  @Override
  public HomeFeedCache get() {
    return newInstance();
  }

  public static HomeFeedCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HomeFeedCache newInstance() {
    return new HomeFeedCache();
  }

  private static final class InstanceHolder {
    private static final HomeFeedCache_Factory INSTANCE = new HomeFeedCache_Factory();
  }
}
