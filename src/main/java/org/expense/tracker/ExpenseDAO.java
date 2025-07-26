package org.expense.tracker;

import org.expense.tracker.BankBalance;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    private final Connection connection;

    public ExpenseDAO(Connection connection) {
        this.connection = connection;
        createBankBalanceTableIfNotExists();
    }

    private void createBankBalanceTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS bank_balance (
                id SERIAL PRIMARY KEY,
                amount DECIMAL(10,2) NOT NULL,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int saveExpense(MainView.Expense expense) {
        String sql = "INSERT INTO expenses (amount, category, description, date, recurring, recurrence_type) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, expense.getAmount());
            stmt.setString(2, expense.getCategory());
            stmt.setString(3, expense.getDescription());
            stmt.setDate(4, Date.valueOf(expense.getDate()));
            stmt.setBoolean(5, expense.getRecurring());
            stmt.setString(6, expense.getRecurrenceType());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateExpense(MainView.Expense expense) {
        String sql = "UPDATE expenses SET amount = ?, category = ?, description = ?, date = ?, recurring = ?, recurrence_type = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, expense.getAmount());
            stmt.setString(2, expense.getCategory());
            stmt.setString(3, expense.getDescription());
            stmt.setDate(4, Date.valueOf(expense.getDate()));
            stmt.setBoolean(5, expense.getRecurring());
            stmt.setString(6, expense.getRecurrenceType());
            stmt.setInt(7, expense.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteExpense(int id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MainView.Expense> getAllExpenses() {
        List<MainView.Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses ORDER BY date DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String recurrenceType = rs.getString("recurrence_type");
                boolean isRecurring = recurrenceType != null && !recurrenceType.equalsIgnoreCase("None");

                MainView.Expense expense = new MainView.Expense(
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getDate("date").toLocalDate(),
                        isRecurring,
                        recurrenceType
                );
                expense.setId(rs.getInt("id"));
                expenses.add(expense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }

    public void saveIncome(Income income) {
        String sql = "INSERT INTO income (amount, month) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, income.getAmount());
            stmt.setDate(2, Date.valueOf(income.getMonth()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Income getIncomeForMonth(LocalDate month) {
        String sql = "SELECT * FROM income WHERE month = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(month.withDayOfMonth(1)));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Income income = new Income();
                income.setId(rs.getInt("id"));
                income.setAmount(rs.getDouble("amount"));
                income.setMonth(rs.getDate("month").toLocalDate());
                return income;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Income> getAllIncome() {
        List<Income> incomeList = new ArrayList<>();
        String sql = "SELECT * FROM income ORDER BY month";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Income income = new Income();
                income.setId(rs.getInt("id"));
                income.setAmount(rs.getDouble("amount"));
                income.setMonth(rs.getDate("month").toLocalDate());
                incomeList.add(income);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return incomeList;
    }

    public void saveOrUpdateIncome(Income income) {
        String checkSql = "SELECT id FROM income WHERE month = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setDate(1, Date.valueOf(income.getMonth()));
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String updateSql = "UPDATE income SET amount = ? WHERE month = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, income.getAmount());
                    updateStmt.setDate(2, Date.valueOf(income.getMonth()));
                    updateStmt.executeUpdate();
                }
            } else {
                saveIncome(income);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Bank Balance methods
    public void saveOrUpdateBankBalance(BankBalance bankBalance) {
        String checkSql = "SELECT COUNT(*) FROM bank_balance";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                String updateSql = "UPDATE bank_balance SET amount = ?, last_updated = ? WHERE id = (SELECT MIN(id) FROM bank_balance)";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, bankBalance.getAmount());
                    updateStmt.setTimestamp(2, Timestamp.valueOf(bankBalance.getLastUpdated()));
                    updateStmt.executeUpdate();
                }
            } else {
                String insertSql = "INSERT INTO bank_balance (amount, last_updated) VALUES (?, ?)";
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    insertStmt.setDouble(1, bankBalance.getAmount());
                    insertStmt.setTimestamp(2, Timestamp.valueOf(bankBalance.getLastUpdated()));
                    insertStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BankBalance getCurrentBankBalance() {
        String sql = "SELECT * FROM bank_balance ORDER BY last_updated DESC LIMIT 1";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                BankBalance bankBalance = new BankBalance();
                bankBalance.setId(rs.getInt("id"));
                bankBalance.setAmount(rs.getDouble("amount"));
                bankBalance.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                return bankBalance;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteIncome(int id) {
        String sql = "DELETE FROM income WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}