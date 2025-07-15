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
        List<Income> allIncome = dao.getAllIncome();

        TreeSet<LocalDate> months = new TreeSet<>();

        Map<LocalDate, Double> incomeByMonth = new HashMap<>();
        for (Income income : allIncome) {
            LocalDate month = income.getMonth().withDayOfMonth(1);
            months.add(month);
            incomeByMonth.put(month, incomeByMonth.getOrDefault(month, 0.0) + income.getAmount());
        }

        Map<LocalDate, Double> expensesByMonth = new HashMap<>();
        for (MainView.Expense expense : allExpenses) {
            LocalDate startMonth = expense.getDate().withDayOfMonth(1);

            if (expense.getRecurring()) {
                for (int i = 0; i < 36; i++) {
                    LocalDate recurringMonth = "Monthly".equals(expense.getRecurrenceType())
                            ? startMonth.plusMonths(i)
                            : startMonth.plusYears(i);

                    if (recurringMonth.isAfter(LocalDate.now().plusMonths(12))) break;

                    months.add(recurringMonth);
                    expensesByMonth.put(recurringMonth, expensesByMonth.getOrDefault(recurringMonth, 0.0) + expense.getAmount());
                }
            } else {
                months.add(startMonth);
                expensesByMonth.put(startMonth, expensesByMonth.getOrDefault(startMonth, 0.0) + expense.getAmount());
            }
        }

        List<String> categories = months.stream()
                .sorted()
                .map(month -> month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear())
                .toList();

        ListSeries incomeSeries = new ListSeries("Income");
        ListSeries expenseSeries = new ListSeries("Expenses");

        for (LocalDate month : months) {
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
        conf.setTooltip(new Tooltip(true));
        chart.getConfiguration().getChart().setStyledMode(true);

        add(chart);
    }
}
