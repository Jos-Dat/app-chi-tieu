package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.User;

public class UserDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public UserDAO(Context context){
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm người dùng
    public long addUser(User user){
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_USER_USERNAME, user.getUsername());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD, user.getPassword());

        long result = db.insert(ExpenseDatabaseHelper.TABLE_USER, null, values);
        db.close();
        return result;
    }

    // Cập nhật thông tin người dùng
    public boolean updateUser(User user) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_USER_USERNAME, user.getUsername());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD, user.getPassword());

        int rowsAffected = db.update(
                ExpenseDatabaseHelper.TABLE_USER,
                values,
                ExpenseDatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );

        db.close();
        return rowsAffected > 0;
    }

    // Lấy người dùng theo tên đăng nhập
    @SuppressLint("Range")
    public User getUserByUsername(String username){
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_USER,
                null,
                ExpenseDatabaseHelper.COLUMN_USER_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD))
            );

            user.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_ID)));
            cursor.close();
        }

        db.close();
        return user;
    }

    // Lấy người dùng theo ID
    @SuppressLint("Range")
    public User getUserById(int userId) {
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_USER,
                null,
                ExpenseDatabaseHelper.COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD))
            );

            user.setId(userId);
            cursor.close();
        }

        db.close();
        return user;
    }

    // Kiểm tra người dùng tồn tại
    public boolean checkUser(String username) {
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_USER,
                new String[]{ExpenseDatabaseHelper.COLUMN_USER_ID},
                ExpenseDatabaseHelper.COLUMN_USER_USERNAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return exists;
    }

    // Xác thực đăng nhập
    @SuppressLint("Range")
    public User authenticateUser(String username, String password) {
        db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                ExpenseDatabaseHelper.TABLE_USER,
                null,
                ExpenseDatabaseHelper.COLUMN_USER_USERNAME + " = ? AND " +
                        ExpenseDatabaseHelper.COLUMN_USER_PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD))
            );

            user.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_ID)));
            cursor.close();
        }

        db.close();
        return user;
    }
}