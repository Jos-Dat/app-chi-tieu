package com.example.campusexpensemanagement.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.utils.NotificationHelper;
import com.example.campusexpensemanagement.adapters.ExpenseAdapter;
import com.example.campusexpensemanagement.data.BudgetDAO;
import com.example.campusexpensemanagement.data.CategoryDAO;
import com.example.campusexpensemanagement.data.ExpenseDAO;
import com.example.campusexpensemanagement.data.RecurringExpenseDAO;
import com.example.campusexpensemanagement.data.TransactionHistoryDAO;
import com.example.campusexpensemanagement.models.Budget;
import com.example.campusexpensemanagement.models.Category;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.models.RecurringExpense;
import com.example.campusexpensemanagement.models.TransactionHistory;
import com.example.campusexpensemanagement.utils.DateTimeUtils;
import com.example.campusexpensemanagement.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ExpenseManagementFragment extends Fragment implements ExpenseAdapter.OnExpenseActionListener {

    private RecyclerView rvExpenses;
    private FloatingActionButton fabAddExpense;
    private PopupWindow fabMenuPopup;
    private ExpenseDAO expenseDAO;
    private CategoryDAO categoryDAO;
    private BudgetDAO budgetDAO;
    private TransactionHistoryDAO transactionHistoryDAO;
    private RecurringExpenseDAO recurringExpenseDAO;
    private SessionManager sessionManager;
    private List<Expense> expenseList;
    private ExpenseAdapter expenseAdapter;
    private Expense expense;
    private int userId;

    // For date picker
    private Calendar calendar, calendarStart, calendarEnd;
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

        recurringExpenseDAO = new RecurringExpenseDAO(getContext());
        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();
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
        fabAddExpense = view.findViewById(R.id.fab_main);

        FloatingActionButton fabMain = view.findViewById(R.id.fab_main);
        fabMain.setOnClickListener(v -> showFabMenu(fabMain));

        processRecurringExpenses();

        // Set up RecyclerView
        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        loadExpenses();

        budgetDAO = new BudgetDAO(requireContext());

    }

    private void showFabMenu(View anchor) {
        // Inflate layout menu
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.fab_menu_expense, null);
        fabMenuPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // Đặt background để cho phép dismiss khi nhấn ra ngoài
        fabMenuPopup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        fabMenuPopup.setOutsideTouchable(true);
        fabMenuPopup.setFocusable(true);

        // Gán sự kiện click cho từng lựa chọn
        popupView.findViewById(R.id.option_add_expense).setOnClickListener(v -> {
            showAddExpenseDialog();
            fabMenuPopup.dismiss();
        });
        popupView.findViewById(R.id.option_add_recurring_expense).setOnClickListener(v -> {
            showAddRecurringExpenseDialog();
            fabMenuPopup.dismiss();
        });

        // Đo kích thước của popup để biết chiều cao của nó
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        fabMenuPopup.showAtLocation(anchor, Gravity.NO_GRAVITY,
                location[0], location[1] - popupHeight);
    }

    private void showAddRecurringExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_recurring_expense, null);
        builder.setView(dialogView);

        EditText etDescription = dialogView.findViewById(R.id.et_recurring_description);
        EditText etAmount = dialogView.findViewById(R.id.et_recurring_amount);
        Spinner spCategory = dialogView.findViewById(R.id.sp_recurring_category);
        Spinner spFrequency = dialogView.findViewById(R.id.sp_recurring_frequency);
        Button btnPickStartDate = dialogView.findViewById(R.id.btn_pick_start_date);
        Button btnPickEndDate = dialogView.findViewById(R.id.btn_pick_end_date);
        Button btnSave = dialogView.findViewById(R.id.btn_save_recurring);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Load categories
        List<String> categoryNames = categoryDAO.getAllCategories().stream()
                .map(Category::getName).collect(Collectors.toList());
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, categoryNames);
        spCategory.setAdapter(categoryAdapter);

        // Load frequency options
        String[] frequencies = {"Daily", "Weekly", "Monthly"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, frequencies);
        spFrequency.setAdapter(frequencyAdapter);

        // Default to current date if no date is selected
        AtomicLong startDate = new AtomicLong(calendarStart.getTimeInMillis());
        AtomicLong endDate = new AtomicLong(calendarEnd.getTimeInMillis());
        btnPickStartDate.setText(DateTimeUtils.formatDate(startDate.get()));
        btnPickEndDate.setText(DateTimeUtils.formatDate(endDate.get()));

        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendarStart.set(year, month, dayOfMonth);
                        startDate.set(calendarStart.getTimeInMillis());
                        btnPickStartDate.setText(DateTimeUtils.formatDate(startDate.get()));
                    }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH),
                    calendarStart.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendarEnd.set(year, month, dayOfMonth);
                        endDate.set(calendarEnd.getTimeInMillis());
                        btnPickEndDate.setText(DateTimeUtils.formatDate(endDate.get()));
                    }, calendarEnd.get(Calendar.YEAR), calendarEnd.get(Calendar.MONTH),
                    calendarEnd.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();
            String frequency = spFrequency.getSelectedItem().toString();

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter full information!", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if budget exists for this category
            if (!budgetDAO.budgetExistsForCategory(category, userId)) {
                Toast.makeText(getContext(), "No budget exists for " + category + ". Please create a budget first.", Toast.LENGTH_LONG).show();
                return;
            }

            // Check if recurring expense already exists
            List<RecurringExpense> existingRecurringExpenses = recurringExpenseDAO.getUserRecurringExpenses(userId);
            for (RecurringExpense existingRecurring : existingRecurringExpenses) {
                Expense existingExpense = expenseDAO.getExpenseById(existingRecurring.getExpenseId());
                if (existingExpense != null &&
                        existingRecurring.getFrequency().equals(frequency) &&
                        existingExpense.getCategory().equals(category) &&
                        existingExpense.getDescription().equals(description)) {
                    Toast.makeText(getContext(), "This recurring expense already exists!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Calculate total spent in this category
            float totalSpentInCategory = calculateCategorySpending(userId, category);

            // Use checkBudgetAndNotify to check budget before adding
            if (!checkBudgetAndNotify(category, amount, totalSpentInCategory)) {
                return; // Stop if over budget
            }

            // Nếu không vượt ngân sách, thêm chi tiêu
            Expense expense = new Expense(description, amount, category, startDate.get());
            expense.setRecurring(true);
            expense.setUserId(userId);
            long expenseId = expenseDAO.addExpense(expense);

            if (expenseId != -1) {
                RecurringExpense recurringExpense = new RecurringExpense((int) expenseId, frequency, startDate.get(), endDate.get());
                recurringExpenseDAO.addRecurringExpense(recurringExpense);

                // Log transaction history
                TransactionHistory transaction = new TransactionHistory(userId, amount, description, startDate.get(), "Recurring Expense");
                transactionHistoryDAO.addTransaction(transaction);

                processRecurringExpenses();
                loadExpenses();
                Toast.makeText(getContext(), "Recurring expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error adding recurring expense!", Toast.LENGTH_SHORT).show();
            }
        });


        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void processRecurringExpenses() {
        List<RecurringExpense> recurringExpenses = recurringExpenseDAO.getUserRecurringExpenses(userId);
        long currentTime = System.currentTimeMillis();

        for (RecurringExpense recurring : recurringExpenses) {
            Expense baseExpense = expenseDAO.getExpenseById(recurring.getExpenseId());
            if (baseExpense == null) continue;

            long startDate = recurring.getStartDate();
            long endDate = recurring.getEndDate();
            String frequency = recurring.getFrequency();
            String category = baseExpense.getCategory();

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startDate);
            long nextDate = startDate;

            // Loop through the recurring period
            while (nextDate <= endDate && nextDate <= currentTime) {
                try {
                    // Check if this expense already exists for the current date
                    List<Expense> existing = expenseDAO.getUserExpensesByDate(userId, nextDate);
                    boolean alreadyAdded = false;

                    // Kiểm tra chi tiết hơn với cả ngày
                    for (Expense e : existing) {
                        if (e.getDescription().equals(baseExpense.getDescription()) &&
                                e.getAmount() == baseExpense.getAmount() &&
                                e.getCategory().equals(baseExpense.getCategory()) &&
                                e.isRecurring() &&
                                Math.abs(e.getDate() - nextDate) < 86400000) { // Trong phạm vi 24 giờ
                            alreadyAdded = true;
                            break;
                        }
                    }

                    if (!alreadyAdded) {
                        // Calculate total amount spent for the category in current period
                        float totalSpentInCategory = calculateCategorySpending(userId, category);

                        // Sử dụng checkBudgetAndNotify để kiểm tra trước khi thêm
                        // Nhưng không hiển thị Toast vì đang xử lý trong background
                        Budget budget = budgetDAO.getBudgetByCategory(category, userId);
                        if (budget != null) {
                            float budgetAmount = budget.getAmount();
                            float newTotal = totalSpentInCategory + baseExpense.getAmount();
                            float ratio = newTotal / budgetAmount;

                            Log.d("RecurringExpense", "Category: " + category +
                                    ", Current total: " + totalSpentInCategory +
                                    ", Adding: " + baseExpense.getAmount() +
                                    ", Budget: " + budgetAmount);

                            if (ratio > 1.0f) {
                                // Vượt ngân sách, gửi thông báo nhưng KHÔNG thêm chi tiêu
                                NotificationHelper.sendBudgetExceededNotification(
                                        getContext(), category, newTotal, budgetAmount);
                                // Bỏ qua chi tiêu này và đi đến chu kỳ tiếp theo
                                cal = updateCalendarByFrequency(cal, frequency);
                                nextDate = cal.getTimeInMillis();
                                continue; // Bỏ qua chi tiêu này
                            } else if (ratio >= 0.8f) {
                                // Gần vượt ngân sách, gửi cảnh báo và vẫn thêm chi tiêu
                                NotificationHelper.sendBudgetWarningNotification(
                                        getContext(), category, newTotal, budgetAmount);
                            }
                        }

                        // Add new recurring expense
                        Expense newExpense = new Expense(baseExpense.getDescription(), baseExpense.getAmount(),
                                category, nextDate);
                        newExpense.setUserId(userId);
                        newExpense.setRecurring(true);
                        long newExpenseId = expenseDAO.addExpense(newExpense);

                        if (newExpenseId != -1) {
                            // Log the transaction history
                            TransactionHistory transaction = new TransactionHistory(userId, newExpense.getAmount(),
                                    newExpense.getDescription(), nextDate, "Recurring Expense");
                            transactionHistoryDAO.addTransaction(transaction);
                        }
                    }
                } catch (Exception e) {
                    Log.e("RecurringExpense", "Error processing recurring expense: " + e.getMessage(), e);
                }

                // Update nextDate based on frequency
                cal = updateCalendarByFrequency(cal, frequency);
                nextDate = cal.getTimeInMillis();
            }
        }
    }

    // Hàm phụ giúp update calendar dựa trên frequency để code sạch hơn
    private Calendar updateCalendarByFrequency(Calendar cal, String frequency) {
        switch (frequency) {
            case "Daily":
                cal.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Weekly":
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "Monthly":
                cal.add(Calendar.MONTH, 1);
                break;
        }
        return cal;
    }

    // Hàm tính tổng chi tiêu theo danh mục (có thể thêm logic để tính theo chu kỳ)
    private float calculateCategorySpending(int userId, String category) {
        // Lấy tháng hiện tại cho ví dụ về chu kỳ tháng
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1); // Ngày đầu tháng
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startOfMonth = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1); // Tháng sau
        long endOfMonth = cal.getTimeInMillis() - 1; // Trừ 1ms để lấy cuối tháng hiện tại

        float total = 0;
        // Lấy chi tiêu theo khoảng thời gian
        List<Expense> expenses = expenseDAO.getExpensesByUserAndDateRange(userId, startOfMonth, endOfMonth);
        for (Expense expense : expenses) {
            if (expense.getCategory().equals(category)) {
                total += expense.getAmount();
            }
        }
        return total;
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

        btnSave.setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();

            if (description.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(getContext(), "Please enter full information!", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if budget exists for this category
            if (!budgetDAO.budgetExistsForCategory(category, userId)) {
                // No budget exists, show error message
                Toast.makeText(getContext(), "No budget exists for " + category + ". Please create a budget first.", Toast.LENGTH_LONG).show();
                return;
            }

            float totalSpent = 0;
            List<Expense> userExpenses = expenseDAO.getUserExpenses(userId);
            for (Expense e : userExpenses) {
                if (e.getCategory().equals(category)) {
                    totalSpent += e.getAmount();
                }
            }

            if (!checkBudgetAndNotify(category, amount, totalSpent)) {
                return; // Ngăn thêm nếu vượt ngân sách
            }

            Expense expense = new Expense(description, amount, category, selectedDate);
            expense.setUserId(userId);
            long expenseId = expenseDAO.addExpense(expense);

            if (expenseId != -1) {
                TransactionHistory transaction = new TransactionHistory(
                        userId, amount, description, selectedDate, "Expense");
                transactionHistoryDAO.addTransaction(transaction);
                loadExpenses();
                Toast.makeText(getContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error when adding expense!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Please enter full information!", Toast.LENGTH_SHORT).show();
                return;
            }

            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if budget exists for this category
            if (!budgetDAO.budgetExistsForCategory(category, userId)) {
                // No budget exists, show error message
                Toast.makeText(getContext(), "No budget exists for " + category + ". Please create a budget first.", Toast.LENGTH_LONG).show();
                return;
            }

            float totalSpent = 0;
            List<Expense> userExpenses = expenseDAO.getUserExpenses(userId);
            for (Expense e : userExpenses) {
                if (e.getCategory().equals(category) && e.getId() != expense.getId()) { // Bỏ qua chi tiêu hiện tại
                    totalSpent += e.getAmount();
                }
            }

            if (!checkBudgetAndNotify(category, amount, totalSpent)) {
                return; // Ngăn sửa nếu vượt ngân sách
            }

            expense.setDescription(description);
            expense.setAmount(amount);
            expense.setCategory(category);
            expense.setDate(selectedDate);

            boolean updated = expenseDAO.updateExpense(expense);
            if (updated) {
                loadExpenses();
                Toast.makeText(getContext(), "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error when updating expense!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean checkBudgetAndNotify(String category, float amount, float totalSpent) {
        Budget budget = budgetDAO.getBudgetByCategory(category, userId);
        if (budget != null) {
            float budgetAmount = budget.getAmount();
            float newTotal = totalSpent + amount;
            float ratio = newTotal / budgetAmount;

            if (ratio > 1.0f) {
                NotificationHelper.sendBudgetExceededNotification(requireContext(), category, newTotal, budgetAmount);
                Toast.makeText(getContext(), "Over budget! Cannot proceed.", Toast.LENGTH_LONG).show();
                return false; // Không cho thêm/sửa
            } else if (ratio >= 0.8f) {
                NotificationHelper.sendBudgetWarningNotification(requireContext(), category, newTotal, budgetAmount);
            }
        }
        return true; // Cho phép thêm/sửa
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm delete")
                .setMessage("Are you sure want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Delete expense
                    expenseDAO.deleteExpense(expense.getId());
                    // Refresh expense list
                    loadExpenses();
                    Toast.makeText(getContext(), "Expense deleted!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addDefaultCategories() {
        // Add some default categories if none exist
        categoryDAO.addCategory(new Category("Food"));
        categoryDAO.addCategory(new Category("House"));
        categoryDAO.addCategory(new Category("Transportation"));
        categoryDAO.addCategory(new Category("Education"));
        categoryDAO.addCategory(new Category("Entertainment"));
        categoryDAO.addCategory(new Category("Health"));
        categoryDAO.addCategory(new Category("Others"));
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