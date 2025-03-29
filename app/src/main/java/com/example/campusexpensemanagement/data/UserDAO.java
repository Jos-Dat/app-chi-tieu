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

    public  UserDAO(Context context){
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    public long AddUser(User user){
        db =dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_USER_USERNAME, user.getUsername());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD, user.getPassword());
        long results = db.insert(ExpenseDatabaseHelper.TABLE_USER, null, values);
        db.close();
        return results;
    }

    public User GetUserByUserName(String username){
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_USER,
                null, ExpenseDatabaseHelper.COLUMN_USER_USERNAME + " = ?", new String[]{username},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range")
            User user = new User(
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_USER_PASSWORD))
            );
            cursor.close();
            db.close();
            return user;
        }
        db.close();
        return null;
    }
}
