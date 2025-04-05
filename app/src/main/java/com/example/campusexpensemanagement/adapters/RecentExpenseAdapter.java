package com.example.campusexpensemanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecentExpenseAdapter extends RecyclerView.Adapter<RecentExpenseAdapter.RecentExpenseViewHolder> {

    private List<Expense> expenseList;
    private NumberFormat currencyFormatter;

    public RecentExpenseAdapter(List<Expense> expenseList) {
        this.expenseList = expenseList;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public RecentExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_expense, parent, false);
        return new RecentExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvExpenseDescription.setText(expense.getDescription());
        holder.tvExpenseAmount.setText(currencyFormatter.format(expense.getAmount()));
        holder.tvExpenseCategory.setText(expense.getCategory());
        holder.tvExpenseDate.setText(DateTimeUtils.getRelativeTimeSpan(expense.getDate()));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newExpenses) {
        this.expenseList = newExpenses;
        notifyDataSetChanged();
    }

    static class RecentExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseDescription, tvExpenseAmount, tvExpenseCategory, tvExpenseDate;

        RecentExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tv_expense_description);
            tvExpenseAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvExpenseCategory = itemView.findViewById(R.id.tv_expense_category);
            tvExpenseDate = itemView.findViewById(R.id.tv_expense_date);
        }
    }
}