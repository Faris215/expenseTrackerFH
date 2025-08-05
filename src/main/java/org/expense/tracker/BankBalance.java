package org.expense.tracker;

import java.time.LocalDateTime;

public class BankBalance {
    private int id;
    private double amount;
    private LocalDateTime lastUpdated;

    public BankBalance() {}

    public BankBalance(double amount) {
        this.amount = amount;
        this.lastUpdated = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}