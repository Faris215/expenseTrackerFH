package org.vaadin.example;

import java.util.List;

public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService() {
        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        this.expenseDAO = new ExpenseDAO(db.getConnection());
    }

    public void addExpense(MainView.Expense expense) {
        int id = expenseDAO.saveExpense(expense);
        expense.setId(id);
    }

    public void updateExpense(MainView.Expense expense) {
        expenseDAO.updateExpense(expense);
    }

    public void deleteExpense(int id) {
        expenseDAO.deleteExpense(id);
    }

    public List<MainView.Expense> getAllExpenses() {
        return expenseDAO.getAllExpenses();
    }
}
