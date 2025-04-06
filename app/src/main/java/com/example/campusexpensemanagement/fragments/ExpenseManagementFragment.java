package com.example.campusexpensemanagement.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.adapters.ExpenseAdapter;
import com.example.campusexpensemanagement.data.CategoryDAO;
import com.example.campusexpensemanagement.data.ExpenseDAO;
import com.example.campusexpensemanagement.data.TransactionHistoryDAO;
import com.example.campusexpensemanagement.models.Category;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.models.TransactionHistory;
import com.example.campusexpensemanagement.utils.DateTimeUtils;
import com.example.campusexpensemanagement.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseManagementFragment extends Fragment implements ExpenseAdapter.OnExpenseActionListener {

    private RecyclerView rvExpenses;
    private FloatingActionButton fabAddExpense;

    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private TransactionHistoryDAO transactionHistoryDAO;
    private SessionManager sessionManager;

    private List<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private int userId;

    // For date picker
    private Calendar calendar;
    private long selectedDate;

    public ExpenseManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        expenseDAO = new ExpenseDAO(getContext());
        categoryDAO = new CategoryDAO(getContext());
        transactionHistoryDAO = new TransactionHistoryDAO(getContext());
        sessionManager = new SessionManager(getContext());
        userId = sessionManager.getUserId();

        calendar = Calendar.getInstance();
        selectedDate = calendar.getTimeInMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expense_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        rvExpenses = view.findViewById(R.id.rv_expenses);
        fabAddExpense = view.findViewById(R.id.fab_add_expense);

        // Set up RecyclerView
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        loadExpenses();

        // Set up FAB click listener
        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());
    }

    private void loadExpenses() {
        expenseList = expenseDAO.getUserExpenses(userId);
        expenseAdapter = new ExpenseAdapter(expenseList, this);
        rvExpenses.setAdapter(expenseAdapter);
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        // Initialize dialog components
        final EditText etDescription = dialogView.findViewById(R.id.et_expense_description);
        final EditText etAmount = dialogView.findViewById(R.id.et_expense_amount);
        final Spinner spCategory = dialogView.findViewById(R.id.sp_expense_category);
        final Button btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        final Button btnSave = dialogView.findViewById(R.id.btn_save_expense);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Set current date on date button
        btnPickDate.setText(DateTimeUtils.formatDate(selectedDate));

        // Set up date picker
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate = calendar.getTimeInMillis();
                        btnPickDate.setText(DateTimeUtils.formatDate(selectedDate));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Load categories for spinner
        List<Category> categories = categoryDAO.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        // If no categories exist, add some defaults
        if (categoryNames.isEmpty()) {
            addDefaultCategories();
            categories = categoryDAO.getAllCategories();
            for (Category category : categories) {
                categoryNames.add(category.getName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryNames
        );
        spCategory.setAdapter(adapter);

        // Create and show dialog
        final AlertDialog dialog = builder.create();

        // Set up button click listeners
        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse amount
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create and save expense
            Expense expense = new Expense(description, amount, category, selectedDate);
            expense.setUserId(userId); // Thêm dòng này để thiết lập userId
            long expenseId = expenseDAO.addExpense(expense);

            if (expenseId != -1) {
                // Also add to transaction history
                TransactionHistory transaction = new TransactionHistory(
                        userId,
                        amount,
                        description,
                        selectedDate,
                        "chi tiêu" // expense type
                );
                transactionHistoryDAO.addTransaction(transaction);

                // Refresh expense list
                loadExpenses();
                Toast.makeText(getContext(), "Đã thêm chi tiêu thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Có lỗi xảy ra khi thêm chi tiêu", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showEditExpenseDialog(Expense expense) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_expense, null);
        builder.setView(dialogView);

        // Initialize dialog components
        final EditText etDescription = dialogView.findViewById(R.id.et_expense_description);
        final EditText etAmount = dialogView.findViewById(R.id.et_expense_amount);
        final Spinner spCategory = dialogView.findViewById(R.id.sp_expense_category);
        final Button btnPickDate = dialogView.findViewById(R.id.btn_pick_date);
        final Button btnSave = dialogView.findViewById(R.id.btn_save_expense);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Set existing expense data
        etDescription.setText(expense.getDescription());
        etAmount.setText(String.valueOf(expense.getAmount()));
        selectedDate = expense.getDate();
        btnPickDate.setText(DateTimeUtils.formatDate(selectedDate));

        // Set up date picker
        btnPickDate.setOnClickListener(v -> {
            calendar.setTimeInMillis(selectedDate);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        selectedDate = calendar.getTimeInMillis();
                        btnPickDate.setText(DateTimeUtils.formatDate(selectedDate));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Load categories for spinner
        List<Category> categories = categoryDAO.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categoryNames
        );
        spCategory.setAdapter(adapter);

        // Select the current category
        int categoryIndex = categoryNames.indexOf(expense.getCategory());
        if (categoryIndex != -1) {
            spCategory.setSelection(categoryIndex);
        }

        // Create and show dialog
        final AlertDialog dialog = builder.create();

        // Set up button click listeners
        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse amount
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update expense
            expense.setDescription(description);
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDate(selectedDate);

            boolean updated = expenseDAO.updateExpense(expense);

            if (updated) {
                // Refresh expense list
                loadExpenses();
                Toast.makeText(getContext(), "Đã cập nhật chi tiêu thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Có lỗi xảy ra khi cập nhật chi tiêu", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa khoản chi tiêu này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Delete expense
                    expenseDAO.deleteExpense(expense.getId());
                    // Refresh expense list
                    loadExpenses();
                    Toast.makeText(getContext(), "Đã xóa chi tiêu", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addDefaultCategories() {
        // Add some default categories if none exist
        categoryDAO.addCategory(new Category("Thực phẩm"));
        categoryDAO.addCategory(new Category("Vận chuyển"));
        categoryDAO.addCategory(new Category("Học tập"));
        categoryDAO.addCategory(new Category("Giải trí"));
        categoryDAO.addCategory(new Category("Sức khỏe"));
        categoryDAO.addCategory(new Category("Khác"));
    }

    @Override
    public void onEditExpense(Expense expense) {
        showEditExpenseDialog(expense);
    }

    @Override
    public void onDeleteExpense(Expense expense) {
        showDeleteConfirmationDialog(expense);
    }
}