package org.vaadin.example;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "compare", layout = MainLayout.class)
@PageTitle("Income vs Expenses")
public class IncomeExpenseChartView extends VerticalLayout {

    public IncomeExpenseChartView() {
        setSizeFull();
        setPadding(true);

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        ExpenseDAO dao = new ExpenseDAO(db.getConnection());

        List<MainView.Expense> allExpenses = dao.getAllExpenses();

        // Group expenses by month
        Map<String, Double> expensesByMonth = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDate().withDayOfMonth(1).toString(),
                        Collectors.summingDouble(MainView.Expense::getAmount)
                ));

        // Get income from DB
        Map<String, Double> incomeByMonth = dao.getAllIncome().stream()
                .collect(Collectors.toMap(
                        i -> i.getMonth().withDayOfMonth(1).toString(),
                        Income::getAmount
                ));

        Set<String> allMonths = new TreeSet<>(incomeByMonth.keySet());
        allMonths.addAll(expensesByMonth.keySet());

        List<String> categories = allMonths.stream()
                .sorted()
                .map(month -> LocalDate.parse(month).getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                .toList();

        ListSeries incomeSeries = new ListSeries("Income");
        ListSeries expenseSeries = new ListSeries("Expenses");

        for (String month : allMonths) {
            incomeSeries.addData(incomeByMonth.getOrDefault(month, 0.0));
            expenseSeries.addData(expensesByMonth.getOrDefault(month, 0.0));
        }

        Chart chart = new Chart(ChartType.COLUMN);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Income vs Expenses by Month");

        XAxis x = new XAxis();
        x.setCategories(categories.toArray(new String[0]));
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Amount (€)");
        conf.addyAxis(y);

        conf.addSeries(incomeSeries);
        conf.addSeries(expenseSeries);

        chart.getConfiguration().getChart().setStyledMode(true);

        add(chart);
    }
}
