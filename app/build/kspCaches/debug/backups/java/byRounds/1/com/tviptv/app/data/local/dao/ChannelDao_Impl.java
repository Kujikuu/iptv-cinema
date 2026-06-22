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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.tviptv.app.data.local.ContentTypeConverters;
import com.tviptv.app.data.local.entity.CategoryCount;
import com.tviptv.app.data.local.entity.ChannelEntity;
import com.tviptv.app.domain.model.ContentType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class ChannelDao_Impl implements ChannelDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ChannelEntity> __insertionAdapterOfChannelEntity;

  private final ContentTypeConverters __contentTypeConverters = new ContentTypeConverters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySource;

  public ChannelDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfChannelEntity = new EntityInsertionAdapter<ChannelEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `channels` (`sourceId`,`externalId`,`name`,`logoUrl`,`categoryId`,`streamUrl`,`sortOrder`,`contentType`,`containerExtension`,`plot`,`addedAt`,`channelNumber`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ChannelEntity entity) {
        statement.bindLong(1, entity.getSourceId());
        statement.bindString(2, entity.getExternalId());
        statement.bindString(3, entity.getName());
        if (entity.getLogoUrl() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getLogoUrl());
        }
        if (entity.getCategoryId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getCategoryId());
        }
        if (entity.getStreamUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStreamUrl());
        }
        statement.bindLong(7, entity.getSortOrder());
        final String _tmp = __contentTypeConverters.fromContentType(entity.getContentType());
        statement.bindString(8, _tmp);
        if (entity.getContainerExtension() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getContainerExtension());
        }
        if (entity.getPlot() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getPlot());
        }
        if (entity.getAddedAt() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getAddedAt());
        }
        if (entity.getChannelNumber() == null) {
          statement.bindNull(12);
        } else {
          statement.bindLong(12, entity.getChannelNumber());
        }
      }
    };
    this.__preparedStmtOfDeleteBySource = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM channels WHERE sourceId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<ChannelEntity> channels,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfChannelEntity.insert(channels);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
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
  public Object countBySource(final long sourceId, final ContentType contentType,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM channels WHERE sourceId = ? AND contentType = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
          } else {
            _result = 0;
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
  public Object countAllBySource(final long sourceId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM channels WHERE sourceId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Object getBySource(final long sourceId, final String categoryId,
      final ContentType contentType, final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND (? IS NULL OR categoryId = ?) AND (? IS NULL OR contentType = ?) ORDER BY addedAt DESC, sortOrder ASC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    if (categoryId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, categoryId);
    }
    _argIndex = 3;
    if (categoryId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, categoryId);
    }
    _argIndex = 4;
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
    _argIndex = 5;
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
  public Object getByCategory(final long sourceId, final String categoryId,
      final ContentType contentType, final int limit,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND categoryId = ? AND contentType = ? ORDER BY addedAt DESC, sortOrder ASC, name ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, categoryId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
  public Object getByCategoryPage(final long sourceId, final String categoryId,
      final ContentType contentType, final int limit, final int offset,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND categoryId = ? AND contentType = ? ORDER BY addedAt DESC, sortOrder ASC, name ASC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, categoryId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 4;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 5;
    _statement.bindLong(_argIndex, offset);
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
  public Object countByCategory(final long sourceId, final String categoryId,
      final ContentType contentType, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM channels WHERE sourceId = ? AND categoryId = ? AND contentType = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, categoryId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(0);
            _result = _tmp_1;
          } else {
            _result = 0;
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
  public Object getAllByCategory(final long sourceId, final String categoryId,
      final ContentType contentType, final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND categoryId = ? AND contentType = ? ORDER BY addedAt DESC, sortOrder ASC, name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, categoryId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
  public Object getCategoryCounts(final long sourceId, final ContentType contentType,
      final Continuation<? super List<CategoryCount>> $completion) {
    final String _sql = "SELECT categoryId, COUNT(*) as count, MAX(addedAt) as latestAddedAt FROM channels WHERE sourceId = ? AND contentType = ? AND categoryId IS NOT NULL GROUP BY categoryId";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CategoryCount>>() {
      @Override
      @NonNull
      public List<CategoryCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCategoryId = 0;
          final int _cursorIndexOfCount = 1;
          final int _cursorIndexOfLatestAddedAt = 2;
          final List<CategoryCount> _result = new ArrayList<CategoryCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CategoryCount _item;
            final String _tmpCategoryId;
            _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            final Long _tmpLatestAddedAt;
            if (_cursor.isNull(_cursorIndexOfLatestAddedAt)) {
              _tmpLatestAddedAt = null;
            } else {
              _tmpLatestAddedAt = _cursor.getLong(_cursorIndexOfLatestAddedAt);
            }
            _item = new CategoryCount(_tmpCategoryId,_tmpCount,_tmpLatestAddedAt);
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
  public Object getChannelsWithLogosForPreviews(final long sourceId, final ContentType contentType,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM channels\n"
            + "        WHERE sourceId = ?\n"
            + "            AND contentType = ?\n"
            + "            AND categoryId IS NOT NULL\n"
            + "            AND logoUrl IS NOT NULL\n"
            + "            AND logoUrl != ''\n"
            + "        ORDER BY categoryId ASC, addedAt DESC, sortOrder ASC, name ASC\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
  public Object searchByName(final long sourceId, final String query, final ContentType contentType,
      final int limit, final Continuation<? super List<ChannelEntity>> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND name LIKE '%' || ? || '%' AND (? IS NULL OR contentType = ?) ORDER BY addedAt DESC, name ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 5);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    _argIndex = 3;
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
    _argIndex = 4;
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
    _argIndex = 5;
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
  public Object getByChannelNumber(final long sourceId, final ContentType contentType,
      final int channelNumber, final Continuation<? super ChannelEntity> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND contentType = ? AND channelNumber = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 3;
    _statement.bindLong(_argIndex, channelNumber);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChannelEntity>() {
      @Override
      @Nullable
      public ChannelEntity call() throws Exception {
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
          final ChannelEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
            _result = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
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
  public Object getLiveChannelBySortIndex(final long sourceId, final int index,
      final Continuation<? super ChannelEntity> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM channels\n"
            + "        WHERE sourceId = ? AND contentType = 'LIVE'\n"
            + "        ORDER BY sortOrder ASC, name ASC\n"
            + "        LIMIT 1 OFFSET ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, index);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChannelEntity>() {
      @Override
      @Nullable
      public ChannelEntity call() throws Exception {
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
          final ChannelEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp);
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
            _result = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
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
  public Object searchByNameAllSources(final List<Long> sourceIds, final String query,
      final ContentType contentType, final int limit,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT * FROM channels WHERE sourceId IN (");
    final int _inputSize = sourceIds.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND name LIKE '%' || ");
    _stringBuilder.append("?");
    _stringBuilder.append(" || '%' AND (");
    _stringBuilder.append("?");
    _stringBuilder.append(" IS NULL OR contentType = ");
    _stringBuilder.append("?");
    _stringBuilder.append(") ORDER BY name ASC LIMIT ");
    _stringBuilder.append("?");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 4 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (long _item : sourceIds) {
      _statement.bindLong(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindString(_argIndex, query);
    _argIndex = 2 + _inputSize;
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
    _argIndex = 3 + _inputSize;
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
    _argIndex = 4 + _inputSize;
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
            final ChannelEntity _item_1;
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
            _item_1 = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
            _result.add(_item_1);
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
  public Object getByExternalId(final long sourceId, final String externalId,
      final ContentType contentType, final Continuation<? super ChannelEntity> $completion) {
    final String _sql = "SELECT * FROM channels WHERE sourceId = ? AND externalId = ? AND contentType = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, externalId);
    _argIndex = 3;
    final String _tmp = __contentTypeConverters.fromContentType(contentType);
    _statement.bindString(_argIndex, _tmp);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ChannelEntity>() {
      @Override
      @Nullable
      public ChannelEntity call() throws Exception {
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
          final ChannelEntity _result;
          if (_cursor.moveToFirst()) {
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
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfContentType);
            _tmpContentType = __contentTypeConverters.toContentType(_tmp_1);
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
            _result = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
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
  public Object searchByNameFtsRaw(final SupportSQLiteQuery query,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, query, false, null);
        try {
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            _item = __entityCursorConverter_comTviptvAppDataLocalEntityChannelEntity(_cursor);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }
    }, $completion);
  }

  @Override
  public Object searchByNameFtsAllSourcesRaw(final SupportSQLiteQuery query,
      final Continuation<? super List<ChannelEntity>> $completion) {
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ChannelEntity>>() {
      @Override
      @NonNull
      public List<ChannelEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, query, false, null);
        try {
          final List<ChannelEntity> _result = new ArrayList<ChannelEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ChannelEntity _item;
            _item = __entityCursorConverter_comTviptvAppDataLocalEntityChannelEntity(_cursor);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private ChannelEntity __entityCursorConverter_comTviptvAppDataLocalEntityChannelEntity(
      @NonNull final Cursor cursor) {
    final ChannelEntity _entity;
    final int _cursorIndexOfSourceId = CursorUtil.getColumnIndex(cursor, "sourceId");
    final int _cursorIndexOfExternalId = CursorUtil.getColumnIndex(cursor, "externalId");
    final int _cursorIndexOfName = CursorUtil.getColumnIndex(cursor, "name");
    final int _cursorIndexOfLogoUrl = CursorUtil.getColumnIndex(cursor, "logoUrl");
    final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndex(cursor, "categoryId");
    final int _cursorIndexOfStreamUrl = CursorUtil.getColumnIndex(cursor, "streamUrl");
    final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndex(cursor, "sortOrder");
    final int _cursorIndexOfContentType = CursorUtil.getColumnIndex(cursor, "contentType");
    final int _cursorIndexOfContainerExtension = CursorUtil.getColumnIndex(cursor, "containerExtension");
    final int _cursorIndexOfPlot = CursorUtil.getColumnIndex(cursor, "plot");
    final int _cursorIndexOfAddedAt = CursorUtil.getColumnIndex(cursor, "addedAt");
    final int _cursorIndexOfChannelNumber = CursorUtil.getColumnIndex(cursor, "channelNumber");
    final long _tmpSourceId;
    if (_cursorIndexOfSourceId == -1) {
      _tmpSourceId = 0;
    } else {
      _tmpSourceId = cursor.getLong(_cursorIndexOfSourceId);
    }
    final String _tmpExternalId;
    if (_cursorIndexOfExternalId == -1) {
      _tmpExternalId = null;
    } else {
      _tmpExternalId = cursor.getString(_cursorIndexOfExternalId);
    }
    final String _tmpName;
    if (_cursorIndexOfName == -1) {
      _tmpName = null;
    } else {
      _tmpName = cursor.getString(_cursorIndexOfName);
    }
    final String _tmpLogoUrl;
    if (_cursorIndexOfLogoUrl == -1) {
      _tmpLogoUrl = null;
    } else {
      if (cursor.isNull(_cursorIndexOfLogoUrl)) {
        _tmpLogoUrl = null;
      } else {
        _tmpLogoUrl = cursor.getString(_cursorIndexOfLogoUrl);
      }
    }
    final String _tmpCategoryId;
    if (_cursorIndexOfCategoryId == -1) {
      _tmpCategoryId = null;
    } else {
      if (cursor.isNull(_cursorIndexOfCategoryId)) {
        _tmpCategoryId = null;
      } else {
        _tmpCategoryId = cursor.getString(_cursorIndexOfCategoryId);
      }
    }
    final String _tmpStreamUrl;
    if (_cursorIndexOfStreamUrl == -1) {
      _tmpStreamUrl = null;
    } else {
      if (cursor.isNull(_cursorIndexOfStreamUrl)) {
        _tmpStreamUrl = null;
      } else {
        _tmpStreamUrl = cursor.getString(_cursorIndexOfStreamUrl);
      }
    }
    final int _tmpSortOrder;
    if (_cursorIndexOfSortOrder == -1) {
      _tmpSortOrder = 0;
    } else {
      _tmpSortOrder = cursor.getInt(_cursorIndexOfSortOrder);
    }
    final ContentType _tmpContentType;
    if (_cursorIndexOfContentType == -1) {
      _tmpContentType = null;
    } else {
      final String _tmp;
      _tmp = cursor.getString(_cursorIndexOfContentType);
      _tmpContentType = __contentTypeConverters.toContentType(_tmp);
    }
    final String _tmpContainerExtension;
    if (_cursorIndexOfContainerExtension == -1) {
      _tmpContainerExtension = null;
    } else {
      if (cursor.isNull(_cursorIndexOfContainerExtension)) {
        _tmpContainerExtension = null;
      } else {
        _tmpContainerExtension = cursor.getString(_cursorIndexOfContainerExtension);
      }
    }
    final String _tmpPlot;
    if (_cursorIndexOfPlot == -1) {
      _tmpPlot = null;
    } else {
      if (cursor.isNull(_cursorIndexOfPlot)) {
        _tmpPlot = null;
      } else {
        _tmpPlot = cursor.getString(_cursorIndexOfPlot);
      }
    }
    final Long _tmpAddedAt;
    if (_cursorIndexOfAddedAt == -1) {
      _tmpAddedAt = null;
    } else {
      if (cursor.isNull(_cursorIndexOfAddedAt)) {
        _tmpAddedAt = null;
      } else {
        _tmpAddedAt = cursor.getLong(_cursorIndexOfAddedAt);
      }
    }
    final Integer _tmpChannelNumber;
    if (_cursorIndexOfChannelNumber == -1) {
      _tmpChannelNumber = null;
    } else {
      if (cursor.isNull(_cursorIndexOfChannelNumber)) {
        _tmpChannelNumber = null;
      } else {
        _tmpChannelNumber = cursor.getInt(_cursorIndexOfChannelNumber);
      }
    }
    _entity = new ChannelEntity(_tmpSourceId,_tmpExternalId,_tmpName,_tmpLogoUrl,_tmpCategoryId,_tmpStreamUrl,_tmpSortOrder,_tmpContentType,_tmpContainerExtension,_tmpPlot,_tmpAddedAt,_tmpChannelNumber);
    return _entity;
  }
}
