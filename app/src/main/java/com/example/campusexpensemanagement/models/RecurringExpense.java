package com.example.campusexpensemanagement.models;

public class RecurringExpense {
    private int id;
    private int expenseId; // lien ket voi expense
    private String frequency; // hang thang, hang nam,....
    private long startDate;
    private long endDate;

    public RecurringExpense(int expenseId, String frequency, long startDate, long endDate) {
        this.expenseId = expenseId;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
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
