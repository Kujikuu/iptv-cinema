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
import com.tviptv.app.data.local.entity.EpgProgramEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class EpgDao_Impl implements EpgDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EpgProgramEntity> __insertionAdapterOfEpgProgramEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByStream;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBySource;

  public EpgDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEpgProgramEntity = new EntityInsertionAdapter<EpgProgramEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `epg_programs` (`sourceId`,`streamId`,`title`,`description`,`startMs`,`endMs`,`fetchedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EpgProgramEntity entity) {
        statement.bindLong(1, entity.getSourceId());
        statement.bindString(2, entity.getStreamId());
        statement.bindString(3, entity.getTitle());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        statement.bindLong(5, entity.getStartMs());
        statement.bindLong(6, entity.getEndMs());
        statement.bindLong(7, entity.getFetchedAt());
      }
    };
    this.__preparedStmtOfDeleteByStream = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM epg_programs WHERE sourceId = ? AND streamId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteBySource = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM epg_programs WHERE sourceId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object upsertAll(final List<EpgProgramEntity> programs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEpgProgramEntity.insert(programs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByStream(final long sourceId, final String streamId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByStream.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, sourceId);
        _argIndex = 2;
        _stmt.bindString(_argIndex, streamId);
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
          __preparedStmtOfDeleteByStream.release(_stmt);
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
  public Object getCurrentProgram(final long sourceId, final String streamId, final long nowMs,
      final Continuation<? super EpgProgramEntity> $completion) {
    final String _sql = "SELECT * FROM epg_programs WHERE sourceId = ? AND streamId = ? AND startMs <= ? AND endMs > ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, streamId);
    _argIndex = 3;
    _statement.bindLong(_argIndex, nowMs);
    _argIndex = 4;
    _statement.bindLong(_argIndex, nowMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EpgProgramEntity>() {
      @Override
      @Nullable
      public EpgProgramEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfStreamId = CursorUtil.getColumnIndexOrThrow(_cursor, "streamId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfStartMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startMs");
          final int _cursorIndexOfEndMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endMs");
          final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
          final EpgProgramEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpStreamId;
            _tmpStreamId = _cursor.getString(_cursorIndexOfStreamId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpStartMs;
            _tmpStartMs = _cursor.getLong(_cursorIndexOfStartMs);
            final long _tmpEndMs;
            _tmpEndMs = _cursor.getLong(_cursorIndexOfEndMs);
            final long _tmpFetchedAt;
            _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
            _result = new EpgProgramEntity(_tmpSourceId,_tmpStreamId,_tmpTitle,_tmpDescription,_tmpStartMs,_tmpEndMs,_tmpFetchedAt);
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
  public Object getNextProgram(final long sourceId, final String streamId, final long nowMs,
      final Continuation<? super EpgProgramEntity> $completion) {
    final String _sql = "SELECT * FROM epg_programs WHERE sourceId = ? AND streamId = ? AND startMs >= ? ORDER BY startMs ASC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, streamId);
    _argIndex = 3;
    _statement.bindLong(_argIndex, nowMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EpgProgramEntity>() {
      @Override
      @Nullable
      public EpgProgramEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfStreamId = CursorUtil.getColumnIndexOrThrow(_cursor, "streamId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfStartMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startMs");
          final int _cursorIndexOfEndMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endMs");
          final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
          final EpgProgramEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpStreamId;
            _tmpStreamId = _cursor.getString(_cursorIndexOfStreamId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpStartMs;
            _tmpStartMs = _cursor.getLong(_cursorIndexOfStartMs);
            final long _tmpEndMs;
            _tmpEndMs = _cursor.getLong(_cursorIndexOfEndMs);
            final long _tmpFetchedAt;
            _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
            _result = new EpgProgramEntity(_tmpSourceId,_tmpStreamId,_tmpTitle,_tmpDescription,_tmpStartMs,_tmpEndMs,_tmpFetchedAt);
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
  public Object getProgramsForStream(final long sourceId, final String streamId,
      final Continuation<? super List<EpgProgramEntity>> $completion) {
    final String _sql = "SELECT * FROM epg_programs WHERE sourceId = ? AND streamId = ? ORDER BY startMs ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, streamId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EpgProgramEntity>>() {
      @Override
      @NonNull
      public List<EpgProgramEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSourceId = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceId");
          final int _cursorIndexOfStreamId = CursorUtil.getColumnIndexOrThrow(_cursor, "streamId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfStartMs = CursorUtil.getColumnIndexOrThrow(_cursor, "startMs");
          final int _cursorIndexOfEndMs = CursorUtil.getColumnIndexOrThrow(_cursor, "endMs");
          final int _cursorIndexOfFetchedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "fetchedAt");
          final List<EpgProgramEntity> _result = new ArrayList<EpgProgramEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EpgProgramEntity _item;
            final long _tmpSourceId;
            _tmpSourceId = _cursor.getLong(_cursorIndexOfSourceId);
            final String _tmpStreamId;
            _tmpStreamId = _cursor.getString(_cursorIndexOfStreamId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpStartMs;
            _tmpStartMs = _cursor.getLong(_cursorIndexOfStartMs);
            final long _tmpEndMs;
            _tmpEndMs = _cursor.getLong(_cursorIndexOfEndMs);
            final long _tmpFetchedAt;
            _tmpFetchedAt = _cursor.getLong(_cursorIndexOfFetchedAt);
            _item = new EpgProgramEntity(_tmpSourceId,_tmpStreamId,_tmpTitle,_tmpDescription,_tmpStartMs,_tmpEndMs,_tmpFetchedAt);
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
  public Object getLastFetchedAt(final long sourceId, final String streamId,
      final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(fetchedAt) FROM epg_programs WHERE sourceId = ? AND streamId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, sourceId);
    _argIndex = 2;
    _statement.bindString(_argIndex, streamId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
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
