package com.example.campusexpensemanagement.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.activities.LoginActivity;
import com.example.campusexpensemanagement.adapters.ExpenseReportAdapter;
import com.example.campusexpensemanagement.data.ExpenseReportDAO;
import com.example.campusexpensemanagement.data.UserDAO;
import com.example.campusexpensemanagement.models.ExpenseReport;
import com.example.campusexpensemanagement.models.User;
import com.example.campusexpensemanagement.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class UserProfileFragment extends Fragment implements ExpenseReportAdapter.OnReportActionListener {

    private TextView tvUsername, tvEmail;
    private Button btnChangePassword, btnLogout;
    private RecyclerView rvSavedReports;

    private UserDAO userDAO;
    private ExpenseReportDAO reportDAO;
    private SessionManager sessionManager;

    private int userId;
    private User currentUser;
    private List<ExpenseReport> reportList;
    private ExpenseReportAdapter reportAdapter;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDAO = new UserDAO(getContext());
        reportDAO = new ExpenseReportDAO(getContext());
        sessionManager = new SessionManager(getContext());

        userId = sessionManager.getUserId();
        currentUser = userDAO.getUserById(userId);
        reportList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        tvUsername = view.findViewById(R.id.tv_username);
        tvEmail = view.findViewById(R.id.tv_email);
        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);
        rvSavedReports = view.findViewById(R.id.rv_saved_reports);

        // Set up user information
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            tvEmail.setText(currentUser.getEmail());
        }

        // Set up RecyclerView
        rvSavedReports.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up button click listeners
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnLogout.setOnClickListener(v -> logout());

        // Load saved reports
        loadSavedReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSavedReports(); // Refresh data when fragment resumes
    }

    private void loadSavedReports() {
        // Get saved reports from database
        Cursor reportCursor = reportDAO.getAllExpenseReports(userId);
        reportList = convertCursorToReportList(reportCursor);

        // Create and set adapter
        reportAdapter = new ExpenseReportAdapter(reportList, this);
        rvSavedReports.setAdapter(reportAdapter);
    }

    private List<ExpenseReport> convertCursorToReportList(Cursor cursor) {
        List<ExpenseReport> reports = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                float totalExpenses = cursor.getFloat(cursor.getColumnIndexOrThrow("total_expenses"));
                float totalBudget = cursor.getFloat(cursor.getColumnIndexOrThrow("total_budget"));
                String reportType = cursor.getString(cursor.getColumnIndexOrThrow("report_type"));
                long startDate = cursor.getLong(cursor.getColumnIndexOrThrow("start_date"));
                long endDate = cursor.getLong(cursor.getColumnIndexOrThrow("end_date"));

                ExpenseReport report = new ExpenseReport(userId, totalExpenses, totalBudget, reportType, startDate, endDate);
                report.setId(id);
                reports.add(report);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return reports;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        // Initialize dialog components
        final EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        final EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        final EditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
        final Button btnSave = dialogView.findViewById(R.id.btn_save_password);
        final Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Create and show dialog
        final AlertDialog dialog = builder.create();

        // Set up button click listeners
        btnSave.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Validate input
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check current password
            if (!currentPassword.equals(currentUser.getPassword())) {
                Toast.makeText(getContext(), "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password
            currentUser.setPassword(newPassword);
            boolean updated = userDAO.updateUser(currentUser);

            if (updated) {
                Toast.makeText(getContext(), "Đã cập nhật mật khẩu thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Có lỗi xảy ra khi cập nhật mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void logout() {
        // Clear session
        sessionManager.clearSession();

        // Redirect to login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void showReportDetailsDialog(ExpenseReport report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_report_details, null);
        builder.setView(dialogView);

        // Initialize dialog components
        final TextView tvReportType = dialogView.findViewById(R.id.tv_report_type);
        final TextView tvReportPeriod = dialogView.findViewById(R.id.tv_report_period);
        final TextView tvTotalExpense = dialogView.findViewById(R.id.tv_total_expense);
        final TextView tvTotalBudget = dialogView.findViewById(R.id.tv_total_budget);
        final TextView tvBalance = dialogView.findViewById(R.id.tv_balance);
        final Button btnClose = dialogView.findViewById(R.id.btn_close);

        // Set report data
        tvReportType.setText(report.getReportType());
        tvReportPeriod.setText(String.format("Từ %s đến %s",
                formatDate(report.getStartDate()),
                formatDate(report.getEndDate())));
        tvTotalExpense.setText(String.format("%.2f đ", report.getTotalExpenses()));
        tvTotalBudget.setText(String.format("%.2f đ", report.getTotalBudget()));

        float balance = report.getTotalBudget() - report.getTotalExpenses();
        tvBalance.setText(String.format("%.2f đ", balance));

        // Set text color for balance
        if (balance < 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.red));
        } else {
            tvBalance.setTextColor(getResources().getColor(R.color.green));
        }

        // Create and show dialog
        final AlertDialog dialog = builder.create();

        // Set up button click listener
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteReportConfirmationDialog(ExpenseReport report) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa báo cáo này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Delete report
                    reportDAO.deleteExpenseReport(report.getId());
                    // Refresh report list
                    loadSavedReports();
                    Toast.makeText(getContext(), "Đã xóa báo cáo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String formatDate(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    @Override
    public void onViewReport(ExpenseReport report) {
        showReportDetailsDialog(report);
    }

    @Override
    public void onDeleteReport(ExpenseReport report) {
        showDeleteReportConfirmationDialog(report);
    }
}