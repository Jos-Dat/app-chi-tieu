package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.Expense;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public ExpenseDAO(Context context){
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    public long addExpense(Expense expense){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE, expense.getDate());
        long results = db.insert(ExpenseDatabaseHelper.TABLE_EXPENSE, null, values);
        db.close();
        return results;
    }

    @SuppressLint("Range")
    public List<Expense> GetAllExpenses(){
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_EXPENSE,
                null, null, null, null, null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE))
                );
                expense.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_ID)));
                expenses.add(expense);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return expenses;

    }
}
