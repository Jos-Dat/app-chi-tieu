package com.example.campusexpensemanagement.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.data.ExpenseDAO;
import com.example.campusexpensemanagement.databinding.FragmentBudgetManagementBinding;
import com.example.campusexpensemanagement.adapters.BudgetAdapter;
import com.example.campusexpensemanagement.data.BudgetDAO;
import com.example.campusexpensemanagement.data.CategoryDAO;
import com.example.campusexpensemanagement.models.Budget;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.utils.SessionManager;

import android.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BudgetManagementFragment extends Fragment implements BudgetAdapter.OnBudgetActionListener {

    private FragmentBudgetManagementBinding binding;
    private BudgetDAO budgetDAO;
    private CategoryDAO categoryDAO;
    private SessionManager sessionManager;
    private BudgetAdapter budgetAdapter;
    private List<Budget> budgetList;
    private Map<String, Float> categoryExpenses;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        budgetDAO = new BudgetDAO(requireContext());
        categoryDAO = new CategoryDAO(requireContext());
        sessionManager = new SessionManager(requireContext());
        categoryExpenses = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetManagementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        binding.rvBudgets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));

        // Setup FAB
        binding.fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        // Load budgets
        loadBudgets();
    }

    private void loadBudgets() {
        budgetList = budgetDAO.getAllBudgets();
        calculateCategoryExpenses();

        if (budgetAdapter == null) {
            budgetAdapter = new BudgetAdapter(budgetList, categoryExpenses, this);
            binding.rvBudgets.setAdapter(budgetAdapter);
        } else {
            budgetAdapter.updateData(budgetList, categoryExpenses);
        }

        // Show empty state if no budgets
        if (budgetList.isEmpty()) {
            binding.emptyState.setVisibility(View.VISIBLE);
        } else {
            binding.emptyState.setVisibility(View.GONE);
        }
    }
    private void printAllExpensesInDatabase() {
        ExpenseDAO expenseDAO = new ExpenseDAO(requireContext());
        List<Expense> allExpenses = expenseDAO.getAllExpenses();

        Log.d("DatabaseDebug", "Total Expenses in Database: " + allExpenses.size());
        for (Expense expense : allExpenses) {
            Log.d("DatabaseDebug", "Expense: " +
                    "ID=" + expense.getId() +
                    ", UserID=" + expense.getUserId() +
                    ", Category=" + expense.getCategory() +
                    ", Amount=" + expense.getAmount());
        }
    }
    private void calculateCategoryExpenses() {
        categoryExpenses.clear();

        int currentUserId = sessionManager.getUserId();
        Log.d("BudgetDebug", "Current User ID: " + currentUserId);

        ExpenseDAO expenseDAO = new ExpenseDAO(requireContext());
        List<Expense> userExpenses = expenseDAO.getUserExpenses(currentUserId);

        Log.d("BudgetDebug", "Total User Expenses: " + userExpenses.size());

        for (Expense expense : userExpenses) {
            // Chuẩn hóa tên category
            String category = expense.getCategory().trim().toLowerCase();
            float expenseAmount = expense.getAmount();

            Log.d("BudgetDebug", "Expense: Category=" + category + ", Amount=" + expenseAmount);

            // Tổng hợp chi tiêu theo category
            if (categoryExpenses.containsKey(category)) {
                categoryExpenses.put(category, categoryExpenses.get(category) + expenseAmount);
            } else {
                categoryExpenses.put(category, expenseAmount);
            }
        }

        // Log kết quả cuối cùng
        Log.d("BudgetDebug", "Final Category Expenses: " + categoryExpenses);
    }


    private List<Expense> getAllExpensesFromDatabase() {
        // Lấy danh sách tất cả chi tiêu từ cơ sở dữ liệu (ví dụ từ ExpenseDAO)
        ExpenseDAO expenseDAO = new ExpenseDAO(requireContext());
        return expenseDAO.getAllExpenses(); // Phương thức này sẽ lấy tất cả chi tiêu
    }


    private void showAddBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_budget, null);
        builder.setView(dialogView);

        // Get references to dialog views
        android.widget.Spinner spCategory = dialogView.findViewById(R.id.sp_budget_category);
        android.widget.EditText etAmount = dialogView.findViewById(R.id.et_budget_amount);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save_budget);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Load categories
        List<String> categories = getCategoryNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        // Setup buttons
        btnSave.setOnClickListener(v -> {
            String category = spCategory.getSelectedItem().toString();
            String amountStr = etAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float amount = Float.parseFloat(amountStr);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if budget for this category already exists
                Budget existingBudget = findBudgetByCategory(category);
                if (existingBudget != null) {
                    // Update existing budget
                    existingBudget.setAmount(amount);
                    budgetDAO.updateBudget(existingBudget);
                    Toast.makeText(requireContext(), "Cập nhật ngân sách thành công", Toast.LENGTH_SHORT).show();
                } else {
                    // Add new budget
                    Budget newBudget = new Budget(category, amount);
                    long id = budgetDAO.addBudget(newBudget);
                    if (id > 0) {
                        Toast.makeText(requireContext(), "Thêm ngân sách thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Thêm ngân sách thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                dialog.dismiss();
                loadBudgets();

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private List<String> getCategoryNames() {
        List<String> categoryNames = new ArrayList<>();
        List<com.example.campusexpensemanagement.models.Category> categories = categoryDAO.getAllCategories();

        if (categories.isEmpty()) {
            // Add default categories if none exist
            addDefaultCategories();
            categories = categoryDAO.getAllCategories();
        }

        for (com.example.campusexpensemanagement.models.Category category : categories) {
            categoryNames.add(category.getName());
        }

        return categoryNames;
    }

    private Budget findBudgetByCategory(String category) {
        for (Budget budget : budgetList) {
            if (budget.getCategory().equals(category)) {
                return budget;
            }
        }
        return null;
    }

    private void addDefaultCategories() {
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Thực phẩm"));
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Vận chuyển"));
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Học tập"));
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Giải trí"));
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Sức khoẻ"));
        categoryDAO.addCategory(new com.example.campusexpensemanagement.models.Category("Khác"));
    }

    @Override
    public void onEditBudget(Budget budget) {
        showEditBudgetDialog(budget);
    }

    @Override
    public void onDeleteBudget(Budget budget) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa ngân sách cho " + budget.getCategory() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    budgetDAO.deleteBudget(budget.getId());
                    loadBudgets();
                    Toast.makeText(requireContext(), "Đã xóa ngân sách", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditBudgetDialog(Budget budget) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);

        // Get references to dialog views
        android.widget.TextView tvCategory = dialogView.findViewById(R.id.tv_budget_category);
        android.widget.EditText etAmount = dialogView.findViewById(R.id.et_budget_amount);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btn_save_budget);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Set current values
        tvCategory.setText(budget.getCategory());
        etAmount.setText(String.valueOf(budget.getAmount()));

        AlertDialog dialog = builder.create();

        // Setup buttons
        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                float amount = Float.parseFloat(amountStr);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                budget.setAmount(amount);
                boolean success = budgetDAO.updateBudget(budget);
                if (success) {
                    Toast.makeText(requireContext(), "Cập nhật ngân sách thành công", Toast.LENGTH_SHORT).show();
                    loadBudgets();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Cập nhật ngân sách thất bại", Toast.LENGTH_SHORT).show();
                }

            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}