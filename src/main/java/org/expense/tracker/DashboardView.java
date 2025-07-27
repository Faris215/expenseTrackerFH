package org.expense.tracker;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
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
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {

    public DashboardView() {
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        ExpenseDAO dao = new ExpenseDAO(db.getConnection());

        List<MainView.Expense> expenses = dao.getAllExpenses();
        List<Income> incomes = dao.getAllIncome();
        BankBalance bankBalance = dao.getCurrentBankBalance();

        LocalDate now = LocalDate.now().withDayOfMonth(1);
        List<LocalDate> last12Months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            last12Months.add(now.minusMonths(i));
        }

        double monthlyIncome = getIncomeForMonth(incomes, now);
        double monthlyExpenses = getExpensesForMonth(expenses, now);
        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
        double totalExpenses = expenses.stream().mapToDouble(MainView.Expense::getAmount).sum();
        double calculatedBalance = totalIncome - totalExpenses;
        double currentBankBalance = bankBalance != null ? bankBalance.getAmount() : 0.0;

        String topCategory = expenses.stream()
                .collect(Collectors.groupingBy(MainView.Expense::getCategory, Collectors.summingDouble(MainView.Expense::getAmount)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        // Header
        H2 dashboardTitle = new H2("📊 Financial Dashboard");
        dashboardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "700")
                .set("font-size", "2rem");

        // Enhanced summary card with blue gradient
        VerticalLayout summaryCard = createSummaryCard(currentBankBalance, calculatedBalance, monthlyIncome, monthlyExpenses, topCategory);

        // Stats cards

        // Charts section
        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setSpacing(true);

        // Income/Expense chart (left side)
        VerticalLayout leftChart = new VerticalLayout();
        leftChart.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("flex", "1");

        H2 chartTitle = new H2("📈 12-Month Financial Overview");
        chartTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)");

        Chart incomeExpenseChart = createIncomeExpenseBalanceChart(last12Months, incomes, expenses);
        leftChart.add(chartTitle, incomeExpenseChart);

        // Pie chart (right side)
        VerticalLayout rightChart = new VerticalLayout();
        rightChart.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("flex", "1");

        H2 pieTitle = new H2("🥧 Expenses by Category");
        pieTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)");

        Chart pieChart = createExpensePieChart(expenses);
        rightChart.add(pieTitle, pieChart);

        chartsLayout.add(leftChart, rightChart);

        add(dashboardTitle, summaryCard, chartsLayout);
    }

    private VerticalLayout createSummaryCard(double bankBalance, double calculatedBalance, double monthlyIncome, double monthlyExpenses, String topCategory) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.15)")
                .set("margin-bottom", "1.5rem")
                .set("color", "white");

        H3 summaryTitle = new H3("💰 Financial Overview");
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

        // Bank Balance
        VerticalLayout bankCard = createBlueMiniStatCard("Bank Balance", "€" + round(bankBalance), bankBalance >= 0);

        // Monthly Net
        double monthlyNet = monthlyIncome - monthlyExpenses;
        VerticalLayout netCard = createBlueMiniStatCard("Monthly Net", "€" + round(monthlyNet), monthlyNet >= 0);

        // Monthly Income
        VerticalLayout incomeCard = createBlueMiniStatCard("Monthly Income", "€" + round(monthlyIncome), true);

        // Monthly Expenses
        VerticalLayout expenseCard = createBlueMiniStatCard("Monthly Expenses", "€" + round(monthlyExpenses), false);

        // Top Category
        VerticalLayout categoryCard = createBlueMiniStatCard("Top Category", topCategory, true);

        statsLayout.add(bankCard, incomeCard, expenseCard, netCard, categoryCard);
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

    private VerticalLayout createStatCard(String title, String value, String colorType) {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("text-align", "center")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("transition", "transform 0.2s ease, box-shadow 0.2s ease")
                .set("cursor", "pointer");

        // Hover effect
        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle()
                    .set("transform", "translateY(-2px)")
                    .set("box-shadow", "0 4px 12px rgba(0,0,0,0.15)");
        });

        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle()
                    .set("transform", "translateY(0)")
                    .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
        });

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "0.9rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("margin-bottom", "0.5rem")
                .set("display", "block");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.8rem")
                .set("font-weight", "700")
                .set("margin", "0.5rem 0 0 0")
                .set("display", "block");

        // Set colors based on type
        switch (colorType) {
            case "success":
                valueSpan.getStyle().set("color", "var(--lumo-success-color)");
                break;
            case "error":
                valueSpan.getStyle().set("color", "var(--lumo-error-color)");
                break;
            case "primary":
                valueSpan.getStyle().set("color", "var(--lumo-primary-color)");
                break;
            case "secondary":
                valueSpan.getStyle().set("color", "var(--lumo-primary-text-color)");
                break;
            default:
                valueSpan.getStyle().set("color", "var(--lumo-contrast-90pct)");
        }

        card.add(titleSpan, valueSpan);
        return card;
    }

    private Chart createExpensePieChart(List<MainView.Expense> expenses) {
        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        chart.getConfiguration().getChart().setStyledMode(true);

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);

        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.name}: {point.percentage:.1f}%");
        plotOptions.setDataLabels(dataLabels);
        conf.setPlotOptions(plotOptions);

        Map<String, Double> categoryTotals = new HashMap<>();
        for (MainView.Expense expense : expenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        DataSeries series = new DataSeries();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            series.add(new DataSeriesItem(entry.getKey(), entry.getValue()));
        }

        conf.setSeries(series);
        chart.setHeight("400px");
        return chart;
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
        ListSeries balanceSeries = new ListSeries("Net Balance");

        for (LocalDate month : months) {
            double income = getIncomeForMonth(incomes, month);
            double exp = getExpensesForMonth(expenses, month);
            double bal = income - exp;

            incomeSeries.addData(income);
            expenseSeries.addData(exp);
            balanceSeries.addData(bal);
        }

        Chart chart = new Chart(ChartType.COLUMN);
        chart.setHeight("400px");
        Configuration conf = chart.getConfiguration();
        chart.getConfiguration().getChart().setStyledMode(true);

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