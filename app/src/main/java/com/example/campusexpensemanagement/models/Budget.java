package com.example.campusexpensemanagement.models;

public class Budget {
    private int id;
    private int userId;
    private String category;
    private float amount;

    public Budget(String category, float amount, int userId) {
        this.category = category;
        this.amount = amount;
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
