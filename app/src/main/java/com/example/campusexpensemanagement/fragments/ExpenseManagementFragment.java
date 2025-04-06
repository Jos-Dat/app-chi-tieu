package com.example.campusexpensemanagement.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

        // Date pickers
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
            Object selectedCategory = spCategory.getSelectedItem();
            if (selectedCategory == null) {
                Toast.makeText(getContext(), "Please select category!", Toast.LENGTH_SHORT).show();
                return;
            }
            String category = selectedCategory.toString();
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

            // Tạo Expense đầu tiên ngay khi lưu recurring
            Expense expense = new Expense(description, amount, category, startDate.get());
            expense.setRecurring(true); // Gán recurring = true
            expense.setUserId(userId);
            long expenseId = expenseDAO.addExpense(expense);

            if (expenseId != -1) {
                // Tạo RecurringExpense
                RecurringExpense recurringExpense = new RecurringExpense((int) expenseId, frequency, startDate.get(), endDate.get());
                recurringExpenseDAO.addRecurringExpense(recurringExpense);

                // Ghi lịch sử giao dịch
                TransactionHistory transaction = new TransactionHistory(userId, amount, description, startDate.get(), "Recurring Expense");
                transactionHistoryDAO.addTransaction(transaction);

                // GỌI xử lý recurring ngay để generate bản ghi tiếp theo (nếu có)
                processRecurringExpenses();
                loadExpenses();
                Toast.makeText(getContext(), "Recurring expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
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

            long interval;
            switch (frequency) {
                case "Daily":
                    interval = 24 * 60 * 60 * 1000L; // 1 ngày
                    break;
                case "Weekly":
                    interval = 7 * 24 * 60 * 60 * 1000L; // 1 tuần
                    break;
                case "Monthly":
                    interval = 30 * 24 * 60 * 60 * 1000L; // 1 tháng (ước lượng)
                    break;
                default:
                    continue;
            }

            long nextDate = startDate;
            while (nextDate <= endDate && nextDate <= currentTime) {
                // Kiểm tra xem chi tiêu đã tồn tại chưa
                List<Expense> existing = expenseDAO.getUserExpensesByDate(userId, nextDate);
                boolean alreadyAdded = existing.stream().anyMatch(e -> e.getDescription().equals(baseExpense.getDescription()) &&
                        e.getAmount() == baseExpense.getAmount() && e.getCategory().equals(baseExpense.getCategory()));

                if (!alreadyAdded) {
                    Expense newExpense = new Expense(baseExpense.getDescription(), baseExpense.getAmount(),
                            baseExpense.getCategory(), nextDate);
                    newExpense.setUserId(userId);
                    newExpense.setRecurring(true);
                    long newExpenseId = expenseDAO.addExpense(newExpense);

                    if (newExpenseId != -1) {
                        TransactionHistory transaction = new TransactionHistory(userId, newExpense.getAmount(),
                                newExpense.getDescription(), nextDate, "Recurring Expense");
                        transactionHistoryDAO.addTransaction(transaction);
                    }
                }
                nextDate += interval;
            }
        }
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
                Toast.makeText(getContext(), "Please enter full information!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse amount
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
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
                        "Expense" // expense type
                );
                transactionHistoryDAO.addTransaction(transaction);
                checkAndNotifyBudgetExceeded(getContext(), userId, category, amount);


                // Refresh expense list
                loadExpenses();
                Toast.makeText(getContext(), "Expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error when add expense!", Toast.LENGTH_SHORT).show();
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

            // Parse amount
            float amount;
            try {
                amount = Float.parseFloat(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid amount!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), "Expense updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Error when updated expense!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

    private void checkAndNotifyBudgetExceeded(Context context, int userId, String category, float newAmount) {
        // Lấy tổng chi tiêu hiện tại của người dùng cho danh mục đó
        List<Expense> expenses = expenseDAO.getUserExpenses(userId);
        float totalSpent = 0;
        for (Expense e : expenses) {
            if (e.getCategory().equals(category)) {
                totalSpent += e.getAmount();
            }
        }

        // Lấy ngân sách đã đặt cho danh mục đó
        Budget budget = budgetDAO.getBudgetByCategory(category);
        if (budget != null && totalSpent > budget.getAmount()) {
            NotificationHelper.sendBudgetExceededNotification(context, category, totalSpent, budget.getAmount());

            String message = "Exceed the budget: \"" + category + "\" (" + totalSpent + "/" + budget.getAmount() + "đ)";

            View rootView = ((Activity) context).findViewById(android.R.id.content);
            Snackbar.make(rootView, message, Snackbar.LENGTH_LONG).setAction("OK", v -> {}).show();
        }

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