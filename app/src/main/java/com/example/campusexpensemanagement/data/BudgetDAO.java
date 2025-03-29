package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.Budget;

import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public BudgetDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm ngân sách
    public long addBudget(Budget budget) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_BUDGET, null, values);
        db.close();
        return result;
    }

    // Cập nhật ngân sách
    public int updateBudget(Budget budget) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
        int rowsAffected = db.update(ExpenseDatabaseHelper.TABLE_BUDGET, values,
                ExpenseDatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{String.valueOf(budget.getId())});
        db.close();
        return rowsAffected;
    }

    // Lấy tất cả ngân sách
    @SuppressLint("Range")
    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_BUDGET,
                null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Budget budget = new Budget(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT))
                );
                budget.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_ID)));
                budgets.add(budget);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return budgets;
    }

    // Lấy ngân sách theo danh mục
    @SuppressLint("Range")
    public Budget getBudgetByCategory(String category) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_BUDGET,
                null, ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY + " = ?", new String[]{category},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Budget budget = new Budget(
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY)),
                    cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT))
            );
            budget.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_ID)));
            cursor.close();
            db.close();
            return budget;
        }
        db.close();
        return null;
    }

    // Xóa ngân sách theo ID
    public void deleteBudget(int budgetId) {
        db = dbHelper.getWritableDatabase();
        db.delete(ExpenseDatabaseHelper.TABLE_BUDGET,
                ExpenseDatabaseHelper.COLUMN_BUDGET_ID + " = ?", new String[]{String.valueOf(budgetId)});
        db.close();
    }
}
