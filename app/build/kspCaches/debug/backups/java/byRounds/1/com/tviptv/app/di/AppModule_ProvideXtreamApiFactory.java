package com.tviptv.app.di;

import com.squareup.moshi.Moshi;
import com.tviptv.app.data.xtream.XtreamApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

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
public final class AppModule_ProvideXtreamApiFactory implements Factory<XtreamApi> {
  private final Provider<Moshi> moshiProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public AppModule_ProvideXtreamApiFactory(Provider<Moshi> moshiProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.moshiProvider = moshiProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public XtreamApi get() {
    return provideXtreamApi(moshiProvider.get(), okHttpClientProvider.get());
  }

  public static AppModule_ProvideXtreamApiFactory create(Provider<Moshi> moshiProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new AppModule_ProvideXtreamApiFactory(moshiProvider, okHttpClientProvider);
  }

  public static XtreamApi provideXtreamApi(Moshi moshi, OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideXtreamApi(moshi, okHttpClient));
  }
}
