package ph.moneytrack.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AuditDao_Impl implements AuditDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AuditLog> __insertionAdapterOfAuditLog;

  public AuditDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAuditLog = new EntityInsertionAdapter<AuditLog>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `audit_logs` (`id`,`userId`,`action`,`recordType`,`recordId`,`oldValue`,`newValue`,`timestamp`,`uploaded`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, AuditLog value) {
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
        if (value.getAction() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getAction());
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
        if (value.getOldValue() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getOldValue());
        }
        if (value.getNewValue() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getNewValue());
        }
        stmt.bindLong(8, value.getTimestamp());
        final int _tmp;
        _tmp = value.getUploaded() ? 1 : 0;
        stmt.bindLong(9, _tmp);
      }
    };
  }

  @Override
  public Object insert(final AuditLog value, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAuditLog.insert(value);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  @Override
  public Flow<List<AuditLog>> observe(final String userId) {
    final String _sql = "SELECT * FROM audit_logs WHERE userId=? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"audit_logs"}, new Callable<List<AuditLog>>() {
      @Override
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfOldValue = CursorUtil.getColumnIndexOrThrow(_cursor, "oldValue");
          final int _cursorIndexOfNewValue = CursorUtil.getColumnIndexOrThrow(_cursor, "newValue");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AuditLog _item;
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
            final String _tmpAction;
            if (_cursor.isNull(_cursorIndexOfAction)) {
              _tmpAction = null;
            } else {
              _tmpAction = _cursor.getString(_cursorIndexOfAction);
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
            final String _tmpOldValue;
            if (_cursor.isNull(_cursorIndexOfOldValue)) {
              _tmpOldValue = null;
            } else {
              _tmpOldValue = _cursor.getString(_cursorIndexOfOldValue);
            }
            final String _tmpNewValue;
            if (_cursor.isNull(_cursorIndexOfNewValue)) {
              _tmpNewValue = null;
            } else {
              _tmpNewValue = _cursor.getString(_cursorIndexOfNewValue);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpUploaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp != 0;
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpAction,_tmpRecordType,_tmpRecordId,_tmpOldValue,_tmpNewValue,_tmpTimestamp,_tmpUploaded);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object pending(final int limit, final Continuation<? super List<AuditLog>> p1) {
    final String _sql = "SELECT * FROM audit_logs WHERE uploaded=0 LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AuditLog>>() {
      @Override
      public List<AuditLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfAction = CursorUtil.getColumnIndexOrThrow(_cursor, "action");
          final int _cursorIndexOfRecordType = CursorUtil.getColumnIndexOrThrow(_cursor, "recordType");
          final int _cursorIndexOfRecordId = CursorUtil.getColumnIndexOrThrow(_cursor, "recordId");
          final int _cursorIndexOfOldValue = CursorUtil.getColumnIndexOrThrow(_cursor, "oldValue");
          final int _cursorIndexOfNewValue = CursorUtil.getColumnIndexOrThrow(_cursor, "newValue");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUploaded = CursorUtil.getColumnIndexOrThrow(_cursor, "uploaded");
          final List<AuditLog> _result = new ArrayList<AuditLog>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final AuditLog _item;
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
            final String _tmpAction;
            if (_cursor.isNull(_cursorIndexOfAction)) {
              _tmpAction = null;
            } else {
              _tmpAction = _cursor.getString(_cursorIndexOfAction);
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
            final String _tmpOldValue;
            if (_cursor.isNull(_cursorIndexOfOldValue)) {
              _tmpOldValue = null;
            } else {
              _tmpOldValue = _cursor.getString(_cursorIndexOfOldValue);
            }
            final String _tmpNewValue;
            if (_cursor.isNull(_cursorIndexOfNewValue)) {
              _tmpNewValue = null;
            } else {
              _tmpNewValue = _cursor.getString(_cursorIndexOfNewValue);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final boolean _tmpUploaded;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfUploaded);
            _tmpUploaded = _tmp != 0;
            _item = new AuditLog(_tmpId,_tmpUserId,_tmpAction,_tmpRecordType,_tmpRecordId,_tmpOldValue,_tmpNewValue,_tmpTimestamp,_tmpUploaded);
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
  public Object markUploaded(final List<String> ids, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("UPDATE audit_logs SET uploaded=1 WHERE id IN (");
        final int _inputSize = ids.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : ids) {
          if (_item == null) {
            _stmt.bindNull(_argIndex);
          } else {
            _stmt.bindString(_argIndex, _item);
          }
          _argIndex ++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
