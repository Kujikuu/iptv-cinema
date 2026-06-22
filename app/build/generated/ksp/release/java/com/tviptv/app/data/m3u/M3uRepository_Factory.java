package com.tviptv.app.data.m3u;

import com.tviptv.app.data.local.dao.CategoryDao;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.SourceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class M3uRepository_Factory implements Factory<M3uRepository> {
  private final Provider<SourceDao> sourceDaoProvider;

  private final Provider<CategoryDao> categoryDaoProvider;

  private final Provider<ChannelDao> channelDaoProvider;

  private final Provider<M3uParser> parserProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public M3uRepository_Factory(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<M3uParser> parserProvider, Provider<OkHttpClient> okHttpClientProvider) {
    this.sourceDaoProvider = sourceDaoProvider;
    this.categoryDaoProvider = categoryDaoProvider;
    this.channelDaoProvider = channelDaoProvider;
    this.parserProvider = parserProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public M3uRepository get() {
    return newInstance(sourceDaoProvider.get(), categoryDaoProvider.get(), channelDaoProvider.get(), parserProvider.get(), okHttpClientProvider.get());
  }

  public static M3uRepository_Factory create(Provider<SourceDao> sourceDaoProvider,
      Provider<CategoryDao> categoryDaoProvider, Provider<ChannelDao> channelDaoProvider,
      Provider<M3uParser> parserProvider, Provider<OkHttpClient> okHttpClientProvider) {
    return new M3uRepository_Factory(sourceDaoProvider, categoryDaoProvider, channelDaoProvider, parserProvider, okHttpClientProvider);
  }

  public static M3uRepository newInstance(SourceDao sourceDao, CategoryDao categoryDao,
      ChannelDao channelDao, M3uParser parser, OkHttpClient okHttpClient) {
    return new M3uRepository(sourceDao, categoryDao, channelDao, parser, okHttpClient);
  }
}
