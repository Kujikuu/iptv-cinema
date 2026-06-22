package com.tviptv.app.data.local;

import com.tviptv.app.data.local.dao.ChannelDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ChannelSearchHelper_Factory implements Factory<ChannelSearchHelper> {
  private final Provider<ChannelDao> channelDaoProvider;

  public ChannelSearchHelper_Factory(Provider<ChannelDao> channelDaoProvider) {
    this.channelDaoProvider = channelDaoProvider;
  }

  @Override
  public ChannelSearchHelper get() {
    return newInstance(channelDaoProvider.get());
  }

  public static ChannelSearchHelper_Factory create(Provider<ChannelDao> channelDaoProvider) {
    return new ChannelSearchHelper_Factory(channelDaoProvider);
  }

  public static ChannelSearchHelper newInstance(ChannelDao channelDao) {
    return new ChannelSearchHelper(channelDao);
  }
}
