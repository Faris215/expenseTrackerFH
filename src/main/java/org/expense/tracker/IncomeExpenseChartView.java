package org.expense.tracker;

import com.vaadin.flow.component.combobox.ComboBox;
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

    private final Chart chart;
    private final Configuration conf;
    private final ComboBox<String> filterComboBox;

    private final DBConnect db;
    private final ExpenseDAO dao;

    private final List<MainView.Expense> allExpenses;
    private final List<Income> allIncome;

    private Chart currentChart;

    public IncomeExpenseChartView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        filterComboBox = new ComboBox<>("Select Filter:");
        filterComboBox.setItems("Past 6 months", "Past 12 months", "Future 6 months", "Future 12 months");
        filterComboBox.setValue("Past 12 months");

        add(filterComboBox);

        db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        allExpenses = dao.getAllExpenses();
        allIncome = dao.getAllIncome();

        chart = new Chart(ChartType.COLUMN);
        conf = chart.getConfiguration();
        conf.setTitle("Income vs Expenses by Month");

        YAxis y = new YAxis();
        y.setTitle("Amount (€)");
        conf.addyAxis(y);

        conf.setTooltip(new Tooltip(true));
        chart.getConfiguration().getChart().setStyledMode(true);

        add(chart);

        // Load initially
        updateChart("Past 12 months");

        filterComboBox.addValueChangeListener(event -> {
            String selectedFilter = event.getValue();
            updateChart(selectedFilter);
        });
    }

    private void updateChart(String filter) {
        // Remove existing chart cleanly
        if (currentChart != null) {
            remove(currentChart);
        }

        // Create a new chart instance each time
        Chart newChart = new Chart(ChartType.COLUMN);
        Configuration conf = newChart.getConfiguration();

        // Set up the Y-axis
        YAxis y = new YAxis();
        y.setTitle("Amount (€)");
        conf.addyAxis(y);

        // Enable tooltips
        conf.setTooltip(new Tooltip(true));
        conf.getChart().setStyledMode(true);

        // === Data Preparation ===
        TreeSet<LocalDate> months = new TreeSet<>();
        Map<LocalDate, Double> incomeByMonth = new HashMap<>();
        Map<LocalDate, Double> expensesByMonth = new HashMap<>();

        for (Income income : allIncome) {
            LocalDate month = income.getMonth().withDayOfMonth(1);
            months.add(month);
            incomeByMonth.put(month, incomeByMonth.getOrDefault(month, 0.0) + income.getAmount());
        }

        for (MainView.Expense expense : allExpenses) {
            LocalDate startMonth = expense.getDate().withDayOfMonth(1);
            if (expense.getRecurring()) {
                for (int i = 0; i < 36; i++) {
                    LocalDate recurringMonth = "Monthly".equals(expense.getRecurrenceType())
                            ? startMonth.plusMonths(i)
                            : startMonth.plusYears(i);

                    if (recurringMonth.isAfter(LocalDate.now().plusMonths(12))) {
                        break;
                    }

                    months.add(recurringMonth);
                    expensesByMonth.put(recurringMonth, expensesByMonth.getOrDefault(recurringMonth, 0.0) + expense.getAmount());
                }
            } else {
                months.add(startMonth);
                expensesByMonth.put(startMonth, expensesByMonth.getOrDefault(startMonth, 0.0) + expense.getAmount());
            }
        }

        LocalDate now = LocalDate.now().withDayOfMonth(1);
        LocalDate startFilter, endFilter;

        switch (filter) {
            case "Past 6 months":
                startFilter = now.minusMonths(5);
                endFilter = now;
                break;
            case "Past 12 months":
                startFilter = now.minusMonths(11);
                endFilter = now;
                break;
            case "Future 6 months":
                startFilter = now.plusMonths(1);
                endFilter = now.plusMonths(6);
                break;
            case "Future 12 months":
                startFilter = now.plusMonths(1);
                endFilter = now.plusMonths(12);
                break;
            default:
                startFilter = now.minusMonths(11);
                endFilter = now;
                break;
        }

        List<LocalDate> filteredMonths = months.stream()
                .filter(month -> !month.isBefore(startFilter) && !month.isAfter(endFilter))
                .sorted()
                .toList();

        List<String> categories = filteredMonths.stream()
                .map(month -> month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear())
                .toList();

        // Configure X-axis with filtered categories
        XAxis x = new XAxis();
        x.setCategories(categories.toArray(new String[0]));
        conf.addxAxis(x);

        // Prepare series
        ListSeries incomeSeries = new ListSeries("Income");
        ListSeries expenseSeries = new ListSeries("Expenses");

        for (LocalDate month : filteredMonths) {
            incomeSeries.addData(incomeByMonth.getOrDefault(month, 0.0));
            expenseSeries.addData(expensesByMonth.getOrDefault(month, 0.0));
        }

        conf.addSeries(incomeSeries);
        conf.addSeries(expenseSeries);

        // Track and display the new chart
        currentChart = newChart;
        add(currentChart);
    }





}
