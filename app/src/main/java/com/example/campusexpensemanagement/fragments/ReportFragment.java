package com.example.campusexpensemanagement.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.campusexpensemanagement.R;
import com.example.campusexpensemanagement.data.ExpenseDAO;
import com.example.campusexpensemanagement.data.ExpenseReportDAO;
import com.example.campusexpensemanagement.models.Expense;
import com.example.campusexpensemanagement.models.ExpenseReport;
import com.example.campusexpensemanagement.utils.DateTimeUtils;
import com.example.campusexpensemanagement.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {

    private TextView tvTotalExpense, tvReportPeriod;
    private Button btnStartDate, btnEndDate, btnGenerateReport, btnSaveReport;
    private PieChart pieChartCategory;
    private BarChart barChartDaily;

    private Calendar startDateCalendar, endDateCalendar;
    private long startDate, endDate;

    private ExpenseDAO expenseDAO;
    private ExpenseReportDAO reportDAO;
    private SessionManager sessionManager;
    private int userId;

    private List<Expense> expenseList;
    private float totalBudget = 0; // This would typically come from your budget data

    public ReportFragment() {
        // Required empty public constructor
    }

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        expenseDAO = new ExpenseDAO(getContext());
        reportDAO = new ExpenseReportDAO(getContext());
        sessionManager = new SessionManager(getContext());
        userId = sessionManager.getUserId();

        // Initialize date range to current month
        startDateCalendar = Calendar.getInstance();
        startDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);

        endDateCalendar = Calendar.getInstance();

        startDate = startDateCalendar.getTimeInMillis();
        endDate = endDateCalendar.getTimeInMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvReportPeriod = view.findViewById(R.id.tv_report_period);
        btnStartDate = view.findViewById(R.id.btn_start_date);
        btnEndDate = view.findViewById(R.id.btn_end_date);
        btnGenerateReport = view.findViewById(R.id.btn_generate_report);
        btnSaveReport = view.findViewById(R.id.btn_save_report);
        pieChartCategory = view.findViewById(R.id.pie_chart_category);
        barChartDaily = view.findViewById(R.id.bar_chart_daily);

        // Set up initial dates
        updateDateButtons();

        // Set up date picker buttons
        btnStartDate.setOnClickListener(v -> showStartDatePicker());
        btnEndDate.setOnClickListener(v -> showEndDatePicker());

        // Set up generate and save buttons
        btnGenerateReport.setOnClickListener(v -> generateReport());
        btnSaveReport.setOnClickListener(v -> saveReport());

        // Generate report with initial date range
        generateReport();
    }

    private void updateDateButtons() {
        btnStartDate.setText(DateTimeUtils.formatDate(startDate));
        btnEndDate.setText(DateTimeUtils.formatDate(endDate));

        tvReportPeriod.setText(String.format("Report from %s to %s",
                DateTimeUtils.formatDate(startDate),
                DateTimeUtils.formatDate(endDate)));
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    startDateCalendar.set(year, month, dayOfMonth);
                    startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startDateCalendar.set(Calendar.MINUTE, 0);
                    startDateCalendar.set(Calendar.SECOND, 0);

                    startDate = startDateCalendar.getTimeInMillis();

                    // Ensure start date is before end date
                    if (startDate > endDate) {
                        Toast.makeText(getContext(), "The start date must be before the end!", Toast.LENGTH_SHORT).show();
                        startDate = endDate;
                        startDateCalendar.setTimeInMillis(endDate);
                    }

                    updateDateButtons();
                },
                startDateCalendar.get(Calendar.YEAR),
                startDateCalendar.get(Calendar.MONTH),
                startDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    endDateCalendar.set(year, month, dayOfMonth);
                    endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    endDateCalendar.set(Calendar.MINUTE, 59);
                    endDateCalendar.set(Calendar.SECOND, 59);

                    endDate = endDateCalendar.getTimeInMillis();

                    // Ensure end date is after start date
                    if (endDate < startDate) {
                        Toast.makeText(getContext(), "The end date must be after the beginning date!", Toast.LENGTH_SHORT).show();
                        endDate = startDate;
                        endDateCalendar.setTimeInMillis(startDate);
                    }

                    updateDateButtons();
                },
                endDateCalendar.get(Calendar.YEAR),
                endDateCalendar.get(Calendar.MONTH),
                endDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void generateReport() {
        // Get expenses for the selected date range
        expenseList = reportDAO.getAllExpensesByUserAndDateRange(userId, startDate, endDate);

        // Calculate total expense
        float totalExpense = 0;
        for (Expense expense : expenseList) {
            totalExpense += expense.getAmount();
        }

        // Format and display total expense
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalExpense.setText(currencyFormatter.format(totalExpense));

        // Generate charts
        generateCategoryPieChart();
        generateDailyBarChart();
    }

    private void generateCategoryPieChart() {
        // Calculate expenses by category
        Map<String, Float> categoryExpenses = new HashMap<>();

        for (Expense expense : expenseList) {
            String category = expense.getCategory();
            float amount = expense.getAmount();

            if (categoryExpenses.containsKey(category)) {
                categoryExpenses.put(category, categoryExpenses.get(category) + amount);
            } else {
                categoryExpenses.put(category, amount);
            }
        }

        // Create pie chart entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryExpenses.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        // Create and configure pie chart data set
        PieDataSet dataSet = new PieDataSet(entries, "Expense according to the category");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        // Create and configure pie chart data
        PieData data = new PieData(dataSet);

        // Configure pie chart
        pieChartCategory.setData(data);
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.setCenterText("Expense according to the category");
        pieChartCategory.setCenterTextSize(16f);
        pieChartCategory.setHoleRadius(40f);
        pieChartCategory.setTransparentCircleRadius(45f);
        pieChartCategory.animateY(1000);
        pieChartCategory.invalidate();
    }

    private void generateDailyBarChart() {
        // Calculate expenses by day
        Map<String, Float> dailyExpenses = new HashMap<>();

        for (Expense expense : expenseList) {
            String day = DateTimeUtils.formatDateShort(expense.getDate());
            float amount = expense.getAmount();

            if (dailyExpenses.containsKey(day)) {
                dailyExpenses.put(day, dailyExpenses.get(day) + amount);
            } else {
                dailyExpenses.put(day, amount);
            }
        }

        // Create bar chart entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int index = 0;
        for (Map.Entry<String, Float> entry : dailyExpenses.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        // Create and configure bar chart data set
        BarDataSet dataSet = new BarDataSet(entries, "Expense by day");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        // Create and configure bar chart data
        BarData data = new BarData(dataSet);

        // Configure bar chart
        barChartDaily.setData(data);
        barChartDaily.getDescription().setEnabled(false);
        barChartDaily.animateY(1000);

        // Configure X axis
        XAxis xAxis = barChartDaily.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(45);

        barChartDaily.invalidate();
    }

    private void saveReport() {
        // Calculate total expense
        float totalExpense = 0;
        for (Expense expense : expenseList) {
            totalExpense += expense.getAmount();
        }

        // Determine report type (monthly/yearly/custom)
        String reportType = determineReportType(startDate, endDate);

        // Create and save report
        ExpenseReport report = new ExpenseReport(
                userId,
                totalExpense,
                totalBudget,
                reportType,
                startDate,
                endDate
        );

        long reportId = reportDAO.addExpenseReport(report);

        if (reportId != -1) {
            Toast.makeText(getContext(), "Report saved successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Error when save report!", Toast.LENGTH_SHORT).show();
        }
    }

    private String determineReportType(long start, long end) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(start);

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis(end);

        // Check if it's a monthly report
        if (startCal.get(Calendar.DAY_OF_MONTH) == 1 &&
                endCal.get(Calendar.MONTH) == startCal.get(Calendar.MONTH) &&
                endCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) &&
                endCal.get(Calendar.DAY_OF_MONTH) == endCal.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            return "Monthly";
        }

        // Check if it's a yearly report
        if (startCal.get(Calendar.DAY_OF_YEAR) == 1 &&
                endCal.get(Calendar.YEAR) == startCal.get(Calendar.YEAR) &&
                endCal.get(Calendar.DAY_OF_YEAR) == endCal.getActualMaximum(Calendar.DAY_OF_YEAR)) {
            return "Yearly";
        }

        // Otherwise it's a custom report
        return "Custom";
    }
}