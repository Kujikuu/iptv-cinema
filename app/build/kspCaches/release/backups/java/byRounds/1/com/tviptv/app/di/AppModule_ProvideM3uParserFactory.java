package com.tviptv.app.di;

import com.tviptv.app.data.m3u.M3uParser;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideM3uParserFactory implements Factory<M3uParser> {
  @Override
  public M3uParser get() {
    return provideM3uParser();
  }

  public static AppModule_ProvideM3uParserFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static M3uParser provideM3uParser() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideM3uParser());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideM3uParserFactory INSTANCE = new AppModule_ProvideM3uParserFactory();
  }
}
