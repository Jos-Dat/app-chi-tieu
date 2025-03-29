package com.example.campusexpensemanagement.models;

import com.example.campusexpensemanagement.data.ExpenseDAO;

public class ExpenseReport {
    private int id;
    private int userId;  // Liên kết với User
    private float totalExpenses;
    private float totalBudget;
    private String reportType;  // Hàng tháng, hàng năm
    private long startDate;
    private long endDate;

    public ExpenseReport(int userId, float totalExpenses, float totalBudget, String reportType, long startDate, long endDate) {
        this.userId = userId;
        this.totalExpenses = totalExpenses;
        this.totalBudget = totalBudget;
        this.reportType = reportType;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public float getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(float totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public float getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(float totalBudget) {
        this.totalBudget = totalBudget;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

}
