package com.example.campusexpensemanagement.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.TransactionHistory;

public class TransactionHistoryDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public TransactionHistoryDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm giao dịch
    public long addTransaction(TransactionHistory transactionHistory) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_TRANSACTION_USER_ID, transactionHistory.getUserId());
        values.put(ExpenseDatabaseHelper.COLUMN_TRANSACTION_AMOUNT, transactionHistory.getAmount());
        values.put(ExpenseDatabaseHelper.COLUMN_TRANSACTION_DESCRIPTION, transactionHistory.getDescription());
        values.put(ExpenseDatabaseHelper.COLUMN_TRANSACTION_DATE, transactionHistory.getDate());
        values.put(ExpenseDatabaseHelper.COLUMN_TRANSACTION_TYPE, transactionHistory.getType());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_TRANSACTION_HISTORY, null, values);
        db.close();
        return result;
    }

    // Lấy tất cả giao dịch của người dùng
    public Cursor getAllTransactions(int userId) {
        db = dbHelper.getReadableDatabase();
        return db.query(ExpenseDatabaseHelper.TABLE_TRANSACTION_HISTORY, null,
                ExpenseDatabaseHelper.COLUMN_TRANSACTION_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, ExpenseDatabaseHelper.COLUMN_TRANSACTION_DATE + " DESC");
    }

    // Xóa giao dịch theo ID
    public void deleteTransaction(int transactionId) {
        db = dbHelper.getWritableDatabase();
        db.delete(ExpenseDatabaseHelper.TABLE_TRANSACTION_HISTORY,
                ExpenseDatabaseHelper.COLUMN_TRANSACTION_ID + " = ?", new String[]{String.valueOf(transactionId)});
        db.close();
    }
}
