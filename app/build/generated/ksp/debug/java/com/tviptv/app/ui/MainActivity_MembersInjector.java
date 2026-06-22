package com.tviptv.app.ui;

import com.tviptv.app.data.repository.SourceRepository;
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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<SourceRepository> sourceRepositoryProvider;

  public MainActivity_MembersInjector(Provider<SourceRepository> sourceRepositoryProvider) {
    this.sourceRepositoryProvider = sourceRepositoryProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<SourceRepository> sourceRepositoryProvider) {
    return new MainActivity_MembersInjector(sourceRepositoryProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectSourceRepository(instance, sourceRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.MainActivity.sourceRepository")
  public static void injectSourceRepository(MainActivity instance,
      SourceRepository sourceRepository) {
    instance.sourceRepository = sourceRepository;
  }
}
