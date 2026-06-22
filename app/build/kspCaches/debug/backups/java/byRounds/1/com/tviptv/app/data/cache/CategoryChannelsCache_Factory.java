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
public final class CategoryChannelsCache_Factory implements Factory<CategoryChannelsCache> {
  @Override
  public CategoryChannelsCache get() {
    return newInstance();
  }

  public static CategoryChannelsCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CategoryChannelsCache newInstance() {
    return new CategoryChannelsCache();
  }

  private static final class InstanceHolder {
    private static final CategoryChannelsCache_Factory INSTANCE = new CategoryChannelsCache_Factory();
  }
}
