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

    public ExpenseDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm chi tiêu
    public long addExpense(Expense expense) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE, expense.getDate());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID, expense.getUserId());
        values.put("is_recurring", expense.isRecurring() ? 1 : 0); // Lưu trạng thái recurring

        long result = db.insert(ExpenseDatabaseHelper.TABLE_EXPENSE, null, values);
        db.close();
        return result;
    }

    // Cập nhật chi tiêu
    public boolean updateExpense(Expense expense) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION, expense.getDescription());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY, expense.getCategory());
        values.put(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE, expense.getDate());

        int rowsAffected = db.update(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                values,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );

        db.close();
        return rowsAffected > 0;
    }

    // Xóa chi tiêu
    public boolean deleteExpense(int expenseId) {
        db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_ID + " = ?",
                new String[]{String.valueOf(expenseId)}
        );

        db.close();
        return rowsAffected > 0;
    }

    // Lấy tất cả chi tiêu
    @SuppressLint("Range")
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                null,
                null,
                null,
                null,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE))
                );

                expense.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_ID)));
                expense.setUserId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID)));

                int isRecurring = cursor.getInt(cursor.getColumnIndex("is_recurring"));
                expense.setRecurring(isRecurring == 1);

                expenses.add(expense);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return expenses;
    }

    public Expense getExpenseById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("expenses", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Expense expense = null;
        if (cursor.moveToFirst()) {
            expense = new Expense(
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getFloat(cursor.getColumnIndexOrThrow("amount")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    cursor.getLong(cursor.getColumnIndexOrThrow("date"))
            );
            // Gán lại id cho expense
            expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        }
        cursor.close();
        db.close();
        return expense;
    }

    @SuppressLint("Range")
    public List<Expense> getUserExpensesByDate(int userId, long date) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Sửa lại thành:
        Cursor cursor = db.query("expenses", null, "userId = ? AND date = ?",
                new String[]{String.valueOf(userId), String.valueOf(date)}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getFloat(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("date"))
                );
                // Gán lại id cho expense
                expense.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));

                int isRecurring = cursor.getInt(cursor.getColumnIndex("is_recurring"));
                expense.setRecurring(isRecurring == 1);

                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return expenses;
    }


    // Lấy chi tiêu theo người dùng
    @SuppressLint("Range")
    public List<Expense> getUserExpenses(int userId) {
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE))
                );

                expense.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_ID)));
                expense.setUserId(userId);

                int isRecurring = cursor.getInt(cursor.getColumnIndex("is_recurring"));
                expense.setRecurring(isRecurring == 1);

                expenses.add(expense);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return expenses;
    }

    // Add this method to ExpenseDAO.java
    public boolean hasExpensesForCategory(String category) {
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                new String[]{"COUNT(*)"},
                ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY + " = ?",
                new String[]{category},
                null, null, null);
        boolean hasExpenses = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasExpenses = cursor.getInt(0) > 0;
            cursor.close();
        }
        db.close();
        return hasExpenses;
    }

    // Lấy chi tiêu theo người dùng và khoảng thời gian
    @SuppressLint("Range")
    public List<Expense> getExpensesByUserAndDateRange(int userId, long startDate, long endDate) {
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ? AND " +
                        ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " BETWEEN ? AND ?",
                new String[]{
                        String.valueOf(userId),
                        String.valueOf(startDate),
                        String.valueOf(endDate)
                },
                null,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE))
                );

                expense.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_ID)));
                expense.setUserId(userId);

                expenses.add(expense);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return expenses;
    }

    // Lấy các chi tiêu gần đây
    @SuppressLint("Range")
    public List<Expense> getRecentExpenses(int userId, int limit) {
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_EXPENSE,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC",
                String.valueOf(limit)
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DESCRIPTION)),
                        cursor.getFloat(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_AMOUNT)),
                        cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_CATEGORY)),
                        cursor.getLong(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE))
                );

                expense.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_EXPENSE_ID)));
                expense.setUserId(userId);

                expenses.add(expense);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return expenses;
    }
}