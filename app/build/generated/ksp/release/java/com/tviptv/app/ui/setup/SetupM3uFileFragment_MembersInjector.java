package com.tviptv.app.ui.setup;

import com.tviptv.app.data.m3u.M3uRepository;
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
public final class SetupM3uFileFragment_MembersInjector implements MembersInjector<SetupM3uFileFragment> {
  private final Provider<SourceRepository> sourceRepositoryProvider;

  private final Provider<M3uRepository> m3uRepositoryProvider;

  public SetupM3uFileFragment_MembersInjector(Provider<SourceRepository> sourceRepositoryProvider,
      Provider<M3uRepository> m3uRepositoryProvider) {
    this.sourceRepositoryProvider = sourceRepositoryProvider;
    this.m3uRepositoryProvider = m3uRepositoryProvider;
  }

  public static MembersInjector<SetupM3uFileFragment> create(
      Provider<SourceRepository> sourceRepositoryProvider,
      Provider<M3uRepository> m3uRepositoryProvider) {
    return new SetupM3uFileFragment_MembersInjector(sourceRepositoryProvider, m3uRepositoryProvider);
  }

  @Override
  public void injectMembers(SetupM3uFileFragment instance) {
    injectSourceRepository(instance, sourceRepositoryProvider.get());
    injectM3uRepository(instance, m3uRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.setup.SetupM3uFileFragment.sourceRepository")
  public static void injectSourceRepository(SetupM3uFileFragment instance,
      SourceRepository sourceRepository) {
    instance.sourceRepository = sourceRepository;
  }

  @InjectedFieldSignature("com.tviptv.app.ui.setup.SetupM3uFileFragment.m3uRepository")
  public static void injectM3uRepository(SetupM3uFileFragment instance,
      M3uRepository m3uRepository) {
    instance.m3uRepository = m3uRepository;
  }
}
