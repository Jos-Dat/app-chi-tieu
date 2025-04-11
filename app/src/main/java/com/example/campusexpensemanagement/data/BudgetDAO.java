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
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID, budget.getUserId());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_BUDGET, null, values);
        db.close();
        return result;
    }

    // Cập nhật ngân sách
    public boolean updateBudget(Budget budget) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID, budget.getUserId());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY, budget.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT, budget.getAmount());

        int rowsAffected = db.update(
                ExpenseDatabaseHelper.TABLE_BUDGET,
                values,
                ExpenseDatabaseHelper.COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budget.getId())}
        );

        db.close();
        return rowsAffected > 0;
    }

    // Lấy tất cả ngân sách
    @SuppressLint("Range")
    public List<Budget> getAllBudgets(int userId) {
        List<Budget> budgets = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_BUDGET,
                null,
                ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    Budget budget = new Budget(
                            cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY)),
                            cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT)),
                            cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID))
                    );
                    budget.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_ID)));
                    budgets.add(budget);
                }
            } finally {
                cursor.close();
            }
        }
        db.close();
        return budgets;
    }

    // Lấy ngân sách theo danh mục và userId
    @SuppressLint("Range")
    public Budget getBudgetByCategory(String category, int userId) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_BUDGET,
                null,
                ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY + " = ? AND " +
                        ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID + " = ?",
                new String[]{category, String.valueOf(userId)},
                null, null, null
        );
        Budget budget = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    budget = new Budget(
                            cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY)),
                            cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_AMOUNT)),
                            cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID))
                    );
                    budget.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_BUDGET_ID)));
                }
            } finally {
                cursor.close();
            }
        }
        db.close();
        return budget;
    }

    // Kiểm tra xem ngân sách có tồn tại cho danh mục và userId không
    public boolean budgetExistsForCategory(String category, int userId) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_BUDGET,
                new String[]{"COUNT(*)"},
                ExpenseDatabaseHelper.COLUMN_BUDGET_CATEGORY + " = ? AND " +
                        ExpenseDatabaseHelper.COLUMN_BUDGET_USER_ID + " = ?",
                new String[]{category, String.valueOf(userId)},
                null, null, null
        );
        boolean exists = false;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    exists = cursor.getInt(0) > 0;
                }
            } finally {
                cursor.close();
            }
        }
        db.close();
        return exists;
    }

    // Xóa ngân sách theo ID
    public void deleteBudget(int budgetId) {
        db = dbHelper.getWritableDatabase();
        db.delete(
                ExpenseDatabaseHelper.TABLE_BUDGET,
                ExpenseDatabaseHelper.COLUMN_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budgetId)}
        );
        db.close();
    }
}