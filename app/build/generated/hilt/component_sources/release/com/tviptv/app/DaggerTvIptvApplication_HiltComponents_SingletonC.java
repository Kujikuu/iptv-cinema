package com.tviptv.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.moshi.Moshi;
import com.tviptv.app.data.cache.CategoryChannelsCache;
import com.tviptv.app.data.cache.CategoryContentLoader;
import com.tviptv.app.data.cache.CategoryPrefetcher;
import com.tviptv.app.data.cache.HomeFeedCache;
import com.tviptv.app.data.cache.SectionFeedCache;
import com.tviptv.app.data.cache.SeriesEpisodesCache;
import com.tviptv.app.data.cache.SeriesEpisodesLoader;
import com.tviptv.app.data.local.AppDatabase;
import com.tviptv.app.data.local.ChannelSearchHelper;
import com.tviptv.app.data.local.dao.CategoryDao;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.EpgDao;
import com.tviptv.app.data.local.dao.FavoriteDao;
import com.tviptv.app.data.local.dao.LastWatchedDao;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.m3u.M3uParser;
import com.tviptv.app.data.m3u.M3uRepository;
import com.tviptv.app.data.platform.WatchNextPublisher;
import com.tviptv.app.data.player.PlayerEpgRepository;
import com.tviptv.app.data.prefs.AppPreferences;
import com.tviptv.app.data.prefs.CredentialStore;
import com.tviptv.app.data.repository.SourceRefreshPolicy;
import com.tviptv.app.data.repository.SourceRepository;
import com.tviptv.app.data.repository.WatchHistoryRepository;
import com.tviptv.app.data.xtream.XtreamApi;
import com.tviptv.app.data.xtream.XtreamRepository;
import com.tviptv.app.di.AppModule_ProvideCategoryDaoFactory;
import com.tviptv.app.di.AppModule_ProvideChannelDaoFactory;
import com.tviptv.app.di.AppModule_ProvideDatabaseFactory;
import com.tviptv.app.di.AppModule_ProvideEpgDaoFactory;
import com.tviptv.app.di.AppModule_ProvideFavoriteDaoFactory;
import com.tviptv.app.di.AppModule_ProvideLastWatchedDaoFactory;
import com.tviptv.app.di.AppModule_ProvideM3uParserFactory;
import com.tviptv.app.di.AppModule_ProvideMoshiFactory;
import com.tviptv.app.di.AppModule_ProvideOkHttpClientFactory;
import com.tviptv.app.di.AppModule_ProvideSourceDaoFactory;
import com.tviptv.app.di.AppModule_ProvideXtreamApiFactory;
import com.tviptv.app.domain.repository.IptvRepositoryFactory;
import com.tviptv.app.ui.MainActivity;
import com.tviptv.app.ui.MainActivity_MembersInjector;
import com.tviptv.app.ui.browse.BrowseViewModel;
import com.tviptv.app.ui.browse.BrowseViewModel_HiltModules;
import com.tviptv.app.ui.browse.ContinueWatchingNavigator;
import com.tviptv.app.ui.categories.CategoryDetailFragment;
import com.tviptv.app.ui.categories.CategoryDetailFragment_MembersInjector;
import com.tviptv.app.ui.categories.CategoryDetailViewModel;
import com.tviptv.app.ui.categories.CategoryDetailViewModel_HiltModules;
import com.tviptv.app.ui.categories.CategoryGridFragment;
import com.tviptv.app.ui.categories.CategoryGridFragment_MembersInjector;
import com.tviptv.app.ui.details.ChannelDetailsFragment;
import com.tviptv.app.ui.details.SeriesDetailsFragment;
import com.tviptv.app.ui.details.SeriesDetailsViewModel;
import com.tviptv.app.ui.details.SeriesDetailsViewModel_HiltModules;
import com.tviptv.app.ui.launcher.LauncherHomeFragment;
import com.tviptv.app.ui.launcher.LauncherHomeFragment_MembersInjector;
import com.tviptv.app.ui.library.LibraryListFragment;
import com.tviptv.app.ui.library.LibraryListFragment_MembersInjector;
import com.tviptv.app.ui.player.PlayerActivity;
import com.tviptv.app.ui.player.PlayerActivity_MembersInjector;
import com.tviptv.app.ui.player.PlayerViewModel;
import com.tviptv.app.ui.player.PlayerViewModel_HiltModules;
import com.tviptv.app.ui.search.ChannelSearchFragment;
import com.tviptv.app.ui.search.SearchViewModel;
import com.tviptv.app.ui.search.SearchViewModel_HiltModules;
import com.tviptv.app.ui.settings.SettingsFragment;
import com.tviptv.app.ui.settings.SettingsFragment_MembersInjector;
import com.tviptv.app.ui.setup.SetupActivity;
import com.tviptv.app.ui.setup.SetupM3uFileFragment;
import com.tviptv.app.ui.setup.SetupM3uFileFragment_MembersInjector;
import com.tviptv.app.ui.setup.SetupM3uFragment;
import com.tviptv.app.ui.setup.SetupM3uFragment_MembersInjector;
import com.tviptv.app.ui.setup.SetupXtreamFragment;
import com.tviptv.app.ui.setup.SetupXtreamFragment_MembersInjector;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class DaggerTvIptvApplication_HiltComponents_SingletonC {
  private DaggerTvIptvApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public TvIptvApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements TvIptvApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements TvIptvApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements TvIptvApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements TvIptvApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements TvIptvApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements TvIptvApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements TvIptvApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public TvIptvApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends TvIptvApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends TvIptvApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public void injectCategoryDetailFragment(CategoryDetailFragment categoryDetailFragment) {
      injectCategoryDetailFragment2(categoryDetailFragment);
    }

    @Override
    public void injectCategoryGridFragment(CategoryGridFragment categoryGridFragment) {
      injectCategoryGridFragment2(categoryGridFragment);
    }

    @Override
    public void injectChannelDetailsFragment(ChannelDetailsFragment channelDetailsFragment) {
    }

    @Override
    public void injectSeriesDetailsFragment(SeriesDetailsFragment seriesDetailsFragment) {
    }

    @Override
    public void injectLauncherHomeFragment(LauncherHomeFragment launcherHomeFragment) {
      injectLauncherHomeFragment2(launcherHomeFragment);
    }

    @Override
    public void injectLibraryListFragment(LibraryListFragment libraryListFragment) {
      injectLibraryListFragment2(libraryListFragment);
    }

    @Override
    public void injectChannelSearchFragment(ChannelSearchFragment channelSearchFragment) {
    }

    @Override
    public void injectSettingsFragment(SettingsFragment settingsFragment) {
      injectSettingsFragment2(settingsFragment);
    }

    @Override
    public void injectSetupM3uFileFragment(SetupM3uFileFragment setupM3uFileFragment) {
      injectSetupM3uFileFragment2(setupM3uFileFragment);
    }

    @Override
    public void injectSetupM3uFragment(SetupM3uFragment setupM3uFragment) {
      injectSetupM3uFragment2(setupM3uFragment);
    }

    @Override
    public void injectSetupXtreamFragment(SetupXtreamFragment setupXtreamFragment) {
      injectSetupXtreamFragment2(setupXtreamFragment);
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }

    private CategoryDetailFragment injectCategoryDetailFragment2(CategoryDetailFragment instance) {
      CategoryDetailFragment_MembersInjector.injectSeriesEpisodesLoader(instance, singletonCImpl.seriesEpisodesLoaderProvider.get());
      return instance;
    }

    private CategoryGridFragment injectCategoryGridFragment2(CategoryGridFragment instance2) {
      CategoryGridFragment_MembersInjector.injectCategoryPrefetcher(instance2, singletonCImpl.categoryPrefetcherProvider.get());
      CategoryGridFragment_MembersInjector.injectContinueWatchingNavigator(instance2, singletonCImpl.continueWatchingNavigatorProvider.get());
      return instance2;
    }

    private LauncherHomeFragment injectLauncherHomeFragment2(LauncherHomeFragment instance3) {
      LauncherHomeFragment_MembersInjector.injectContinueWatchingNavigator(instance3, singletonCImpl.continueWatchingNavigatorProvider.get());
      return instance3;
    }

    private LibraryListFragment injectLibraryListFragment2(LibraryListFragment instance4) {
      LibraryListFragment_MembersInjector.injectContinueWatchingNavigator(instance4, singletonCImpl.continueWatchingNavigatorProvider.get());
      return instance4;
    }

    private SettingsFragment injectSettingsFragment2(SettingsFragment instance5) {
      SettingsFragment_MembersInjector.injectAppPreferences(instance5, singletonCImpl.appPreferencesProvider.get());
      return instance5;
    }

    private SetupM3uFileFragment injectSetupM3uFileFragment2(SetupM3uFileFragment instance6) {
      SetupM3uFileFragment_MembersInjector.injectSourceRepository(instance6, singletonCImpl.sourceRepositoryProvider.get());
      SetupM3uFileFragment_MembersInjector.injectM3uRepository(instance6, singletonCImpl.m3uRepositoryProvider.get());
      return instance6;
    }

    private SetupM3uFragment injectSetupM3uFragment2(SetupM3uFragment instance7) {
      SetupM3uFragment_MembersInjector.injectSourceRepository(instance7, singletonCImpl.sourceRepositoryProvider.get());
      SetupM3uFragment_MembersInjector.injectM3uRepository(instance7, singletonCImpl.m3uRepositoryProvider.get());
      return instance7;
    }

    private SetupXtreamFragment injectSetupXtreamFragment2(SetupXtreamFragment instance8) {
      SetupXtreamFragment_MembersInjector.injectSourceRepository(instance8, singletonCImpl.sourceRepositoryProvider.get());
      SetupXtreamFragment_MembersInjector.injectXtreamRepository(instance8, singletonCImpl.xtreamRepositoryProvider.get());
      return instance8;
    }
  }

  private static final class ViewCImpl extends TvIptvApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends TvIptvApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
      injectMainActivity2(mainActivity);
    }

    @Override
    public void injectPlayerActivity(PlayerActivity playerActivity) {
      injectPlayerActivity2(playerActivity);
    }

    @Override
    public void injectSetupActivity(SetupActivity setupActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(ImmutableMap.<String, Boolean>of(LazyClassKeyProvider.com_tviptv_app_ui_browse_BrowseViewModel, BrowseViewModel_HiltModules.KeyModule.provide(), LazyClassKeyProvider.com_tviptv_app_ui_categories_CategoryDetailViewModel, CategoryDetailViewModel_HiltModules.KeyModule.provide(), LazyClassKeyProvider.com_tviptv_app_ui_player_PlayerViewModel, PlayerViewModel_HiltModules.KeyModule.provide(), LazyClassKeyProvider.com_tviptv_app_ui_search_SearchViewModel, SearchViewModel_HiltModules.KeyModule.provide(), LazyClassKeyProvider.com_tviptv_app_ui_details_SeriesDetailsViewModel, SeriesDetailsViewModel_HiltModules.KeyModule.provide()));
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    private MainActivity injectMainActivity2(MainActivity instance) {
      MainActivity_MembersInjector.injectSourceRepository(instance, singletonCImpl.sourceRepositoryProvider.get());
      return instance;
    }

    private PlayerActivity injectPlayerActivity2(PlayerActivity instance2) {
      PlayerActivity_MembersInjector.injectRepositoryFactory(instance2, singletonCImpl.iptvRepositoryFactoryProvider.get());
      PlayerActivity_MembersInjector.injectAppPreferences(instance2, singletonCImpl.appPreferencesProvider.get());
      return instance2;
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_tviptv_app_ui_browse_BrowseViewModel = "com.tviptv.app.ui.browse.BrowseViewModel";

      static String com_tviptv_app_ui_player_PlayerViewModel = "com.tviptv.app.ui.player.PlayerViewModel";

      static String com_tviptv_app_ui_search_SearchViewModel = "com.tviptv.app.ui.search.SearchViewModel";

      static String com_tviptv_app_ui_details_SeriesDetailsViewModel = "com.tviptv.app.ui.details.SeriesDetailsViewModel";

      static String com_tviptv_app_ui_categories_CategoryDetailViewModel = "com.tviptv.app.ui.categories.CategoryDetailViewModel";

      @KeepFieldType
      BrowseViewModel com_tviptv_app_ui_browse_BrowseViewModel2;

      @KeepFieldType
      PlayerViewModel com_tviptv_app_ui_player_PlayerViewModel2;

      @KeepFieldType
      SearchViewModel com_tviptv_app_ui_search_SearchViewModel2;

      @KeepFieldType
      SeriesDetailsViewModel com_tviptv_app_ui_details_SeriesDetailsViewModel2;

      @KeepFieldType
      CategoryDetailViewModel com_tviptv_app_ui_categories_CategoryDetailViewModel2;
    }
  }

  private static final class ViewModelCImpl extends TvIptvApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<BrowseViewModel> browseViewModelProvider;

    private Provider<CategoryDetailViewModel> categoryDetailViewModelProvider;

    private Provider<PlayerViewModel> playerViewModelProvider;

    private Provider<SearchViewModel> searchViewModelProvider;

    private Provider<SeriesDetailsViewModel> seriesDetailsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.browseViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.categoryDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.playerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.searchViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.seriesDetailsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(ImmutableMap.<String, javax.inject.Provider<ViewModel>>of(LazyClassKeyProvider.com_tviptv_app_ui_browse_BrowseViewModel, ((Provider) browseViewModelProvider), LazyClassKeyProvider.com_tviptv_app_ui_categories_CategoryDetailViewModel, ((Provider) categoryDetailViewModelProvider), LazyClassKeyProvider.com_tviptv_app_ui_player_PlayerViewModel, ((Provider) playerViewModelProvider), LazyClassKeyProvider.com_tviptv_app_ui_search_SearchViewModel, ((Provider) searchViewModelProvider), LazyClassKeyProvider.com_tviptv_app_ui_details_SeriesDetailsViewModel, ((Provider) seriesDetailsViewModelProvider)));
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return ImmutableMap.<Class<?>, Object>of();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_tviptv_app_ui_player_PlayerViewModel = "com.tviptv.app.ui.player.PlayerViewModel";

      static String com_tviptv_app_ui_search_SearchViewModel = "com.tviptv.app.ui.search.SearchViewModel";

      static String com_tviptv_app_ui_categories_CategoryDetailViewModel = "com.tviptv.app.ui.categories.CategoryDetailViewModel";

      static String com_tviptv_app_ui_browse_BrowseViewModel = "com.tviptv.app.ui.browse.BrowseViewModel";

      static String com_tviptv_app_ui_details_SeriesDetailsViewModel = "com.tviptv.app.ui.details.SeriesDetailsViewModel";

      @KeepFieldType
      PlayerViewModel com_tviptv_app_ui_player_PlayerViewModel2;

      @KeepFieldType
      SearchViewModel com_tviptv_app_ui_search_SearchViewModel2;

      @KeepFieldType
      CategoryDetailViewModel com_tviptv_app_ui_categories_CategoryDetailViewModel2;

      @KeepFieldType
      BrowseViewModel com_tviptv_app_ui_browse_BrowseViewModel2;

      @KeepFieldType
      SeriesDetailsViewModel com_tviptv_app_ui_details_SeriesDetailsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.tviptv.app.ui.browse.BrowseViewModel 
          return (T) new BrowseViewModel(singletonCImpl.sourceRepositoryProvider.get(), singletonCImpl.iptvRepositoryFactoryProvider.get(), singletonCImpl.sourceRefreshPolicyProvider.get(), singletonCImpl.sectionFeedCacheProvider.get(), singletonCImpl.categoryChannelsCacheProvider.get(), singletonCImpl.homeFeedCacheProvider.get(), singletonCImpl.seriesEpisodesCacheProvider.get(), singletonCImpl.channelDao(), singletonCImpl.favoriteDao(), singletonCImpl.watchHistoryRepositoryProvider.get(), singletonCImpl.appPreferencesProvider.get(), singletonCImpl.watchNextPublisherProvider.get());

          case 1: // com.tviptv.app.ui.categories.CategoryDetailViewModel 
          return (T) new CategoryDetailViewModel(singletonCImpl.channelDao(), singletonCImpl.categoryChannelsCacheProvider.get(), singletonCImpl.categoryContentLoaderProvider.get());

          case 2: // com.tviptv.app.ui.player.PlayerViewModel 
          return (T) new PlayerViewModel(singletonCImpl.channelDao(), singletonCImpl.sourceDao(), singletonCImpl.favoriteDao(), singletonCImpl.watchHistoryRepositoryProvider.get(), singletonCImpl.playerEpgRepositoryProvider.get(), singletonCImpl.seriesEpisodesLoaderProvider.get(), singletonCImpl.appPreferencesProvider.get());

          case 3: // com.tviptv.app.ui.search.SearchViewModel 
          return (T) new SearchViewModel(singletonCImpl.channelSearchHelperProvider.get(), singletonCImpl.sourceRepositoryProvider.get());

          case 4: // com.tviptv.app.ui.details.SeriesDetailsViewModel 
          return (T) new SeriesDetailsViewModel(singletonCImpl.seriesEpisodesLoaderProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends TvIptvApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends TvIptvApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends TvIptvApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<CredentialStore> credentialStoreProvider;

    private Provider<SourceRepository> sourceRepositoryProvider;

    private Provider<M3uParser> provideM3uParserProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<M3uRepository> m3uRepositoryProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<XtreamApi> provideXtreamApiProvider;

    private Provider<PlayerEpgRepository> playerEpgRepositoryProvider;

    private Provider<XtreamRepository> xtreamRepositoryProvider;

    private Provider<IptvRepositoryFactory> iptvRepositoryFactoryProvider;

    private Provider<AppPreferences> appPreferencesProvider;

    private Provider<SeriesEpisodesCache> seriesEpisodesCacheProvider;

    private Provider<SeriesEpisodesLoader> seriesEpisodesLoaderProvider;

    private Provider<CategoryChannelsCache> categoryChannelsCacheProvider;

    private Provider<CategoryContentLoader> categoryContentLoaderProvider;

    private Provider<CategoryPrefetcher> categoryPrefetcherProvider;

    private Provider<WatchHistoryRepository> watchHistoryRepositoryProvider;

    private Provider<ContinueWatchingNavigator> continueWatchingNavigatorProvider;

    private Provider<SourceRefreshPolicy> sourceRefreshPolicyProvider;

    private Provider<SectionFeedCache> sectionFeedCacheProvider;

    private Provider<HomeFeedCache> homeFeedCacheProvider;

    private Provider<WatchNextPublisher> watchNextPublisherProvider;

    private Provider<ChannelSearchHelper> channelSearchHelperProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private SourceDao sourceDao() {
      return AppModule_ProvideSourceDaoFactory.provideSourceDao(provideDatabaseProvider.get());
    }

    private CategoryDao categoryDao() {
      return AppModule_ProvideCategoryDaoFactory.provideCategoryDao(provideDatabaseProvider.get());
    }

    private ChannelDao channelDao() {
      return AppModule_ProvideChannelDaoFactory.provideChannelDao(provideDatabaseProvider.get());
    }

    private FavoriteDao favoriteDao() {
      return AppModule_ProvideFavoriteDaoFactory.provideFavoriteDao(provideDatabaseProvider.get());
    }

    private LastWatchedDao lastWatchedDao() {
      return AppModule_ProvideLastWatchedDaoFactory.provideLastWatchedDao(provideDatabaseProvider.get());
    }

    private EpgDao epgDao() {
      return AppModule_ProvideEpgDaoFactory.provideEpgDao(provideDatabaseProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 1));
      this.credentialStoreProvider = DoubleCheck.provider(new SwitchingProvider<CredentialStore>(singletonCImpl, 2));
      this.sourceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SourceRepository>(singletonCImpl, 0));
      this.provideM3uParserProvider = DoubleCheck.provider(new SwitchingProvider<M3uParser>(singletonCImpl, 5));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 6));
      this.m3uRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<M3uRepository>(singletonCImpl, 4));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 9));
      this.provideXtreamApiProvider = DoubleCheck.provider(new SwitchingProvider<XtreamApi>(singletonCImpl, 8));
      this.playerEpgRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PlayerEpgRepository>(singletonCImpl, 10));
      this.xtreamRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<XtreamRepository>(singletonCImpl, 7));
      this.iptvRepositoryFactoryProvider = DoubleCheck.provider(new SwitchingProvider<IptvRepositoryFactory>(singletonCImpl, 3));
      this.appPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<AppPreferences>(singletonCImpl, 11));
      this.seriesEpisodesCacheProvider = DoubleCheck.provider(new SwitchingProvider<SeriesEpisodesCache>(singletonCImpl, 13));
      this.seriesEpisodesLoaderProvider = DoubleCheck.provider(new SwitchingProvider<SeriesEpisodesLoader>(singletonCImpl, 12));
      this.categoryChannelsCacheProvider = DoubleCheck.provider(new SwitchingProvider<CategoryChannelsCache>(singletonCImpl, 16));
      this.categoryContentLoaderProvider = DoubleCheck.provider(new SwitchingProvider<CategoryContentLoader>(singletonCImpl, 15));
      this.categoryPrefetcherProvider = DoubleCheck.provider(new SwitchingProvider<CategoryPrefetcher>(singletonCImpl, 14));
      this.watchHistoryRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<WatchHistoryRepository>(singletonCImpl, 18));
      this.continueWatchingNavigatorProvider = DoubleCheck.provider(new SwitchingProvider<ContinueWatchingNavigator>(singletonCImpl, 17));
      this.sourceRefreshPolicyProvider = DoubleCheck.provider(new SwitchingProvider<SourceRefreshPolicy>(singletonCImpl, 19));
      this.sectionFeedCacheProvider = DoubleCheck.provider(new SwitchingProvider<SectionFeedCache>(singletonCImpl, 20));
      this.homeFeedCacheProvider = DoubleCheck.provider(new SwitchingProvider<HomeFeedCache>(singletonCImpl, 21));
      this.watchNextPublisherProvider = DoubleCheck.provider(new SwitchingProvider<WatchNextPublisher>(singletonCImpl, 22));
      this.channelSearchHelperProvider = DoubleCheck.provider(new SwitchingProvider<ChannelSearchHelper>(singletonCImpl, 23));
    }

    @Override
    public void injectTvIptvApplication(TvIptvApplication tvIptvApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return ImmutableSet.<Boolean>of();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.tviptv.app.data.repository.SourceRepository 
          return (T) new SourceRepository(singletonCImpl.sourceDao(), singletonCImpl.categoryDao(), singletonCImpl.channelDao(), singletonCImpl.favoriteDao(), singletonCImpl.lastWatchedDao(), singletonCImpl.epgDao(), singletonCImpl.credentialStoreProvider.get());

          case 1: // com.tviptv.app.data.local.AppDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.tviptv.app.data.prefs.CredentialStore 
          return (T) new CredentialStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.tviptv.app.domain.repository.IptvRepositoryFactory 
          return (T) new IptvRepositoryFactory(singletonCImpl.sourceDao(), singletonCImpl.m3uRepositoryProvider.get(), singletonCImpl.xtreamRepositoryProvider.get());

          case 4: // com.tviptv.app.data.m3u.M3uRepository 
          return (T) new M3uRepository(singletonCImpl.sourceDao(), singletonCImpl.categoryDao(), singletonCImpl.channelDao(), singletonCImpl.provideM3uParserProvider.get(), singletonCImpl.provideOkHttpClientProvider.get());

          case 5: // com.tviptv.app.data.m3u.M3uParser 
          return (T) AppModule_ProvideM3uParserFactory.provideM3uParser();

          case 6: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 7: // com.tviptv.app.data.xtream.XtreamRepository 
          return (T) new XtreamRepository(singletonCImpl.sourceDao(), singletonCImpl.categoryDao(), singletonCImpl.channelDao(), singletonCImpl.credentialStoreProvider.get(), singletonCImpl.provideXtreamApiProvider.get(), singletonCImpl.playerEpgRepositoryProvider.get());

          case 8: // com.tviptv.app.data.xtream.XtreamApi 
          return (T) AppModule_ProvideXtreamApiFactory.provideXtreamApi(singletonCImpl.provideMoshiProvider.get(), singletonCImpl.provideOkHttpClientProvider.get());

          case 9: // com.squareup.moshi.Moshi 
          return (T) AppModule_ProvideMoshiFactory.provideMoshi();

          case 10: // com.tviptv.app.data.player.PlayerEpgRepository 
          return (T) new PlayerEpgRepository(singletonCImpl.epgDao(), singletonCImpl.sourceDao(), singletonCImpl.credentialStoreProvider.get(), singletonCImpl.provideXtreamApiProvider.get());

          case 11: // com.tviptv.app.data.prefs.AppPreferences 
          return (T) new AppPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 12: // com.tviptv.app.data.cache.SeriesEpisodesLoader 
          return (T) new SeriesEpisodesLoader(singletonCImpl.iptvRepositoryFactoryProvider.get(), singletonCImpl.seriesEpisodesCacheProvider.get());

          case 13: // com.tviptv.app.data.cache.SeriesEpisodesCache 
          return (T) new SeriesEpisodesCache();

          case 14: // com.tviptv.app.data.cache.CategoryPrefetcher 
          return (T) new CategoryPrefetcher(singletonCImpl.categoryContentLoaderProvider.get(), singletonCImpl.categoryChannelsCacheProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 15: // com.tviptv.app.data.cache.CategoryContentLoader 
          return (T) new CategoryContentLoader(singletonCImpl.channelDao(), singletonCImpl.favoriteDao(), singletonCImpl.categoryChannelsCacheProvider.get());

          case 16: // com.tviptv.app.data.cache.CategoryChannelsCache 
          return (T) new CategoryChannelsCache();

          case 17: // com.tviptv.app.ui.browse.ContinueWatchingNavigator 
          return (T) new ContinueWatchingNavigator(singletonCImpl.channelDao(), singletonCImpl.sourceDao(), singletonCImpl.watchHistoryRepositoryProvider.get(), singletonCImpl.seriesEpisodesLoaderProvider.get());

          case 18: // com.tviptv.app.data.repository.WatchHistoryRepository 
          return (T) new WatchHistoryRepository(singletonCImpl.lastWatchedDao(), singletonCImpl.channelDao());

          case 19: // com.tviptv.app.data.repository.SourceRefreshPolicy 
          return (T) new SourceRefreshPolicy(singletonCImpl.appPreferencesProvider.get());

          case 20: // com.tviptv.app.data.cache.SectionFeedCache 
          return (T) new SectionFeedCache();

          case 21: // com.tviptv.app.data.cache.HomeFeedCache 
          return (T) new HomeFeedCache();

          case 22: // com.tviptv.app.data.platform.WatchNextPublisher 
          return (T) new WatchNextPublisher(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.sourceRepositoryProvider.get(), singletonCImpl.watchHistoryRepositoryProvider.get());

          case 23: // com.tviptv.app.data.local.ChannelSearchHelper 
          return (T) new ChannelSearchHelper(singletonCImpl.channelDao());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
