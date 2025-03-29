package com.example.campusexpensemanagement.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.RecurringExpense;

public class RecurringExpenseDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public RecurringExpenseDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm chi tiêu định kỳ
    public long addRecurringExpense(RecurringExpense recurringExpense) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_ID, recurringExpense.getExpenseId());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_FREQUENCY, recurringExpense.getFrequency());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_START_DATE, recurringExpense.getStartDate());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_END_DATE, recurringExpense.getEndDate());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_RECURRING_EXPENSE, null, values);
        db.close();
        return result;
    }

    public void deleteRecurringExpense(int recurringExpenseId) {
        db = dbHelper.getWritableDatabase();
        db.delete(ExpenseDatabaseHelper.TABLE_RECURRING_EXPENSE,
                ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_ID + " = ?", new String[]{String.valueOf(recurringExpenseId)});
        db.close();
    }

}
