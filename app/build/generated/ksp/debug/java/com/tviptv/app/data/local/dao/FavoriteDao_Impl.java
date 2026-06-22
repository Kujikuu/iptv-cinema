package com.tviptv.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
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
import com.tviptv.app.data.local.entity.FavoriteEntity;
import com.tviptv.app.domain.model.ContentType;
import java.lang.Boolean;
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
public final class FavoriteDao_Impl implements FavoriteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FavoriteEntity> __insertionAdapterOfFavoriteEntity;

  private final ContentTypeConverters __contentTypeConverters = new ContentTypeConverters();

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySource;

  public FavoriteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFavoriteEntity = new EntityInsertionAdapter<FavoriteEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `favorites` (`sourceId`,`channelId`,`contentType`,`addedAt`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FavoriteEntity entity) {
        statement.bindLong(1, entity.getSourceId());
        statement.bindString(2, entity.getChannelId());
        final String _tmp = __contentTypeConverters.fromContentType(entity.getContentType());
        statement.bindString(3, _tmp);
        statement.bindLong(4, entity.getAddedAt());
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM favorites WHERE sourceId = ? AND channelId = ? AND contentType = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBySource = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM favorites WHERE sourceId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final FavoriteEntity favorite,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFavoriteEntity.insert(favorite);
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
  public Object getFavoriteChannels(final long sourceId, final ContentType contentType,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT c.* FROM channels c\n"
            + "        INNER JOIN favorites f ON c.sourceId = f.sourceId\n"
            + "            AND c.externalId = f.channelId\n"
            + "            AND c.contentType = f.contentType\n"
            + "        WHERE f.sourceId = ?\n"
            + "        AND (? IS NULL OR c.contentType = ?)\n"
            + "        ORDER BY f.addedAt DESC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
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
  public Object isFavorite(final long sourceId, final String channelId,
      final ContentType contentType, final Continuation<? super Boolean> $completion) {
    final String _sql = "SELECT EXISTS(SELECT 1 FROM favorites WHERE sourceId = ? AND channelId = ? AND contentType = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, channelId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Boolean>() {
      @Override
      @NonNull
      public Boolean call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Boolean _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1 != 0;
          } else {
            _result = false;
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
  public Object getFavoriteChannelIds(final long sourceId, final ContentType contentType,
      final Continuation<? super List<String>> $completion) {
    final String _sql = "SELECT channelId FROM favorites WHERE sourceId = ? AND (? IS NULL OR contentType = ?)";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
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
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
