package com.afifistudio.iptvcinema.di

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.afifistudio.iptvcinema.data.local.AppDatabase
import com.afifistudio.iptvcinema.data.local.MIGRATION_1_2
import com.afifistudio.iptvcinema.data.local.MIGRATION_2_3
import com.afifistudio.iptvcinema.data.local.MIGRATION_3_4
import com.afifistudio.iptvcinema.data.local.MIGRATION_4_5
import com.afifistudio.iptvcinema.data.local.MIGRATION_5_6
import com.afifistudio.iptvcinema.data.local.MIGRATION_6_7
import com.afifistudio.iptvcinema.data.local.MIGRATION_7_8
import com.afifistudio.iptvcinema.data.local.MIGRATION_8_9
import com.afifistudio.iptvcinema.data.m3u.M3uParser
import com.afifistudio.iptvcinema.data.xtream.XtreamApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideXtreamApi(moshi: Moshi, okHttpClient: OkHttpClient): XtreamApi = Retrofit.Builder()
        .baseUrl("http://localhost/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(XtreamApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "tv_iptv.db",
    ).addMigrations(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_8_9,
    ).build()

    @Provides
    fun provideSourceDao(database: AppDatabase) = database.sourceDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase) = database.categoryDao()

    @Provides
    fun provideChannelDao(database: AppDatabase) = database.channelDao()

    @Provides
    fun provideFavoriteDao(database: AppDatabase) = database.favoriteDao()

    @Provides
    fun provideLastWatchedDao(database: AppDatabase) = database.lastWatchedDao()

    @Provides
    fun provideEpgDao(database: AppDatabase) = database.epgDao()

    @Provides
    fun provideSectionImportStateDao(database: AppDatabase) = database.sectionImportStateDao()

    @Provides
    @Singleton
    fun provideM3uParser(): M3uParser = M3uParser()
}
