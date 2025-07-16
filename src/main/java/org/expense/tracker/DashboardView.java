package org.expense.tracker;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setPadding(true);
        setSpacing(true);

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        ExpenseDAO dao = new ExpenseDAO(db.getConnection());

        List<MainView.Expense> expenses = dao.getAllExpenses();
        List<Income> incomes = dao.getAllIncome();

        LocalDate now = LocalDate.now().withDayOfMonth(1);
        List<LocalDate> last12Months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            last12Months.add(now.minusMonths(i));
        }

        // Stats
        double monthlyIncome = getIncomeForMonth(incomes, now);
        double monthlyExpenses = getExpensesForMonth(expenses, now);
        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(MainView.Expense::getAmount).sum();
        double balance = totalIncome - totalExpenses;

        String topCategory = expenses.stream()
                .collect(Collectors.groupingBy(MainView.Expense::getCategory, Collectors.summingDouble(MainView.Expense::getAmount)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // Board Cards
        Board board = new Board();
        Row row1 = board.addRow(
                createStatCard("Current Balance", "€" + round(balance)),
                createStatCard("This Month's Income", "€" + round(monthlyIncome)),
                createStatCard("This Month's Expenses", "€" + round(monthlyExpenses)),
                createStatCard("Top Expense Category", topCategory)
        );

        add(new H2("Financial Overview"), board);
        add(new H2("12-Month Income/Expense/Balance Chart"), createIncomeExpenseBalanceChart(last12Months, incomes, expenses));
    }

    private VerticalLayout createStatCard(String title, String value) {
        VerticalLayout card = new VerticalLayout();
        card.add(new Span(title), new H2(value));
        card.getStyle().set("padding", "1rem").set("border", "1px solid #ccc").set("border-radius", "8px");
        return card;
    }

    private double getIncomeForMonth(List<Income> incomes, LocalDate month) {
        return incomes.stream()
                .filter(i -> i.getMonth().getMonth().equals(month.getMonth()) && i.getMonth().getYear() == month.getYear())
                .mapToDouble(Income::getAmount)
                .sum();
    }

    private double getExpensesForMonth(List<MainView.Expense> expenses, LocalDate month) {
        return expenses.stream()
                .filter(e -> {
                    if (!e.getRecurring()) {
                        return e.getDate().getMonth().equals(month.getMonth()) &&
                                e.getDate().getYear() == month.getYear();
                    }
                    return "Monthly".equalsIgnoreCase(e.getRecurrenceType()) ||
                            ("Annually".equalsIgnoreCase(e.getRecurrenceType()) &&
                                    e.getDate().getMonth().equals(month.getMonth()));
                })
                .mapToDouble(MainView.Expense::getAmount)
                .sum();
    }

    private Chart createIncomeExpenseBalanceChart(List<LocalDate> months, List<Income> incomes, List<MainView.Expense> expenses) {
        List<String> monthLabels = months.stream()
                .map(m -> m.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + m.getYear())
                .toList();

        ListSeries incomeSeries = new ListSeries("Income");
        ListSeries expenseSeries = new ListSeries("Expenses");
        ListSeries balanceSeries = new ListSeries("Balance");

        for (LocalDate month : months) {
            double income = getIncomeForMonth(incomes, month);
            double exp = getExpensesForMonth(expenses, month);
            double bal = income - exp;

            incomeSeries.addData(income);
            expenseSeries.addData(exp);
            balanceSeries.addData(bal);
        }

        Chart chart = new Chart(ChartType.COLUMN);
        chart.setSizeFull();
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Monthly Income vs Expenses vs Balance");

        XAxis xAxis = new XAxis();
        xAxis.setCategories(monthLabels.toArray(new String[0]));
        conf.addxAxis(xAxis);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Amount (€)");
        conf.addyAxis(yAxis);

        conf.addSeries(incomeSeries);
        conf.addSeries(expenseSeries);
        conf.addSeries(balanceSeries);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        conf.setTooltip(tooltip);

        return chart;
    }

    private String round(double value) {
        return String.format("%.2f", value);
    }
}
