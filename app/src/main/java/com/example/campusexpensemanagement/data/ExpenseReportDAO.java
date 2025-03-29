package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.models.ExpenseReport;

import java.util.ArrayList;
import java.util.List;

public class ExpenseReportDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public ExpenseReportDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm báo cáo chi tiêu
    public long addExpenseReport(ExpenseReport expenseReport) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_USER_ID, expenseReport.getUserId());
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_TOTAL_EXPENSES, expenseReport.getTotalExpenses());
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_TOTAL_BUDGET, expenseReport.getTotalBudget());
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_TYPE, expenseReport.getReportType());
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_START_DATE, expenseReport.getStartDate());
        values.put(ExpenseDatabaseHelper.COLUMN_REPORT_END_DATE, expenseReport.getEndDate());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_EXPENSE_REPORT, null, values);
        db.close();
        return result;
    }

    // Lấy tất cả báo cáo chi tiêu của người dùng
    public Cursor getAllExpenseReports(int userId) {
        db = dbHelper.getReadableDatabase();
        return db.query(ExpenseDatabaseHelper.TABLE_EXPENSE_REPORT, null,
                ExpenseDatabaseHelper.COLUMN_REPORT_USER_ID + " = ?", new String[]{String.valueOf(userId)},
                null, null, ExpenseDatabaseHelper.COLUMN_REPORT_START_DATE + " DESC");
    }

    // Xóa báo cáo chi tiêu theo ID
    public void deleteExpenseReport(int reportId) {
        db = dbHelper.getWritableDatabase();
        db.delete(ExpenseDatabaseHelper.TABLE_EXPENSE_REPORT,
                ExpenseDatabaseHelper.COLUMN_REPORT_ID + " = ?", new String[]{String.valueOf(reportId)});
        db.close();
    }

    @SuppressLint("Range")
    public List<Expense> getAllExpensesByUserAndDateRange(int userId, long startDate, long endDate) {
        List<Expense> expenses = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_EXPENSE,
                null,
                ExpenseDatabaseHelper.COLUMN_EXPENSE_USER_ID + " = ? AND " +
                        ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " BETWEEN ? AND ?",
                new String[]{String.valueOf(userId), String.valueOf(startDate), String.valueOf(endDate)},
                null, null, ExpenseDatabaseHelper.COLUMN_EXPENSE_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
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
