package com.example.campusexpensemanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.models.Budget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgetList;
    private Map<String, Float> categoryExpenses;
    private OnBudgetActionListener listener;
    private NumberFormat currencyFormatter;

    public interface OnBudgetActionListener {
        void onEditBudget(Budget budget);
        void onDeleteBudget(Budget budget);
    }

    public BudgetAdapter(List<Budget> budgetList, Map<String, Float> categoryExpenses, OnBudgetActionListener listener) {
        this.budgetList = budgetList;
        this.categoryExpenses = categoryExpenses;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        String category = budget.getCategory();
        float budgetAmount = budget.getAmount();

        // Get the expense amount for this category (if any)
        float expenseAmount = categoryExpenses.containsKey(category) ? categoryExpenses.get(category) : 0;

        // Calculate the remaining budget and usage percentage
        float remainingBudget = budgetAmount - expenseAmount;
        int usagePercentage = budgetAmount > 0 ? (int) ((expenseAmount / budgetAmount) * 100) : 0;

        // Format currency
        String formattedBudget = currencyFormatter.format(budgetAmount);
        String formattedExpense = currencyFormatter.format(expenseAmount);
        String formattedRemaining = currencyFormatter.format(remainingBudget);

        // Update the UI
        holder.tvBudgetCategory.setText(category);
        holder.tvBudgetAmount.setText(formattedBudget);
        holder.tvExpenseAmount.setText(formattedExpense);
        holder.tvRemainingBudget.setText(formattedRemaining);
        holder.pbBudgetUsage.setProgress(usagePercentage);
        holder.tvBudgetUsage.setText(String.format("%d%%", usagePercentage));

        // Change colors based on budget usage
        if (usagePercentage > 100) {
            // Over budget
            holder.tvRemainingBudget.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            holder.pbBudgetUsage.setProgressDrawable(holder.itemView.getContext().getResources().getDrawable(R.drawable.progress_bar_red));
        } else if (usagePercentage > 80) {
            // Close to budget limit
            holder.tvRemainingBudget.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.orange));
            holder.pbBudgetUsage.setProgressDrawable(holder.itemView.getContext().getResources().getDrawable(R.drawable.progress_bar_orange));
        } else {
            // Normal
            holder.tvRemainingBudget.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
            holder.pbBudgetUsage.setProgressDrawable(holder.itemView.getContext().getResources().getDrawable(R.drawable.progress_bar_green));
        }

        // Set up button click listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditBudget(budget);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteBudget(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public void updateData(List<Budget> newBudgets, Map<String, Float> newCategoryExpenses) {
        this.budgetList = newBudgets;
        this.categoryExpenses = newCategoryExpenses;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvBudgetCategory, tvBudgetAmount, tvExpenseAmount, tvRemainingBudget, tvBudgetUsage;
        ProgressBar pbBudgetUsage;
        ImageButton btnEdit, btnDelete;

        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetCategory = itemView.findViewById(R.id.tv_budget_category);
            tvBudgetAmount = itemView.findViewById(R.id.tv_budget_amount);
            tvExpenseAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvRemainingBudget = itemView.findViewById(R.id.tv_remaining_budget);
            tvBudgetUsage = itemView.findViewById(R.id.tv_budget_usage);
            pbBudgetUsage = itemView.findViewById(R.id.pb_budget_usage);
            btnEdit = itemView.findViewById(R.id.btn_edit_budget);
            btnDelete = itemView.findViewById(R.id.btn_delete_budget);
        }
    }
}