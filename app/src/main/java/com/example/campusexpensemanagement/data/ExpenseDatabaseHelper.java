package com.example.campusexpensemanagement.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ExpenseDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusExpenseManagement.db";
    private static final int DATABASE_VERSION = 3;

    // ten bang va cac cot
    public static  final String TABLE_EXPENSE = "expenses";
    public static final String TABLE_CATEGORY = "categories";
    public static final String TABLE_BUDGET = "budgets";
    public static final String TABLE_USER = "users";
    public static final String TABLE_EXPENSE_NOTIFICATION = "expense_notifications";
    public static final String TABLE_RECURRING_EXPENSE = "recurring_expense";
    public static final String TABLE_EXPENSE_REPORT = "expense_report";
    public static final String TABLE_TRANSACTION_HISTORY = "transaction_history";

    // cac cot cua bang expense
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_USER_ID = "userId";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "description";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CATEGORY = "category";
    public static final String COLUMN_EXPENSE_DATE = "date";
    public static final String COLUMN_EXPENSE_IS_RECURRING = "is_recurring";

    // cac cot cua bang category
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";

    // cac cot cua bang budget
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_BUDGET_CATEGORY = "category";
    public static final String COLUMN_BUDGET_AMOUNT = "amount";
    public static final String COLUMN_BUDGET_USER_ID = "user_id";

    // cac vot cua bang user
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // cac cot cua bang expense_notification
    public static final String COLUMN_NOTIFICATION_ID = "id";
    public static final String COLUMN_NOTIFICATION_USER_ID = "user_id";
    public static final String COLUMN_NOTIFICATION_MESSAGE = "message";
    public static final String COLUMN_NOTIFICATION_SENT = "is_sent";
    public static final String COLUMN_NOTIFICATION_DATE = "date";

    // cac cot cua bang recurring expense
    public static final String COLUMN_RECURRING_EXPENSE_ID = "id";
    public static final String COLUMN_RECURRING_EXPENSE_EXPENSE_ID = "expense_id";
    public static final String COLUMN_RECURRING_EXPENSE_FREQUENCY = "frequency";
    public static final String COLUMN_RECURRING_EXPENSE_START_DATE = "start_date";
    public static final String COLUMN_RECURRING_EXPENSE_END_DATE = "end_date";

    // cac cot cua bang expense report
    public static final String COLUMN_REPORT_ID = "id";
    public static final String COLUMN_REPORT_USER_ID = "user_id";
    public static final String COLUMN_REPORT_TOTAL_EXPENSES = "total_expenses";
    public static final String COLUMN_REPORT_TOTAL_BUDGET = "total_budget";
    public static final String COLUMN_REPORT_TYPE = "report_type";
    public static final String COLUMN_REPORT_START_DATE = "start_date";
    public static final String COLUMN_REPORT_END_DATE = "end_date";

    // cac cot cua bang transaction history
    public static final String COLUMN_TRANSACTION_ID = "id";
    public static final String COLUMN_TRANSACTION_USER_ID = "user_id";
    public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
    public static final String COLUMN_TRANSACTION_DATE = "date";
    public static final String COLUMN_TRANSACTION_TYPE = "type";  // chi tieu hoac thu nhap

    public ExpenseDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key support
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Tạo bảng users (nên tạo trước vì các bảng khác phụ thuộc vào nó)
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_USER_EMAIL + " TEXT NOT NULL UNIQUE, " +
                COLUMN_USER_PASSWORD + " TEXT NOT NULL);";
        db.execSQL(CREATE_USER_TABLE);

        // Tạo bảng categories
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT NOT NULL UNIQUE);";
        db.execSQL(CREATE_CATEGORY_TABLE);

        // Tạo bảng expenses với khoá ngoại tới users và categories
        String CREATE_EXPENSE_TABLE = "CREATE TABLE " + TABLE_EXPENSE + " (" +
                COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXPENSE_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_EXPENSE_DESCRIPTION + " TEXT, " +
                COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                COLUMN_EXPENSE_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_EXPENSE_IS_RECURRING + " INTEGER DEFAULT 0, " +
                COLUMN_EXPENSE_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_EXPENSE_USER_ID + ") REFERENCES " +
                TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_EXPENSE_CATEGORY + ") REFERENCES " +
                TABLE_CATEGORY + "(" + COLUMN_CATEGORY_NAME + ") ON DELETE RESTRICT);";
        db.execSQL(CREATE_EXPENSE_TABLE);

        // Tạo bảng budgets với khoá ngoại tới categories và users
        String CREATE_BUDGET_TABLE = "CREATE TABLE " + TABLE_BUDGET + " (" +
                COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_BUDGET_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_BUDGET_AMOUNT + " REAL NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_BUDGET_CATEGORY + ") REFERENCES " +
                TABLE_CATEGORY + "(" + COLUMN_CATEGORY_NAME + ") ON DELETE CASCADE, " +
                "FOREIGN KEY (" + COLUMN_BUDGET_USER_ID + ") REFERENCES " +
                TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_BUDGET_TABLE);

        // Tạo bảng expense_notifications với khoá ngoại tới users
        String CREATE_NOTIFICATION_TABLE = "CREATE TABLE " + TABLE_EXPENSE_NOTIFICATION + " (" +
                COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIFICATION_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_NOTIFICATION_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_NOTIFICATION_SENT + " INTEGER NOT NULL, " +
                COLUMN_NOTIFICATION_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_NOTIFICATION_USER_ID + ") REFERENCES " +
                TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_NOTIFICATION_TABLE);

        // Tạo bảng recurring_expenses với khoá ngoại tới expenses
        String CREATE_RECURRING_EXPENSE_TABLE = "CREATE TABLE " + TABLE_RECURRING_EXPENSE + " (" +
                COLUMN_RECURRING_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECURRING_EXPENSE_EXPENSE_ID + " INTEGER NOT NULL, " +
                COLUMN_RECURRING_EXPENSE_FREQUENCY + " TEXT NOT NULL, " +
                COLUMN_RECURRING_EXPENSE_START_DATE + " INTEGER NOT NULL, " +
                COLUMN_RECURRING_EXPENSE_END_DATE + " INTEGER, " +
                "FOREIGN KEY (" + COLUMN_RECURRING_EXPENSE_EXPENSE_ID + ") REFERENCES " +
                TABLE_EXPENSE + "(" + COLUMN_EXPENSE_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_RECURRING_EXPENSE_TABLE);

        // Tạo bảng expense_reports với khoá ngoại tới users
        String CREATE_REPORT_TABLE = "CREATE TABLE " + TABLE_EXPENSE_REPORT + " (" +
                COLUMN_REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_REPORT_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_REPORT_TOTAL_EXPENSES + " REAL NOT NULL, " +
                COLUMN_REPORT_TOTAL_BUDGET + " REAL NOT NULL, " +
                COLUMN_REPORT_TYPE + " TEXT NOT NULL, " +
                COLUMN_REPORT_START_DATE + " INTEGER NOT NULL, " +
                COLUMN_REPORT_END_DATE + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_REPORT_USER_ID + ") REFERENCES " +
                TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_REPORT_TABLE);

        // Tạo bảng transaction_history với khoá ngoại tới users
        String CREATE_TRANSACTION_HISTORY_TABLE = "CREATE TABLE " + TABLE_TRANSACTION_HISTORY + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_USER_ID + " INTEGER NOT NULL, " +
                COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL, " +
                COLUMN_TRANSACTION_DESCRIPTION + " TEXT, " +
                COLUMN_TRANSACTION_DATE + " INTEGER NOT NULL, " +
                COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_TRANSACTION_USER_ID + ") REFERENCES " +
                TABLE_USER + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE);";
        db.execSQL(CREATE_TRANSACTION_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables in reverse order of dependencies to avoid foreign key constraint issues
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }
}