package com.tviptv.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tviptv.app.data.local.ContentTypeConverters;
import com.tviptv.app.data.local.entity.ChannelEntity;
import com.tviptv.app.data.local.entity.LastWatchedEntity;
import com.tviptv.app.data.local.entity.LastWatchedRow;
import com.tviptv.app.domain.model.ContentType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LastWatchedDao_Impl implements LastWatchedDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LastWatchedEntity> __insertionAdapterOfLastWatchedEntity;

  private final ContentTypeConverters __contentTypeConverters = new ContentTypeConverters();

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySource;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public LastWatchedDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLastWatchedEntity = new EntityInsertionAdapter<LastWatchedEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `last_watched` (`sourceId`,`channelId`,`contentType`,`watchedAt`,`playbackPositionMs`,`durationMs`,`title`,`imageUrl`,`categoryId`,`seriesId`,`seriesName`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final LastWatchedEntity entity) {
        statement.bindLong(1, entity.getSourceId());
        statement.bindString(2, entity.getChannelId());
        final String _tmp = __contentTypeConverters.fromContentType(entity.getContentType());
        statement.bindString(3, _tmp);
        statement.bindLong(4, entity.getWatchedAt());
        statement.bindLong(5, entity.getPlaybackPositionMs());
        statement.bindLong(6, entity.getDurationMs());
        if (entity.getTitle() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTitle());
        }
        if (entity.getImageUrl() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getImageUrl());
        }
        if (entity.getCategoryId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCategoryId());
        }
        if (entity.getSeriesId() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getSeriesId());
        }
        if (entity.getSeriesName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getSeriesName());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM last_watched WHERE sourceId = ? AND channelId = ? AND contentType = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBySource = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM last_watched WHERE sourceId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM last_watched";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final LastWatchedEntity lastWatched,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfLastWatchedEntity.insert(lastWatched);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final long sourceId, final String channelId, final ContentType contentType,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sourceId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, channelId);
        _argIndex = 3;
        final String _tmp = __contentTypeConverters.fromContentType(contentType);
        _stmt.bindString(_argIndex, _tmp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDelete.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBySource(final long sourceId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBySource.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sourceId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteBySource.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentWatchRows(final long sourceId, final ContentType contentType,
      final int limit, final Continuation<? super List<LastWatchedRow>> $completion) {
    final String _sql = "\n"
            + "        SELECT\n"
            + "            lw.sourceId AS sourceId,\n"
            + "            lw.channelId AS channelId,\n"
            + "            lw.contentType AS contentType,\n"
            + "            lw.watchedAt AS watchedAt,\n"
            + "            lw.playbackPositionMs AS playbackPositionMs,\n"
            + "            lw.durationMs AS durationMs,\n"
            + "            lw.title AS title,\n"
            + "            lw.imageUrl AS imageUrl,\n"
            + "            lw.categoryId AS categoryId,\n"
            + "            lw.seriesId AS seriesId,\n"
            + "            lw.seriesName AS seriesName,\n"
            + "            c.name AS channelName,\n"
            + "            c.logoUrl AS channelLogoUrl,\n"
            + "            c.categoryId AS channelCategoryId,\n"
            + "            c.streamUrl AS channelStreamUrl,\n"
            + "            c.sortOrder AS channelSortOrder,\n"
            + "            c.containerExtension AS channelContainerExtension,\n"
            + "            c.plot AS channelPlot,\n"
            + "            c.addedAt AS channelAddedAt\n"
            + "        FROM last_watched lw\n"
            + "        LEFT JOIN channels c ON c.sourceId = lw.sourceId\n"
            + "            AND c.externalId = lw.channelId\n"
            + "            AND c.contentType = lw.contentType\n"
            + "        WHERE lw.sourceId = ?\n"
            + "        AND (? IS NULL OR lw.contentType = ?)\n"
            + "        ORDER BY lw.watchedAt DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp;
    if (contentType == null) {
      _tmp = null;
    } else {
      _tmp = __contentTypeConverters.fromContentType(contentType);
    }
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 3;
    final String _tmp_1;
    if (contentType == null) {
      _tmp_1 = null;
    } else {
      _tmp_1 = __contentTypeConverters.fromContentType(contentType);
    }
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    _argIndex = 4;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<LastWatchedRow>>() {
      @Override
      @NonNull
      public List<LastWatchedRow> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = 0;
          final int _cursorIndexOfChannelId = 1;
          final int _cursorIndexOfContentType = 2;
          final int _cursorIndexOfWatchedAt = 3;
          final int _cursorIndexOfPlaybackPositionMs = 4;
          final int _cursorIndexOfDurationMs = 5;
          final int _cursorIndexOfTitle = 6;
          final int _cursorIndexOfImageUrl = 7;
          final int _cursorIndexOfCategoryId = 8;
          final int _cursorIndexOfSeriesId = 9;
          final int _cursorIndexOfSeriesName = 10;
          final int _cursorIndexOfChannelName = 11;
          final int _cursorIndexOfChannelLogoUrl = 12;
          final int _cursorIndexOfChannelCategoryId = 13;
          final int _cursorIndexOfChannelStreamUrl = 14;
          final int _cursorIndexOfChannelSortOrder = 15;
          final int _cursorIndexOfChannelContainerExtension = 16;
          final int _cursorIndexOfChannelPlot = 17;
          final int _cursorIndexOfChannelAddedAt = 18;
          final List<LastWatchedRow> _result = new ArrayList<LastWatchedRow>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final LastWatchedRow _item;
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpChannelId;
            _tmpChannelId = _cursor.getString(_cursorIndexOfChannelId);
            final ContentType _tmpContentType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_2);
            final long _tmpWatchedAt;
            _tmpWatchedAt = _cursor.getLong(_cursorIndexOfWatchedAt);
            final long _tmpPlaybackPositionMs;
            _tmpPlaybackPositionMs = _cursor.getLong(_cursorIndexOfPlaybackPositionMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpSeriesId;
            if (_cursor.isNull(_cursorIndexOfSeriesId)) {
              _tmpSeriesId = null;
            } else {
              _tmpSeriesId = _cursor.getString(_cursorIndexOfSeriesId);
            }
            final String _tmpSeriesName;
            if (_cursor.isNull(_cursorIndexOfSeriesName)) {
              _tmpSeriesName = null;
            } else {
              _tmpSeriesName = _cursor.getString(_cursorIndexOfSeriesName);
            }
            final String _tmpChannelName;
            if (_cursor.isNull(_cursorIndexOfChannelName)) {
              _tmpChannelName = null;
            } else {
              _tmpChannelName = _cursor.getString(_cursorIndexOfChannelName);
            }
            final String _tmpChannelLogoUrl;
            if (_cursor.isNull(_cursorIndexOfChannelLogoUrl)) {
              _tmpChannelLogoUrl = null;
            } else {
              _tmpChannelLogoUrl = _cursor.getString(_cursorIndexOfChannelLogoUrl);
            }
            final String _tmpChannelCategoryId;
            if (_cursor.isNull(_cursorIndexOfChannelCategoryId)) {
              _tmpChannelCategoryId = null;
            } else {
              _tmpChannelCategoryId = _cursor.getString(_cursorIndexOfChannelCategoryId);
            }
            final String _tmpChannelStreamUrl;
            if (_cursor.isNull(_cursorIndexOfChannelStreamUrl)) {
              _tmpChannelStreamUrl = null;
            } else {
              _tmpChannelStreamUrl = _cursor.getString(_cursorIndexOfChannelStreamUrl);
            }
            final Integer _tmpChannelSortOrder;
            if (_cursor.isNull(_cursorIndexOfChannelSortOrder)) {
              _tmpChannelSortOrder = null;
            } else {
              _tmpChannelSortOrder = _cursor.getInt(_cursorIndexOfChannelSortOrder);
            }
            final String _tmpChannelContainerExtension;
            if (_cursor.isNull(_cursorIndexOfChannelContainerExtension)) {
              _tmpChannelContainerExtension = null;
            } else {
              _tmpChannelContainerExtension = _cursor.getString(_cursorIndexOfChannelContainerExtension);
            }
            final String _tmpChannelPlot;
            if (_cursor.isNull(_cursorIndexOfChannelPlot)) {
              _tmpChannelPlot = null;
            } else {
              _tmpChannelPlot = _cursor.getString(_cursorIndexOfChannelPlot);
            }
            final Long _tmpChannelAddedAt;
            if (_cursor.isNull(_cursorIndexOfChannelAddedAt)) {
              _tmpChannelAddedAt = null;
            } else {
              _tmpChannelAddedAt = _cursor.getLong(_cursorIndexOfChannelAddedAt);
            }
            _item = new LastWatchedRow(_tmpSourceId,_tmpChannelId,_tmpContentType,_tmpWatchedAt,_tmpPlaybackPositionMs,_tmpDurationMs,_tmpTitle,_tmpImageUrl,_tmpCategoryId,_tmpSeriesId,_tmpSeriesName,_tmpChannelName,_tmpChannelLogoUrl,_tmpChannelCategoryId,_tmpChannelStreamUrl,_tmpChannelSortOrder,_tmpChannelContainerExtension,_tmpChannelPlot,_tmpChannelAddedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getRecentChannels(final long sourceId, final ContentType contentType,
      final int limit, final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT c.* FROM channels c\n"
            + "        INNER JOIN last_watched lw ON c.sourceId = lw.sourceId\n"
            + "            AND c.externalId = lw.channelId\n"
            + "            AND c.contentType = lw.contentType\n"
            + "        WHERE lw.sourceId = ?\n"
            + "        AND (? IS NULL OR c.contentType = ?)\n"
            + "        ORDER BY lw.watchedAt DESC LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp;
    if (contentType == null) {
      _tmp = null;
    } else {
      _tmp = __contentTypeConverters.fromContentType(contentType);
    }
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 3;
    final String _tmp_1;
    if (contentType == null) {
      _tmp_1 = null;
    } else {
      _tmp_1 = __contentTypeConverters.fromContentType(contentType);
    }
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    _argIndex = 4;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfExternalId = CursorUtil.getColumnIndexOrThrow(_cursor, "externalId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "logoUrl");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfStreamUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "streamUrl");
          final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
          final int _cursorIndexOfContentType = CursorUtil.getColumnIndexOrThrow(_cursor, "contentType");
          final int _cursorIndexOfContainerExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "containerExtension");
          final int _cursorIndexOfPlot = CursorUtil.getColumnIndexOrThrow(_cursor, "plot");
          final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "addedAt");
          final int _cursorIndexOfChannelNumber = CursorUtil.getColumnIndexOrThrow(_cursor, "channelNumber");
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpExternalId;
            _tmpExternalId = _cursor.getString(_cursorIndexOfExternalId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpLogoUrl;
            if (_cursor.isNull(_cursorIndexOfLogoUrl)) {
              _tmpLogoUrl = null;
            } else {
              _tmpLogoUrl = _cursor.getString(_cursorIndexOfLogoUrl);
            }
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpStreamUrl;
            if (_cursor.isNull(_cursorIndexOfStreamUrl)) {
              _tmpStreamUrl = null;
            } else {
              _tmpStreamUrl = _cursor.getString(_cursorIndexOfStreamUrl);
            }
            final int _tmpSortOrder;
            _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
            final ContentType _tmpContentType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_2);
            final String _tmpContainerExtension;
            if (_cursor.isNull(_cursorIndexOfContainerExtension)) {
              _tmpContainerExtension = null;
            } else {
              _tmpContainerExtension = _cursor.getString(_cursorIndexOfContainerExtension);
            }
            final String _tmpPlot;
            if (_cursor.isNull(_cursorIndexOfPlot)) {
              _tmpPlot = null;
            } else {
              _tmpPlot = _cursor.getString(_cursorIndexOfPlot);
            }
            final Long _tmpAddedAt;
            if (_cursor.isNull(_cursorIndexOfAddedAt)) {
              _tmpAddedAt = null;
            } else {
              _tmpAddedAt = _cursor.getLong(_cursorIndexOfAddedAt);
            }
            final Integer _tmpChannelNumber;
            if (_cursor.isNull(_cursorIndexOfChannelNumber)) {
              _tmpChannelNumber = null;
            } else {
              _tmpChannelNumber = _cursor.getInt(_cursorIndexOfChannelNumber);
            }
            _item = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMostRecentChannelId(final long sourceId,
      final Continuation<? super String> $completion) {
    final String _sql = "SELECT channelId FROM last_watched WHERE sourceId = ? ORDER BY watchedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<String>() {
      @Override
      @Nullable
      public String call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final String _result;
          if (_cursor.moveToFirst()) {
            if (_cursor.isNull(0)) {
              _result = null;
            } else {
              _result = _cursor.getString(0);
            }
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object get(final long sourceId, final String channelId, final ContentType contentType,
      final Continuation<? super LastWatchedEntity> $completion) {
    final String _sql = "SELECT * FROM last_watched WHERE sourceId = ? AND channelId = ? AND contentType = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, channelId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LastWatchedEntity>() {
      @Override
      @Nullable
      public LastWatchedEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfChannelId = CursorUtil.getColumnIndexOrThrow(_cursor, "channelId");
          final int _cursorIndexOfContentType = CursorUtil.getColumnIndexOrThrow(_cursor, "contentType");
          final int _cursorIndexOfWatchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "watchedAt");
          final int _cursorIndexOfPlaybackPositionMs = CursorUtil.getColumnIndexOrThrow(_cursor, "playbackPositionMs");
          final int _cursorIndexOfDurationMs = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMs");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "imageUrl");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfSeriesId = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesId");
          final int _cursorIndexOfSeriesName = CursorUtil.getColumnIndexOrThrow(_cursor, "seriesName");
          final LastWatchedEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpChannelId;
            _tmpChannelId = _cursor.getString(_cursorIndexOfChannelId);
            final ContentType _tmpContentType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
            final long _tmpWatchedAt;
            _tmpWatchedAt = _cursor.getLong(_cursorIndexOfWatchedAt);
            final long _tmpPlaybackPositionMs;
            _tmpPlaybackPositionMs = _cursor.getLong(_cursorIndexOfPlaybackPositionMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpSeriesId;
            if (_cursor.isNull(_cursorIndexOfSeriesId)) {
              _tmpSeriesId = null;
            } else {
              _tmpSeriesId = _cursor.getString(_cursorIndexOfSeriesId);
            }
            final String _tmpSeriesName;
            if (_cursor.isNull(_cursorIndexOfSeriesName)) {
              _tmpSeriesName = null;
            } else {
              _tmpSeriesName = _cursor.getString(_cursorIndexOfSeriesName);
            }
            _result = new LastWatchedEntity(_tmpSourceId,_tmpChannelId,_tmpContentType,_tmpWatchedAt,_tmpPlaybackPositionMs,_tmpDurationMs,_tmpTitle,_tmpImageUrl,_tmpCategoryId,_tmpSeriesId,_tmpSeriesName);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLatestEpisodeRowForSeries(final long sourceId, final String seriesId,
      final Continuation<? super LastWatchedRow> $completion) {
    final String _sql = "\n"
            + "        SELECT\n"
            + "            lw.sourceId AS sourceId,\n"
            + "            lw.channelId AS channelId,\n"
            + "            lw.contentType AS contentType,\n"
            + "            lw.watchedAt AS watchedAt,\n"
            + "            lw.playbackPositionMs AS playbackPositionMs,\n"
            + "            lw.durationMs AS durationMs,\n"
            + "            lw.title AS title,\n"
            + "            lw.imageUrl AS imageUrl,\n"
            + "            lw.categoryId AS categoryId,\n"
            + "            lw.seriesId AS seriesId,\n"
            + "            lw.seriesName AS seriesName,\n"
            + "            c.name AS channelName,\n"
            + "            c.logoUrl AS channelLogoUrl,\n"
            + "            c.categoryId AS channelCategoryId,\n"
            + "            c.streamUrl AS channelStreamUrl,\n"
            + "            c.sortOrder AS channelSortOrder,\n"
            + "            c.containerExtension AS channelContainerExtension,\n"
            + "            c.plot AS channelPlot,\n"
            + "            c.addedAt AS channelAddedAt\n"
            + "        FROM last_watched lw\n"
            + "        LEFT JOIN channels c ON c.sourceId = lw.sourceId\n"
            + "            AND c.externalId = lw.channelId\n"
            + "            AND c.contentType = lw.contentType\n"
            + "        WHERE lw.sourceId = ?\n"
            + "            AND lw.contentType = 'EPISODE'\n"
            + "            AND lw.seriesId = ?\n"
            + "        ORDER BY lw.watchedAt DESC\n"
            + "        LIMIT 1\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, seriesId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<LastWatchedRow>() {
      @Override
      @Nullable
      public LastWatchedRow call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = 0;
          final int _cursorIndexOfChannelId = 1;
          final int _cursorIndexOfContentType = 2;
          final int _cursorIndexOfWatchedAt = 3;
          final int _cursorIndexOfPlaybackPositionMs = 4;
          final int _cursorIndexOfDurationMs = 5;
          final int _cursorIndexOfTitle = 6;
          final int _cursorIndexOfImageUrl = 7;
          final int _cursorIndexOfCategoryId = 8;
          final int _cursorIndexOfSeriesId = 9;
          final int _cursorIndexOfSeriesName = 10;
          final int _cursorIndexOfChannelName = 11;
          final int _cursorIndexOfChannelLogoUrl = 12;
          final int _cursorIndexOfChannelCategoryId = 13;
          final int _cursorIndexOfChannelStreamUrl = 14;
          final int _cursorIndexOfChannelSortOrder = 15;
          final int _cursorIndexOfChannelContainerExtension = 16;
          final int _cursorIndexOfChannelPlot = 17;
          final int _cursorIndexOfChannelAddedAt = 18;
          final LastWatchedRow _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpChannelId;
            _tmpChannelId = _cursor.getString(_cursorIndexOfChannelId);
            final ContentType _tmpContentType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp);
            final long _tmpWatchedAt;
            _tmpWatchedAt = _cursor.getLong(_cursorIndexOfWatchedAt);
            final long _tmpPlaybackPositionMs;
            _tmpPlaybackPositionMs = _cursor.getLong(_cursorIndexOfPlaybackPositionMs);
            final long _tmpDurationMs;
            _tmpDurationMs = _cursor.getLong(_cursorIndexOfDurationMs);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpImageUrl;
            if (_cursor.isNull(_cursorIndexOfImageUrl)) {
              _tmpImageUrl = null;
            } else {
              _tmpImageUrl = _cursor.getString(_cursorIndexOfImageUrl);
            }
            final String _tmpCategoryId;
            if (_cursor.isNull(_cursorIndexOfCategoryId)) {
              _tmpCategoryId = null;
            } else {
              _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            }
            final String _tmpSeriesId;
            if (_cursor.isNull(_cursorIndexOfSeriesId)) {
              _tmpSeriesId = null;
            } else {
              _tmpSeriesId = _cursor.getString(_cursorIndexOfSeriesId);
            }
            final String _tmpSeriesName;
            if (_cursor.isNull(_cursorIndexOfSeriesName)) {
              _tmpSeriesName = null;
            } else {
              _tmpSeriesName = _cursor.getString(_cursorIndexOfSeriesName);
            }
            final String _tmpChannelName;
            if (_cursor.isNull(_cursorIndexOfChannelName)) {
              _tmpChannelName = null;
            } else {
              _tmpChannelName = _cursor.getString(_cursorIndexOfChannelName);
            }
            final String _tmpChannelLogoUrl;
            if (_cursor.isNull(_cursorIndexOfChannelLogoUrl)) {
              _tmpChannelLogoUrl = null;
            } else {
              _tmpChannelLogoUrl = _cursor.getString(_cursorIndexOfChannelLogoUrl);
            }
            final String _tmpChannelCategoryId;
            if (_cursor.isNull(_cursorIndexOfChannelCategoryId)) {
              _tmpChannelCategoryId = null;
            } else {
              _tmpChannelCategoryId = _cursor.getString(_cursorIndexOfChannelCategoryId);
            }
            final String _tmpChannelStreamUrl;
            if (_cursor.isNull(_cursorIndexOfChannelStreamUrl)) {
              _tmpChannelStreamUrl = null;
            } else {
              _tmpChannelStreamUrl = _cursor.getString(_cursorIndexOfChannelStreamUrl);
            }
            final Integer _tmpChannelSortOrder;
            if (_cursor.isNull(_cursorIndexOfChannelSortOrder)) {
              _tmpChannelSortOrder = null;
            } else {
              _tmpChannelSortOrder = _cursor.getInt(_cursorIndexOfChannelSortOrder);
            }
            final String _tmpChannelContainerExtension;
            if (_cursor.isNull(_cursorIndexOfChannelContainerExtension)) {
              _tmpChannelContainerExtension = null;
            } else {
              _tmpChannelContainerExtension = _cursor.getString(_cursorIndexOfChannelContainerExtension);
            }
            final String _tmpChannelPlot;
            if (_cursor.isNull(_cursorIndexOfChannelPlot)) {
              _tmpChannelPlot = null;
            } else {
              _tmpChannelPlot = _cursor.getString(_cursorIndexOfChannelPlot);
            }
            final Long _tmpChannelAddedAt;
            if (_cursor.isNull(_cursorIndexOfChannelAddedAt)) {
              _tmpChannelAddedAt = null;
            } else {
              _tmpChannelAddedAt = _cursor.getLong(_cursorIndexOfChannelAddedAt);
            }
            _result = new LastWatchedRow(_tmpSourceId,_tmpChannelId,_tmpContentType,_tmpWatchedAt,_tmpPlaybackPositionMs,_tmpDurationMs,_tmpTitle,_tmpImageUrl,_tmpCategoryId,_tmpSeriesId,_tmpSeriesName,_tmpChannelName,_tmpChannelLogoUrl,_tmpChannelCategoryId,_tmpChannelStreamUrl,_tmpChannelSortOrder,_tmpChannelContainerExtension,_tmpChannelPlot,_tmpChannelAddedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
