package org.expense.tracker;

import java.time.LocalDate;

public class Income {
    private int id;
    private double amount;
    private LocalDate month;

    public Income() {}

    public Income(double amount, LocalDate month) {
        this.amount = amount;
        this.month = month;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getMonth() { return month; }
    public void setMonth(LocalDate month) { this.month = month; }
}
