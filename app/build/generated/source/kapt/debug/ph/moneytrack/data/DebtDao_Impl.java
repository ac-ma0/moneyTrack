package ph.moneytrack.data;

import android.database.Cursor;
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
import java.lang.Long;
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
public final class DebtDao_Impl implements DebtDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Debt> __insertionAdapterOfDebt;

  private final SharedSQLiteStatement __preparedStmtOfSetStatus;

  private final SharedSQLiteStatement __preparedStmtOfSoftDelete;

  public DebtDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDebt = new EntityInsertionAdapter<Debt>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `debts` (`id`,`userId`,`person`,`principalAmount`,`interestRate`,`interestType`,`numberOfMonths`,`startDate`,`dueDate`,`monthlyExpectedPayment`,`totalPayable`,`notes`,`status`,`updatedAt`,`deleted`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Debt value) {
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
        if (value.getPerson() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getPerson());
        }
        stmt.bindLong(4, value.getPrincipalAmount());
        stmt.bindDouble(5, value.getInterestRate());
        if (value.getInterestType() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.getInterestType());
        }
        stmt.bindLong(7, value.getNumberOfMonths());
        if (value.getStartDate() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getStartDate());
        }
        if (value.getDueDate() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getDueDate());
        }
        if (value.getMonthlyExpectedPayment() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindLong(10, value.getMonthlyExpectedPayment());
        }
        if (value.getTotalPayable() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindLong(11, value.getTotalPayable());
        }
        if (value.getNotes() == null) {
          stmt.bindNull(12);
        } else {
          stmt.bindString(12, value.getNotes());
        }
        if (value.getStatus() == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindString(13, value.getStatus());
        }
        stmt.bindLong(14, value.getUpdatedAt());
        final int _tmp;
        _tmp = value.getDeleted() ? 1 : 0;
        stmt.bindLong(15, _tmp);
      }
    };
    this.__preparedStmtOfSetStatus = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE debts SET status=?, updatedAt=? WHERE id=? AND userId=?";
        return _query;
      }
    };
    this.__preparedStmtOfSoftDelete = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE debts SET deleted=1, updatedAt=? WHERE id=? AND userId=?";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final Debt value, final Continuation<? super Unit> p1) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDebt.insert(value);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, p1);
  }

  @Override
  public Object setStatus(final String id, final String userId, final String status, final long at,
      final Continuation<? super Unit> p4) {
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
        _stmt.bindLong(_argIndex, at);
        _argIndex = 3;
        if (id == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, id);
        }
        _argIndex = 4;
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
    }, p4);
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
  public Flow<List<Debt>> observe(final String userId) {
    final String _sql = "SELECT * FROM debts WHERE userId=? AND deleted=0 ORDER BY startDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (userId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, userId);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"debts"}, new Callable<List<Debt>>() {
      @Override
      public List<Debt> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfPerson = CursorUtil.getColumnIndexOrThrow(_cursor, "person");
          final int _cursorIndexOfPrincipalAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "principalAmount");
          final int _cursorIndexOfInterestRate = CursorUtil.getColumnIndexOrThrow(_cursor, "interestRate");
          final int _cursorIndexOfInterestType = CursorUtil.getColumnIndexOrThrow(_cursor, "interestType");
          final int _cursorIndexOfNumberOfMonths = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfMonths");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfDueDate = CursorUtil.getColumnIndexOrThrow(_cursor, "dueDate");
          final int _cursorIndexOfMonthlyExpectedPayment = CursorUtil.getColumnIndexOrThrow(_cursor, "monthlyExpectedPayment");
          final int _cursorIndexOfTotalPayable = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPayable");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final int _cursorIndexOfDeleted = CursorUtil.getColumnIndexOrThrow(_cursor, "deleted");
          final List<Debt> _result = new ArrayList<Debt>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final Debt _item;
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
            final String _tmpPerson;
            if (_cursor.isNull(_cursorIndexOfPerson)) {
              _tmpPerson = null;
            } else {
              _tmpPerson = _cursor.getString(_cursorIndexOfPerson);
            }
            final long _tmpPrincipalAmount;
            _tmpPrincipalAmount = _cursor.getLong(_cursorIndexOfPrincipalAmount);
            final double _tmpInterestRate;
            _tmpInterestRate = _cursor.getDouble(_cursorIndexOfInterestRate);
            final String _tmpInterestType;
            if (_cursor.isNull(_cursorIndexOfInterestType)) {
              _tmpInterestType = null;
            } else {
              _tmpInterestType = _cursor.getString(_cursorIndexOfInterestType);
            }
            final int _tmpNumberOfMonths;
            _tmpNumberOfMonths = _cursor.getInt(_cursorIndexOfNumberOfMonths);
            final String _tmpStartDate;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmpStartDate = null;
            } else {
              _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            }
            final String _tmpDueDate;
            if (_cursor.isNull(_cursorIndexOfDueDate)) {
              _tmpDueDate = null;
            } else {
              _tmpDueDate = _cursor.getString(_cursorIndexOfDueDate);
            }
            final Long _tmpMonthlyExpectedPayment;
            if (_cursor.isNull(_cursorIndexOfMonthlyExpectedPayment)) {
              _tmpMonthlyExpectedPayment = null;
            } else {
              _tmpMonthlyExpectedPayment = _cursor.getLong(_cursorIndexOfMonthlyExpectedPayment);
            }
            final Long _tmpTotalPayable;
            if (_cursor.isNull(_cursorIndexOfTotalPayable)) {
              _tmpTotalPayable = null;
            } else {
              _tmpTotalPayable = _cursor.getLong(_cursorIndexOfTotalPayable);
            }
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            final boolean _tmpDeleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfDeleted);
            _tmpDeleted = _tmp != 0;
            _item = new Debt(_tmpId,_tmpUserId,_tmpPerson,_tmpPrincipalAmount,_tmpInterestRate,_tmpInterestType,_tmpNumberOfMonths,_tmpStartDate,_tmpDueDate,_tmpMonthlyExpectedPayment,_tmpTotalPayable,_tmpNotes,_tmpStatus,_tmpUpdatedAt,_tmpDeleted);
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

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
