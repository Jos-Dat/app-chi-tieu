package com.example.campusexpensemanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.models.ExpenseReport;
import com.example.campusexpensemanagement.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ExpenseReportAdapter extends RecyclerView.Adapter<ExpenseReportAdapter.ReportViewHolder> {

    private List<ExpenseReport> reportList;
    private OnReportActionListener listener;
    private NumberFormat currencyFormatter;

    public interface OnReportActionListener {
        void onViewReport(ExpenseReport report);
        void onDeleteReport(ExpenseReport report);
    }

    public ExpenseReportAdapter(List<ExpenseReport> reportList, OnReportActionListener listener) {
        this.reportList = reportList;
        this.listener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ExpenseReport report = reportList.get(position);

        holder.tvReportType.setText(report.getReportType());
        holder.tvReportPeriod.setText(String.format("%s - %s",
                DateTimeUtils.formatDate(report.getStartDate()),
                DateTimeUtils.formatDate(report.getEndDate())));
        holder.tvTotalExpense.setText(currencyFormatter.format(report.getTotalExpenses()));

        // Set up click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewReport(report);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteReport(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void updateData(List<ExpenseReport> newReports) {
        this.reportList = newReports;
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvReportType, tvReportPeriod, tvTotalExpense;
        ImageButton btnDelete;

        ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReportType = itemView.findViewById(R.id.tv_report_type);
            tvReportPeriod = itemView.findViewById(R.id.tv_report_period);
            tvTotalExpense = itemView.findViewById(R.id.tv_total_expense);
            btnDelete = itemView.findViewById(R.id.btn_delete_report);
        }
    }
}