package com.example.campusexpensemanagement.models;

public class ExpenseNotification {
    private int id;
    private int userId;
    private String message;
    private boolean isSent;
    private long date;

    public ExpenseNotification(int userId, String message, boolean isSent, long date) {
        this.userId = userId;
        this.message = message;
        this.isSent = isSent;
        this.date = date;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
