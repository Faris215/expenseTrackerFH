package org.vaadin.example;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "chart", layout = MainLayout.class)
@PageTitle("Expense Chart")
public class ChartView extends VerticalLayout {

    public ChartView() {
        setSizeFull();
        setPadding(true);

        DBConnect dbConnect = new DBConnect();
        dbConnect.connectToDatabase("expenseTracker", "postgres", "faris123");
        ExpenseDAO expenseDAO = new ExpenseDAO(dbConnect.getConnection());

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Expenses by Category");

        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);

        // Show % labels
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("{point.name}: {point.percentage:.1f} %");
        plotOptions.setDataLabels(dataLabels);
        conf.setPlotOptions(plotOptions);

        List<MainView.Expense> allExpenses = expenseDAO.getAllExpenses();

        Map<String, Double> categoryTotals = new HashMap<>();
        for (MainView.Expense expense : allExpenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        DataSeries series = new DataSeries();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            series.add(new DataSeriesItem(entry.getKey(), entry.getValue()));
        }

        conf.setSeries(series);
        add(chart);
    }
}
