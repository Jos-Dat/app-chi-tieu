package com.example.campusexpensemanagement.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class ExpenseDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusExpenseManagement.db";
    private static final int DATABASE_VERSION = 1;

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

    // cac cot cua bang category
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_NAME = "name";

    // cac cot cua bang budget
    public static final String COLUMN_BUDGET_ID = "id";
    public static final String COLUMN_BUDGET_CATEGORY = "category";
    public static final String COLUMN_BUDGET_AMOUNT = "amount";

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
        // Tạo bảng expenses
        String CREATE_EXPENSE_TABLE = "CREATE TABLE " + TABLE_EXPENSE + " (" +
                COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EXPENSE_USER_ID + " INTEGER, " +
                COLUMN_EXPENSE_DESCRIPTION + " TEXT, " +
                COLUMN_EXPENSE_AMOUNT + " REAL, " +
                COLUMN_EXPENSE_CATEGORY + " TEXT, " +
                COLUMN_EXPENSE_DATE + " INTEGER);";
        db.execSQL(CREATE_EXPENSE_TABLE);

        // Tạo bảng categories
        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CATEGORY_NAME + " TEXT);";
        db.execSQL(CREATE_CATEGORY_TABLE);

        // Tạo bảng budgets
        String CREATE_BUDGET_TABLE = "CREATE TABLE " + TABLE_BUDGET + " (" +
                COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_BUDGET_CATEGORY + " TEXT, " +
                COLUMN_BUDGET_AMOUNT + " REAL);";
        db.execSQL(CREATE_BUDGET_TABLE);

        // tao bang user
        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_USERNAME + " TEXT, " +
                COLUMN_USER_EMAIL + " TEXT, " +
                COLUMN_USER_PASSWORD + " TEXT);";
        db.execSQL(CREATE_USER_TABLE);

        // Tạo bảng expense_notifications
        String CREATE_NOTIFICATION_TABLE = "CREATE TABLE " + TABLE_EXPENSE_NOTIFICATION + " (" +
                COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTIFICATION_USER_ID + " INTEGER, " +
                COLUMN_NOTIFICATION_MESSAGE + " TEXT, " +
                COLUMN_NOTIFICATION_SENT + " INTEGER, " +
                COLUMN_NOTIFICATION_DATE + " INTEGER);";
        db.execSQL(CREATE_NOTIFICATION_TABLE);

        // Tạo bảng recurring_expenses
        String CREATE_RECURRING_EXPENSE_TABLE = "CREATE TABLE " + TABLE_RECURRING_EXPENSE + " (" +
                COLUMN_RECURRING_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_RECURRING_EXPENSE_EXPENSE_ID + " INTEGER, " +
                COLUMN_RECURRING_EXPENSE_FREQUENCY + " TEXT, " +
                COLUMN_RECURRING_EXPENSE_START_DATE + " INTEGER, " +
                COLUMN_RECURRING_EXPENSE_END_DATE + " INTEGER);";
        db.execSQL(CREATE_RECURRING_EXPENSE_TABLE);

        // Tạo bảng expense_reports
        String CREATE_REPORT_TABLE = "CREATE TABLE " + TABLE_EXPENSE_REPORT + " (" +
                COLUMN_REPORT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_REPORT_USER_ID + " INTEGER, " +
                COLUMN_REPORT_TOTAL_EXPENSES + " REAL, " +
                COLUMN_REPORT_TOTAL_BUDGET + " REAL, " +
                COLUMN_REPORT_TYPE + " TEXT, " +
                COLUMN_REPORT_START_DATE + " INTEGER, " +
                COLUMN_REPORT_END_DATE + " INTEGER);";
        db.execSQL(CREATE_REPORT_TABLE);

        // Tạo bảng transaction_history
        String CREATE_TRANSACTION_HISTORY_TABLE = "CREATE TABLE " + TABLE_TRANSACTION_HISTORY + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSACTION_USER_ID + " INTEGER, " +
                COLUMN_TRANSACTION_AMOUNT + " REAL, " +
                COLUMN_TRANSACTION_DESCRIPTION + " TEXT, " +
                COLUMN_TRANSACTION_DATE + " INTEGER, " +
                COLUMN_TRANSACTION_TYPE + " TEXT);";
        db.execSQL(CREATE_TRANSACTION_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_NOTIFICATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECURRING_EXPENSE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION_HISTORY);
        onCreate(db);
    }
}
