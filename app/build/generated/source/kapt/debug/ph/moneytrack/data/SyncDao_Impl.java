package ph.moneytrack.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SyncDao_Impl implements SyncDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SyncQueue> __insertionAdapterOfSyncQueue;

  private final EntityDeletionOrUpdateAdapter<SyncQueue> __deletionAdapterOfSyncQueue;

  private final SharedSQLiteStatement __preparedStmtOfFailed;

  public SyncDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSyncQueue = new EntityInsertionAdapter<SyncQueue>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `sync_queue` (`id`,`userId`,`operation`,`recordType`,`recordId`,`payload`,`createdAt`,`attempts`,`lastError`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SyncQueue value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        if (value.getUserId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getUserId());
        }
        if (value.getOperation() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getOperation());
        }
        if (value.getRecordType() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getRecordType());
        }
        if (value.getRecordId() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getRecordId());
        }
        if (value.getPayload() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getPayload());
        }
        stmt.bindLong(7, value.getCreatedAt());
        stmt.bindLong(8, value.getAttempts());
        if (value.getLastError() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getLastError());
        }
      }
    };
    this.__deletionAdapterOfSyncQueue = new EntityDeletionOrUpdateAdapter<SyncQueue>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `sync_queue` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, SyncQueue value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
      }
    };
    this.__preparedStmtOfFailed = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE sync_queue SET attempts=attempts+1,lastError=? WHERE id=?";
        return _query;
      }
    };
  }

  @Override
  public Object enqueue(final SyncQueue value, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSyncQueue.insert(value);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  @Override
  public Object remove(final SyncQueue value, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSyncQueue.handle(value);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  @Override
  public Object failed(final String id, final String error, final Continuation<? super Unit> p2) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfFailed.acquire();
        int _argIndex = 1;
        if (error == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, error);
        }
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfFailed.release(_stmt);
        }
      }
    }, p2);
  }

  @Override
  public Object next(final int limit, final Continuation<? super List<SyncQueue>> p1) {
    final String _sql = "SELECT * FROM sync_queue ORDER BY createdAt LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SyncQueue>>() {
      @Override
      public List<SyncQueue> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfOperation = CursorUtil.getColumnIndexOrThrow(_cursor, "operation");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfPayload = CursorUtil.getColumnIndexOrThrow(_cursor, "payload");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfAttempts = CursorUtil.getColumnIndexOrThrow(_cursor, "attempts");
          final int _cursorIndexOfLastError = CursorUtil.getColumnIndexOrThrow(_cursor, "lastError");
          final List<SyncQueue> _result = new ArrayList<SyncQueue>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final SyncQueue _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpOperation;
            if (_cursor.isNull(_cursorIndexOfOperation)) {
              _tmpOperation = null;
            } else {
              _tmpOperation = _cursor.getString(_cursorIndexOfOperation);
            }
            final String _tmpRecordType;
            if (_cursor.isNull(_cursorIndexOfRecordType)) {
              _tmpRecordType = null;
            } else {
              _tmpRecordType = _cursor.getString(_cursorIndexOfRecordType);
            }
            final String _tmpRecordId;
            if (_cursor.isNull(_cursorIndexOfRecordId)) {
              _tmpRecordId = null;
            } else {
              _tmpRecordId = _cursor.getString(_cursorIndexOfRecordId);
            }
            final String _tmpPayload;
            if (_cursor.isNull(_cursorIndexOfPayload)) {
              _tmpPayload = null;
            } else {
              _tmpPayload = _cursor.getString(_cursorIndexOfPayload);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final int _tmpAttempts;
            _tmpAttempts = _cursor.getInt(_cursorIndexOfAttempts);
            final String _tmpLastError;
            if (_cursor.isNull(_cursorIndexOfLastError)) {
              _tmpLastError = null;
            } else {
              _tmpLastError = _cursor.getString(_cursorIndexOfLastError);
            }
            _item = new SyncQueue(_tmpId,_tmpUserId,_tmpOperation,_tmpRecordType,_tmpRecordId,_tmpPayload,_tmpCreatedAt,_tmpAttempts,_tmpLastError);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, p1);
  }

  @Override
  public Object count(final Continuation<? super Integer> p0) {
    final String _sql = "SELECT COUNT(*) FROM sync_queue";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
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
    }, p0);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
