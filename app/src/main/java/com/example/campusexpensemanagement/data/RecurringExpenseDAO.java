package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.RecurringExpense;

import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public RecurringExpenseDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    public long addRecurringExpense(RecurringExpense recurringExpense) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Sửa lỗi: sử dụng cột đúng để lưu expenseId
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_EXPENSE_ID, recurringExpense.getExpenseId());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_FREQUENCY, recurringExpense.getFrequency());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_START_DATE, recurringExpense.getStartDate());
        values.put(ExpenseDatabaseHelper.COLUMN_RECURRING_EXPENSE_END_DATE, recurringExpense.getEndDate());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_RECURRING_EXPENSE, null, values);
        db.close();
        return result;
    }

    public List<RecurringExpense> getUserRecurringExpenses(int userId) {
        List<RecurringExpense> recurringExpenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Sửa lại thành:
        Cursor cursor = db.rawQuery("SELECT * FROM recurring_expense WHERE expense_id IN " +
                "(SELECT id FROM expenses WHERE userId = ?)", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                RecurringExpense recurringExpense = new RecurringExpense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("expense_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("frequency")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("start_date")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("end_date"))
                );
                recurringExpenses.add(recurringExpense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return recurringExpenses;
    }

}
