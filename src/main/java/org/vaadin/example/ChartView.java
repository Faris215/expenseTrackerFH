package org.vaadin.example;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import org.vaadin.example.MainLayout;
import org.vaadin.example.MainView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "chart", layout = MainLayout.class)
@PageTitle("Expense Chart")
public class ChartView extends VerticalLayout {

    public ChartView() {
        setSizeFull();
        setPadding(true);

        Chart chart = new Chart(ChartType.PIE);
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Expenses by Category");

        List<MainView.Expense> allExpenses = MainView.expenses; // We’ll make this static for now

        // Aggregate by category
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
