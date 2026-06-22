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
public final class SectionFeedCache_Factory implements Factory<SectionFeedCache> {
  @Override
  public SectionFeedCache get() {
    return newInstance();
  }

  public static SectionFeedCache_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SectionFeedCache newInstance() {
    return new SectionFeedCache();
  }

  private static final class InstanceHolder {
    private static final SectionFeedCache_Factory INSTANCE = new SectionFeedCache_Factory();
  }
}
