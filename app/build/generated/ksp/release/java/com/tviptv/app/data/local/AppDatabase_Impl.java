package com.tviptv.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.tviptv.app.data.local.dao.CategoryDao;
import com.tviptv.app.data.local.dao.CategoryDao_Impl;
import com.tviptv.app.data.local.dao.ChannelDao;
import com.tviptv.app.data.local.dao.ChannelDao_Impl;
import com.tviptv.app.data.local.dao.EpgDao;
import com.tviptv.app.data.local.dao.EpgDao_Impl;
import com.tviptv.app.data.local.dao.FavoriteDao;
import com.tviptv.app.data.local.dao.FavoriteDao_Impl;
import com.tviptv.app.data.local.dao.LastWatchedDao;
import com.tviptv.app.data.local.dao.LastWatchedDao_Impl;
import com.tviptv.app.data.local.dao.SourceDao;
import com.tviptv.app.data.local.dao.SourceDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SourceDao _sourceDao;

  private volatile CategoryDao _categoryDao;

  private volatile ChannelDao _channelDao;

  private volatile FavoriteDao _favoriteDao;

  private volatile LastWatchedDao _lastWatchedDao;

  private volatile EpgDao _epgDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(8) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sources` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `name` TEXT NOT NULL, `url` TEXT, `username` TEXT, `updatedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`sourceId` INTEGER NOT NULL, `externalId` TEXT NOT NULL, `name` TEXT NOT NULL, `contentType` TEXT NOT NULL, PRIMARY KEY(`sourceId`, `externalId`, `contentType`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `channels` (`sourceId` INTEGER NOT NULL, `externalId` TEXT NOT NULL, `name` TEXT NOT NULL, `logoUrl` TEXT, `categoryId` TEXT, `streamUrl` TEXT, `sortOrder` INTEGER NOT NULL, `contentType` TEXT NOT NULL, `containerExtension` TEXT, `plot` TEXT, `addedAt` INTEGER, `channelNumber` INTEGER, PRIMARY KEY(`sourceId`, `externalId`, `contentType`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_channels_sourceId_contentType_categoryId` ON `channels` (`sourceId`, `contentType`, `categoryId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_channels_sourceId_contentType_categoryId_addedAt_sortOrder_name` ON `channels` (`sourceId`, `contentType`, `categoryId`, `addedAt`, `sortOrder`, `name`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `favorites` (`sourceId` INTEGER NOT NULL, `channelId` TEXT NOT NULL, `contentType` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`sourceId`, `channelId`, `contentType`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `last_watched` (`sourceId` INTEGER NOT NULL, `channelId` TEXT NOT NULL, `contentType` TEXT NOT NULL, `watchedAt` INTEGER NOT NULL, `playbackPositionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `title` TEXT, `imageUrl` TEXT, `categoryId` TEXT, `seriesId` TEXT, `seriesName` TEXT, PRIMARY KEY(`sourceId`, `channelId`, `contentType`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `epg_programs` (`sourceId` INTEGER NOT NULL, `streamId` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT, `startMs` INTEGER NOT NULL, `endMs` INTEGER NOT NULL, `fetchedAt` INTEGER NOT NULL, PRIMARY KEY(`sourceId`, `streamId`, `startMs`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'cf7dc1ecf631f23554f62ffcbd32ef94')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `sources`");
        db.execSQL("DROP TABLE IF EXISTS `categories`");
        db.execSQL("DROP TABLE IF EXISTS `channels`");
        db.execSQL("DROP TABLE IF EXISTS `favorites`");
        db.execSQL("DROP TABLE IF EXISTS `last_watched`");
        db.execSQL("DROP TABLE IF EXISTS `epg_programs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSources = new HashMap<String, TableInfo.Column>(6);
        _columnsSources.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSources.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSources.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSources.put("url", new TableInfo.Column("url", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSources.put("username", new TableInfo.Column("username", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSources.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSources = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSources = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSources = new TableInfo("sources", _columnsSources, _foreignKeysSources, _indicesSources);
        final TableInfo _existingSources = TableInfo.read(db, "sources");
        if (!_infoSources.equals(_existingSources)) {
          return new RoomOpenHelper.ValidationResult(false, "sources(com.tviptv.app.data.local.entity.SourceEntity).\n"
                  + " Expected:\n" + _infoSources + "\n"
                  + " Found:\n" + _existingSources);
        }
        final HashMap<String, TableInfo.Column> _columnsCategories = new HashMap<String, TableInfo.Column>(4);
        _columnsCategories.put("sourceId", new TableInfo.Column("sourceId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("externalId", new TableInfo.Column("externalId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCategories.put("contentType", new TableInfo.Column("contentType", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCategories = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCategories = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCategories = new TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories);
        final TableInfo _existingCategories = TableInfo.read(db, "categories");
        if (!_infoCategories.equals(_existingCategories)) {
          return new RoomOpenHelper.ValidationResult(false, "categories(com.tviptv.app.data.local.entity.CategoryEntity).\n"
                  + " Expected:\n" + _infoCategories + "\n"
                  + " Found:\n" + _existingCategories);
        }
        final HashMap<String, TableInfo.Column> _columnsChannels = new HashMap<String, TableInfo.Column>(12);
        _columnsChannels.put("sourceId", new TableInfo.Column("sourceId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("externalId", new TableInfo.Column("externalId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("logoUrl", new TableInfo.Column("logoUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("categoryId", new TableInfo.Column("categoryId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("streamUrl", new TableInfo.Column("streamUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("sortOrder", new TableInfo.Column("sortOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("contentType", new TableInfo.Column("contentType", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("containerExtension", new TableInfo.Column("containerExtension", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("plot", new TableInfo.Column("plot", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsChannels.put("channelNumber", new TableInfo.Column("channelNumber", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysChannels = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesChannels = new HashSet<TableInfo.Index>(2);
        _indicesChannels.add(new TableInfo.Index("index_channels_sourceId_contentType_categoryId", false, Arrays.asList("sourceId", "contentType", "categoryId"), Arrays.asList("ASC", "ASC", "ASC")));
        _indicesChannels.add(new TableInfo.Index("index_channels_sourceId_contentType_categoryId_addedAt_sortOrder_name", false, Arrays.asList("sourceId", "contentType", "categoryId", "addedAt", "sortOrder", "name"), Arrays.asList("ASC", "ASC", "ASC", "ASC", "ASC", "ASC")));
        final TableInfo _infoChannels = new TableInfo("channels", _columnsChannels, _foreignKeysChannels, _indicesChannels);
        final TableInfo _existingChannels = TableInfo.read(db, "channels");
        if (!_infoChannels.equals(_existingChannels)) {
          return new RoomOpenHelper.ValidationResult(false, "channels(com.tviptv.app.data.local.entity.ChannelEntity).\n"
                  + " Expected:\n" + _infoChannels + "\n"
                  + " Found:\n" + _existingChannels);
        }
        final HashMap<String, TableInfo.Column> _columnsFavorites = new HashMap<String, TableInfo.Column>(4);
        _columnsFavorites.put("sourceId", new TableInfo.Column("sourceId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavorites.put("channelId", new TableInfo.Column("channelId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavorites.put("contentType", new TableInfo.Column("contentType", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFavorites.put("addedAt", new TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFavorites = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFavorites = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFavorites = new TableInfo("favorites", _columnsFavorites, _foreignKeysFavorites, _indicesFavorites);
        final TableInfo _existingFavorites = TableInfo.read(db, "favorites");
        if (!_infoFavorites.equals(_existingFavorites)) {
          return new RoomOpenHelper.ValidationResult(false, "favorites(com.tviptv.app.data.local.entity.FavoriteEntity).\n"
                  + " Expected:\n" + _infoFavorites + "\n"
                  + " Found:\n" + _existingFavorites);
        }
        final HashMap<String, TableInfo.Column> _columnsLastWatched = new HashMap<String, TableInfo.Column>(11);
        _columnsLastWatched.put("sourceId", new TableInfo.Column("sourceId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("channelId", new TableInfo.Column("channelId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("contentType", new TableInfo.Column("contentType", "TEXT", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("watchedAt", new TableInfo.Column("watchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("playbackPositionMs", new TableInfo.Column("playbackPositionMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("durationMs", new TableInfo.Column("durationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("title", new TableInfo.Column("title", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("imageUrl", new TableInfo.Column("imageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("categoryId", new TableInfo.Column("categoryId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("seriesId", new TableInfo.Column("seriesId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLastWatched.put("seriesName", new TableInfo.Column("seriesName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLastWatched = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLastWatched = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLastWatched = new TableInfo("last_watched", _columnsLastWatched, _foreignKeysLastWatched, _indicesLastWatched);
        final TableInfo _existingLastWatched = TableInfo.read(db, "last_watched");
        if (!_infoLastWatched.equals(_existingLastWatched)) {
          return new RoomOpenHelper.ValidationResult(false, "last_watched(com.tviptv.app.data.local.entity.LastWatchedEntity).\n"
                  + " Expected:\n" + _infoLastWatched + "\n"
                  + " Found:\n" + _existingLastWatched);
        }
        final HashMap<String, TableInfo.Column> _columnsEpgPrograms = new HashMap<String, TableInfo.Column>(7);
        _columnsEpgPrograms.put("sourceId", new TableInfo.Column("sourceId", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("streamId", new TableInfo.Column("streamId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("startMs", new TableInfo.Column("startMs", "INTEGER", true, 3, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("endMs", new TableInfo.Column("endMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEpgPrograms.put("fetchedAt", new TableInfo.Column("fetchedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEpgPrograms = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEpgPrograms = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEpgPrograms = new TableInfo("epg_programs", _columnsEpgPrograms, _foreignKeysEpgPrograms, _indicesEpgPrograms);
        final TableInfo _existingEpgPrograms = TableInfo.read(db, "epg_programs");
        if (!_infoEpgPrograms.equals(_existingEpgPrograms)) {
          return new RoomOpenHelper.ValidationResult(false, "epg_programs(com.tviptv.app.data.local.entity.EpgProgramEntity).\n"
                  + " Expected:\n" + _infoEpgPrograms + "\n"
                  + " Found:\n" + _existingEpgPrograms);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "cf7dc1ecf631f23554f62ffcbd32ef94", "263f8da672a385036001199ec2aeb456");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "sources","categories","channels","favorites","last_watched","epg_programs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `sources`");
      _db.execSQL("DELETE FROM `categories`");
      _db.execSQL("DELETE FROM `channels`");
      _db.execSQL("DELETE FROM `favorites`");
      _db.execSQL("DELETE FROM `last_watched`");
      _db.execSQL("DELETE FROM `epg_programs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SourceDao.class, SourceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CategoryDao.class, CategoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ChannelDao.class, ChannelDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FavoriteDao.class, FavoriteDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(LastWatchedDao.class, LastWatchedDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(EpgDao.class, EpgDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SourceDao sourceDao() {
    if (_sourceDao != null) {
      return _sourceDao;
    } else {
      synchronized(this) {
        if(_sourceDao == null) {
          _sourceDao = new SourceDao_Impl(this);
        }
        return _sourceDao;
      }
    }
  }

  @Override
  public CategoryDao categoryDao() {
    if (_categoryDao != null) {
      return _categoryDao;
    } else {
      synchronized(this) {
        if(_categoryDao == null) {
          _categoryDao = new CategoryDao_Impl(this);
        }
        return _categoryDao;
      }
    }
  }

  @Override
  public ChannelDao channelDao() {
    if (_channelDao != null) {
      return _channelDao;
    } else {
      synchronized(this) {
        if(_channelDao == null) {
          _channelDao = new ChannelDao_Impl(this);
        }
        return _channelDao;
      }
    }
  }

  @Override
  public FavoriteDao favoriteDao() {
    if (_favoriteDao != null) {
      return _favoriteDao;
    } else {
      synchronized(this) {
        if(_favoriteDao == null) {
          _favoriteDao = new FavoriteDao_Impl(this);
        }
        return _favoriteDao;
      }
    }
  }

  @Override
  public LastWatchedDao lastWatchedDao() {
    if (_lastWatchedDao != null) {
      return _lastWatchedDao;
    } else {
      synchronized(this) {
        if(_lastWatchedDao == null) {
          _lastWatchedDao = new LastWatchedDao_Impl(this);
        }
        return _lastWatchedDao;
      }
    }
  }

  @Override
  public EpgDao epgDao() {
    if (_epgDao != null) {
      return _epgDao;
    } else {
      synchronized(this) {
        if(_epgDao == null) {
          _epgDao = new EpgDao_Impl(this);
        }
        return _epgDao;
      }
    }
  }
}
