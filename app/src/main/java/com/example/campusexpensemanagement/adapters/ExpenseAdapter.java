package com.example.campusexpensemanagement.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenseList;
    private OnExpenseActionListener listener;
    private NumberFormat currencyFormatter;

    public interface OnExpenseActionListener {
        void onEditExpense(Expense expense);
        void onDeleteExpense(Expense expense);
    }

    public ExpenseAdapter(List<Expense> expenseList, OnExpenseActionListener listener) {
        this.expenseList = expenseList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenseList.get(position);

        holder.tvExpenseDescription.setText(expense.getDescription());
        holder.tvExpenseAmount.setText(currencyFormatter.format(expense.getAmount()));
        holder.tvExpenseCategory.setText(expense.getCategory());
        holder.tvExpenseDate.setText(DateTimeUtils.formatDate(expense.getDate()));

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditExpense(expense);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteExpense(expense);
            }
        });

        // Đánh dấu chi tiêu định kỳ một cách trực quan
        if (expense.isRecurring()) {
            // Ví dụ: Thêm icon hoặc thay đổi màu sắc
            holder.tvRecurring.setVisibility(View.VISIBLE);
            holder.tvRecurring.setText("Định kỳ");
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvRecurring.setVisibility(View.GONE);
        }
        Log.d("DEBUG_EXPENSE", "desc=" + expense.getDescription() + " | isRecurring=" + expense.isRecurring());

    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    public void updateData(List<Expense> newExpenses) {
        this.expenseList = newExpenses;
        notifyDataSetChanged();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvExpenseDescription, tvExpenseAmount, tvExpenseCategory, tvExpenseDate, tvRecurring;
        ImageButton btnEdit, btnDelete;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExpenseDescription = itemView.findViewById(R.id.tv_expense_description);
            tvExpenseAmount = itemView.findViewById(R.id.tv_expense_amount);
            tvExpenseCategory = itemView.findViewById(R.id.tv_expense_category);
            tvExpenseDate = itemView.findViewById(R.id.tv_expense_date);
            btnEdit = itemView.findViewById(R.id.btn_edit_expense);
            btnDelete = itemView.findViewById(R.id.btn_delete_expense);
            tvRecurring = itemView.findViewById(R.id.tv_recurring);
        }
    }
}