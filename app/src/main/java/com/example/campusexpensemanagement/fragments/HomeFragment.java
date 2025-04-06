package com.example.campusexpensemanagement.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.adapters.RecentExpenseAdapter;
import com.example.campusexpensemanagement.data.BudgetDAO;
import com.example.campusexpensemanagement.data.ExpenseDAO;
import com.example.campusexpensemanagement.models.Budget;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvTotalBudget, tvRemainingBudget, tvSpentAmount, tvBudgetUsage;
    private LinearProgressIndicator budgetProgressBar;
    private RecyclerView rvRecentExpenses;
    private MaterialCardView cardAddExpense;

    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;
    private SessionManager sessionManager;
    private int userId;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expenseDAO = new ExpenseDAO(getContext());
        budgetDAO = new BudgetDAO(getContext());
        sessionManager = new SessionManager(getContext());
        userId = sessionManager.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvRemainingBudget = view.findViewById(R.id.tv_remaining_budget);
        tvSpentAmount = view.findViewById(R.id.tv_spent_amount);
        tvBudgetUsage = view.findViewById(R.id.tv_budget_usage);
        budgetProgressBar = view.findViewById(R.id.budget_progress);
        rvRecentExpenses = view.findViewById(R.id.rv_recent_expenses);
        cardAddExpense = view.findViewById(R.id.card_add_expense);

        // Set up RecyclerView
        rvRecentExpenses.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sử dụng getParentFragmentManager() thay vì getActivity().getSupportFragmentManager()
        cardAddExpense.setOnClickListener(v -> {
            ExpenseManagementFragment expenseFragment = new ExpenseManagementFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, expenseFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Load and display data
        updateDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboard();
    }

    @SuppressLint("DefaultLocale")
    private void updateDashboard() {
        // Get current month's data
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long startOfMonth = calendar.getTimeInMillis();

        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfMonth = calendar.getTimeInMillis();

        // Get this month's expenses
        List<Expense> monthlyExpenses = expenseDAO.getExpensesByUserAndDateRange(userId, startOfMonth, endOfMonth);

        // Calculate total spent
        float totalSpent = 0;
        for (Expense expense : monthlyExpenses) {
            totalSpent += expense.getAmount();
        }

        // Get total budget
        List<Budget> budgets = budgetDAO.getAllBudgets();
        float totalBudget = 0;
        for (Budget budget : budgets) {
            totalBudget += budget.getAmount();
        }

        // Calculate remaining budget and usage percentage
        float remainingBudget = totalBudget - totalSpent;
        int usagePercentage = totalBudget > 0 ? (int) ((totalSpent / totalBudget) * 100) : 0;

        // Format currency
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Update UI
        tvTotalBudget.setText(currencyFormatter.format(totalBudget));
        tvRemainingBudget.setText(currencyFormatter.format(remainingBudget));
        tvSpentAmount.setText(currencyFormatter.format(totalSpent));
        tvBudgetUsage.setText(String.format("%d%%", usagePercentage));

        // Update progress bar
        budgetProgressBar.setProgress(usagePercentage);

        // Sử dụng ContextCompat.getColor() thay vì getResources().getColor()
        if (usagePercentage > 100) {
            budgetProgressBar.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.red));
        } else if (usagePercentage > 80) {
            budgetProgressBar.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.orange));
        } else {
            budgetProgressBar.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.green));
        }

        // Show recent expenses (last 5)
        List<Expense> recentExpenses = expenseDAO.getRecentExpenses(userId, 5);
        RecentExpenseAdapter adapter = new RecentExpenseAdapter(recentExpenses);
        rvRecentExpenses.setAdapter(adapter);
    }
}
