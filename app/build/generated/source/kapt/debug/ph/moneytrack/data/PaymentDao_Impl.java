package ph.moneytrack.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class PaymentDao_Impl implements PaymentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DebtMonthlyPayment> __insertionAdapterOfDebtMonthlyPayment;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  private final SharedSQLiteStatement __preparedStmtOfSetStatus;

  public PaymentDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDebtMonthlyPayment = new EntityInsertionAdapter<DebtMonthlyPayment>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `debt_monthly_payments` (`id`,`debtId`,`userId`,`paymentMonth`,`amount`,`status`,`notes`,`updatedAt`,`deleted`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, DebtMonthlyPayment value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        if (value.getDebtId() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getDebtId());
        }
        if (value.getUserId() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getUserId());
        }
        if (value.getPaymentMonth() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getPaymentMonth());
        }
        stmt.bindLong(5, value.getAmount());
        if (value.getStatus() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getStatus());
        }
        if (value.getNotes() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.getNotes());
        }
        stmt.bindLong(8, value.getUpdatedAt());
        final int _tmp;
        _tmp = value.getDeleted() ? 1 : 0;
        stmt.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE debt_monthly_payments SET deleted=1, updatedAt=? WHERE id=? AND userId=?";
        return _query;
      }
    };
    this.__preparedStmtOfSetStatus = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE debt_monthly_payments SET status=?, amount=?, updatedAt=? WHERE id=? AND userId=?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final DebtMonthlyPayment value, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDebtMonthlyPayment.insert(value);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  @Override
  public Object softDelete(final String id, final String userId, final long at,
      final Continuation<? super Unit> p3) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSoftDelete.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, at);
        _argIndex = 2;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        _argIndex = 3;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, userId);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfSoftDelete.release(_stmt);
        }
      }
    }, p3);
  }

  @Override
  public Object setStatus(final String id, final String userId, final String status,
      final long amount, final long at, final Continuation<? super Unit> p5) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetStatus.acquire();
        int _argIndex = 1;
        if (status == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, status);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, amount);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, at);
        _argIndex = 4;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        _argIndex = 5;
        if (userId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, userId);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfSetStatus.release(_stmt);
        }
      }
    }, p5);
  }

  @Override
  public Flow<List<DebtMonthlyPayment>> observe(final String debtId, final String userId) {
    final String _sql = "SELECT * FROM debt_monthly_payments WHERE debtId=? AND userId=? AND deleted=0 ORDER BY paymentMonth";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (debtId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, debtId);
    }
    _argIndex = 2;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"debt_monthly_payments"}, new Callable<List<DebtMonthlyPayment>>() {
      @Override
      public List<DebtMonthlyPayment> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDebtId = CursorUtil.getColumnIndexOrThrow(_cursor, "debtId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfPaymentMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMonth");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted");
          final List<DebtMonthlyPayment> _result = new ArrayList<DebtMonthlyPayment>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final DebtMonthlyPayment _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDebtId;
            if (_cursor.isNull(_cursorIndexOfDebtId)) {
              _tmpDebtId = null;
            } else {
              _tmpDebtId = _cursor.getString(_cursorIndexOfDebtId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpPaymentMonth;
            if (_cursor.isNull(_cursorIndexOfPaymentMonth)) {
              _tmpPaymentMonth = null;
            } else {
              _tmpPaymentMonth = _cursor.getString(_cursorIndexOfPaymentMonth);
            }
            final long _tmpAmount;
            _tmpAmount = _cursor.getLong(_cursorIndexOfAmount);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDeleted);
            _tmpDeleted = _tmp != 0;
            _item = new DebtMonthlyPayment(_tmpId,_tmpDebtId,_tmpUserId,_tmpPaymentMonth,_tmpAmount,_tmpStatus,_tmpNotes,_tmpUpdatedAt,_tmpDeleted);
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
  public Object get(final String id, final String userId,
      final Continuation<? super DebtMonthlyPayment> p2) {
    final String _sql = "SELECT * FROM debt_monthly_payments WHERE id=? AND userId=? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    _argIndex = 2;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DebtMonthlyPayment>() {
      @Override
      public DebtMonthlyPayment call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDebtId = CursorUtil.getColumnIndexOrThrow(_cursor, "debtId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfPaymentMonth = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMonth");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted");
          final DebtMonthlyPayment _result;
          if(_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpDebtId;
            if (_cursor.isNull(_cursorIndexOfDebtId)) {
              _tmpDebtId = null;
            } else {
              _tmpDebtId = _cursor.getString(_cursorIndexOfDebtId);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpPaymentMonth;
            if (_cursor.isNull(_cursorIndexOfPaymentMonth)) {
              _tmpPaymentMonth = null;
            } else {
              _tmpPaymentMonth = _cursor.getString(_cursorIndexOfPaymentMonth);
            }
            final long _tmpAmount;
            _tmpAmount = _cursor.getLong(_cursorIndexOfAmount);
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDeleted);
            _tmpDeleted = _tmp != 0;
            _result = new DebtMonthlyPayment(_tmpId,_tmpDebtId,_tmpUserId,_tmpPaymentMonth,_tmpAmount,_tmpStatus,_tmpNotes,_tmpUpdatedAt,_tmpDeleted);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, p2);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
