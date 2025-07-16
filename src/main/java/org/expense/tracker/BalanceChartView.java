package org.expense.tracker;

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

        TreeSet<LocalDate> months = new TreeSet<>();

        Map<LocalDate, Double> incomeMap = new HashMap<>();
        for (Income income : allIncome) {
            LocalDate month = income.getMonth().withDayOfMonth(1);
            months.add(month);
            incomeMap.put(month, incomeMap.getOrDefault(month, 0.0) + income.getAmount());
        }

        Map<LocalDate, Double> expenseMap = new HashMap<>();
        for (MainView.Expense expense : allExpenses) {
            LocalDate startMonth = expense.getDate().withDayOfMonth(1);

            if (expense.getRecurring()) {
                for (int i = 0; i < 36; i++) {
                    LocalDate recurringMonth = "Monthly".equals(expense.getRecurrenceType())
                            ? startMonth.plusMonths(i)
                            : startMonth.plusYears(i);

                    if (recurringMonth.isAfter(LocalDate.now().plusMonths(12))) break;

                    months.add(recurringMonth);
                    expenseMap.put(recurringMonth, expenseMap.getOrDefault(recurringMonth, 0.0) + expense.getAmount());
                }
            } else {
                months.add(startMonth);
                expenseMap.put(startMonth, expenseMap.getOrDefault(startMonth, 0.0) + expense.getAmount());
            }
        }

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

        Chart chart = new Chart(ChartType.LINE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Running Balance Over Time");

        XAxis x = new XAxis();
        x.setCategories(monthLabels.toArray(new String[0]));
        conf.addxAxis(x);

        YAxis y = new YAxis();
        y.setTitle("Balance (€)");
        conf.addyAxis(y);

        conf.addSeries(new ListSeries("Balance", balanceData.toArray(new Number[0])));
        conf.setTooltip(new Tooltip(true));
        chart.setSizeFull();

        add(chart);
    }
}
