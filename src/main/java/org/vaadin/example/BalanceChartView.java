package org.vaadin.example;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Route(value = "balance", layout = MainLayout.class)
@PageTitle("Running Balance")
public class BalanceChartView extends VerticalLayout {

    public BalanceChartView() {
        setSizeFull();
        setPadding(true);

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        ExpenseDAO dao = new ExpenseDAO(db.getConnection());

        List<MainView.Expense> allExpenses = dao.getAllExpenses();
        List<Income> allIncome = dao.getAllIncome();

        // Group income and expenses by month
        TreeSet<LocalDate> months = new TreeSet<>();

        Map<LocalDate, Double> incomeMap = new HashMap<>();
        for (Income income : allIncome) {
            LocalDate month = income.getMonth().withDayOfMonth(1);
            months.add(month);
            incomeMap.put(month, incomeMap.getOrDefault(month, 0.0) + income.getAmount());
        }

        Map<LocalDate, Double> expenseMap = new HashMap<>();
        for (MainView.Expense expense : allExpenses) {
            LocalDate month = expense.getDate().withDayOfMonth(1);
            months.add(month);
            expenseMap.put(month, expenseMap.getOrDefault(month, 0.0) + expense.getAmount());
        }

        // Build cumulative balance
        List<String> monthLabels = new ArrayList<>();
        List<Double> balanceData = new ArrayList<>();
        double runningBalance = 0.0;

        for (LocalDate month : months) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            runningBalance += income - expense;

            monthLabels.add(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear());
            balanceData.add(runningBalance);
        }

        // Create chart
        Chart chart = new Chart(ChartType.LINE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Running Balance Over Time");

        XAxis x = new XAxis();
        x.setCategories(monthLabels.toArray(new String[0]));
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Balance (€)");
        conf.addyAxis(y);

        ListSeries series = new ListSeries("Balance", balanceData.toArray(new Number[0]));
        conf.addSeries(series);

        Tooltip tooltip = new Tooltip();
        tooltip.setShared(true);
        conf.setTooltip(tooltip);

        chart.setSizeFull();
        add(chart);
    }
}
