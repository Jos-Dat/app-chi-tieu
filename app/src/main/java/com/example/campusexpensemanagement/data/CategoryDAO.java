package com.example.campusexpensemanagement.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.campusexpensemanagement.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private SQLiteDatabase db;
    private ExpenseDatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new ExpenseDatabaseHelper(context);
    }

    // Thêm danh mục
    public long addCategory(Category category) {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ExpenseDatabaseHelper.COLUMN_CATEGORY_NAME, category.getName());
        long result = db.insert(ExpenseDatabaseHelper.TABLE_CATEGORY, null, values);
        db.close();
        return result;
    }

    // Lấy tất cả danh mục
    @SuppressLint("Range")
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ExpenseDatabaseHelper.TABLE_CATEGORY,
                null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range")
                Category category = new Category(cursor.getString(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_CATEGORY_NAME)));
                category.setId(cursor.getInt(cursor.getColumnIndex(ExpenseDatabaseHelper.COLUMN_CATEGORY_ID)));
                categories.add(category);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return categories;
    }

    public void deleteCategory(int categoryId) {
        db = dbHelper.getWritableDatabase();
        db.delete(ExpenseDatabaseHelper.TABLE_CATEGORY,
                ExpenseDatabaseHelper.COLUMN_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        db.close();
    }
}
