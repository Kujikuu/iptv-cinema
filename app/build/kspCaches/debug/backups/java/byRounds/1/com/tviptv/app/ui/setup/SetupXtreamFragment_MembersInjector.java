package com.tviptv.app.ui.setup;

import com.tviptv.app.data.repository.SourceRepository;
import com.tviptv.app.data.xtream.XtreamRepository;
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
public final class SetupXtreamFragment_MembersInjector implements MembersInjector<SetupXtreamFragment> {
  private final Provider<SourceRepository> sourceRepositoryProvider;

  private final Provider<XtreamRepository> xtreamRepositoryProvider;

  public SetupXtreamFragment_MembersInjector(Provider<SourceRepository> sourceRepositoryProvider,
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    this.sourceRepositoryProvider = sourceRepositoryProvider;
    this.xtreamRepositoryProvider = xtreamRepositoryProvider;
  }

  public static MembersInjector<SetupXtreamFragment> create(
      Provider<SourceRepository> sourceRepositoryProvider,
      Provider<XtreamRepository> xtreamRepositoryProvider) {
    return new SetupXtreamFragment_MembersInjector(sourceRepositoryProvider, xtreamRepositoryProvider);
  }

  @Override
  public void injectMembers(SetupXtreamFragment instance) {
    injectSourceRepository(instance, sourceRepositoryProvider.get());
    injectXtreamRepository(instance, xtreamRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.tviptv.app.ui.setup.SetupXtreamFragment.sourceRepository")
  public static void injectSourceRepository(SetupXtreamFragment instance,
      SourceRepository sourceRepository) {
    instance.sourceRepository = sourceRepository;
  }

  @InjectedFieldSignature("com.tviptv.app.ui.setup.SetupXtreamFragment.xtreamRepository")
  public static void injectXtreamRepository(SetupXtreamFragment instance,
      XtreamRepository xtreamRepository) {
    instance.xtreamRepository = xtreamRepository;
  }
}
