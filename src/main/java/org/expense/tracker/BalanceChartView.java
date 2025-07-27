package org.expense.tracker;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        setSpacing(true);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

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
        double finalBalance = 0.0;
        double maxBalance = Double.MIN_VALUE;
        double minBalance = Double.MAX_VALUE;

        for (LocalDate month : months) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            runningBalance += income - expense;

            monthLabels.add(month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + month.getYear());
            balanceData.add(runningBalance);

            maxBalance = Math.max(maxBalance, runningBalance);
            minBalance = Math.min(minBalance, runningBalance);
            finalBalance = runningBalance;
        }

        // Header
        H2 title = new H2("📊 Running Balance Over Time");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "700")
                .set("font-size", "2rem");

        // Enhanced summary card with blue gradient
        VerticalLayout summaryCard = createBalanceSummaryCard(finalBalance, maxBalance, minBalance, months.size());

        // Chart card
        VerticalLayout chartCard = createChartCard(monthLabels, balanceData);

        add(title, summaryCard, chartCard);
    }

    private VerticalLayout createBalanceSummaryCard(double currentBalance, double maxBalance, double minBalance, int monthsTracked) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.15)")
                .set("margin-bottom", "1.5rem")
                .set("color", "white");

        H3 summaryTitle = new H3("📈 Balance Analytics");
        summaryTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "white")
                .set("font-weight", "600")
                .set("text-align", "center")
                .set("font-size", "1.5rem");

        // Create stats layout
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        statsLayout.setSpacing(true);

        // Current Balance
        VerticalLayout currentCard = createBlueMiniStatCard("Current Balance", "€" + String.format("%.2f", currentBalance), currentBalance >= 0);

        // Max Balance
        VerticalLayout maxCard = createBlueMiniStatCard("Highest Balance", "€" + String.format("%.2f", maxBalance), true);

        // Min Balance
        VerticalLayout minCard = createBlueMiniStatCard("Lowest Balance", "€" + String.format("%.2f", minBalance), minBalance >= 0);

        // Balance Range
        double range = maxBalance - minBalance;
        VerticalLayout rangeCard = createBlueMiniStatCard("Balance Range", "€" + String.format("%.2f", range), true);

        // Months Tracked
        VerticalLayout monthsCard = createBlueMiniStatCard("Months Tracked", String.valueOf(monthsTracked), true);

        statsLayout.add(currentCard, maxCard, minCard, rangeCard, monthsCard);
        card.add(summaryTitle, statsLayout);

        return card;
    }

    private VerticalLayout createBlueMiniStatCard(String label, String value, boolean isPositive) {
        VerticalLayout miniCard = new VerticalLayout();
        miniCard.getStyle()
                .set("background", "rgba(255,255,255,0.15)")
                .set("border-radius", "12px")
                .set("padding", "1rem")
                .set("text-align", "center")
                .set("backdrop-filter", "blur(10px)")
                .set("min-width", "140px")
                .set("transition", "transform 0.2s ease");

        // Add hover effect
        miniCard.getElement().addEventListener("mouseenter", e -> {
            miniCard.getStyle().set("transform", "translateY(-2px)");
        });
        miniCard.getElement().addEventListener("mouseleave", e -> {
            miniCard.getStyle().set("transform", "translateY(0)");
        });

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "700")
                .set("display", "block");

        // Set color based on context
        if (value.startsWith("€")) {
            valueSpan.getStyle().set("color", isPositive ? "#4ade80" : "#f87171"); // Green for positive, red for negative
        } else {
            valueSpan.getStyle().set("color", "white");
        }

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "rgba(255,255,255,0.8)")
                .set("display", "block")
                .set("margin-top", "0.25rem");

        miniCard.add(valueSpan, labelSpan);
        return miniCard;
    }

    private VerticalLayout createChartCard(List<String> monthLabels, List<Double> balanceData) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
                .set("flex-grow", "1");

        H3 cardTitle = new H3("📊 Balance Trend Chart");
        cardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center");

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
        chart.getConfiguration().getChart().setStyledMode(true);

        // Enhanced chart styling
        chart.getStyle()
                .set("border-radius", "12px")
                .set("overflow", "hidden");

        card.add(cardTitle, chart);
        return card;
    }
}