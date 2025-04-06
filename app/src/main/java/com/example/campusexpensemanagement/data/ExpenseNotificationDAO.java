package com.example.campusexpensemanagement.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.ExpenseNotification;

public class ExpenseNotificationDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public ExpenseNotificationDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm thông báo chi tiêu
    public long addNotification(ExpenseNotification notification) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_NOTIFICATION_USER_ID, notification.getUserId());
        values.put(ExpenseDatabaseHelper.COLUMN_NOTIFICATION_MESSAGE, notification.getMessage());
        values.put(ExpenseDatabaseHelper.COLUMN_NOTIFICATION_SENT, notification.isSent() ? 1 : 0);
        values.put(ExpenseDatabaseHelper.COLUMN_NOTIFICATION_DATE, notification.getDate());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_EXPENSE_NOTIFICATION, null, values);
        db.close();
        return result;
    }

}
