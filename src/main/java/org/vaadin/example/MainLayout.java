package org.vaadin.example;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;


public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Expense Tracker");
        logo.getStyle().set("margin", "1rem");

        addToNavbar(logo);
    }

    private void createDrawer() {
        RouterLink mainLink = new RouterLink("Expenses", MainView.class);
        RouterLink chartLink = new RouterLink("Pie Chart", ChartView.class);
        RouterLink balanceLink = new RouterLink("Balance Chart", BalanceChartView.class); // <-- Add this
        RouterLink incomeLink = new RouterLink("Monthly Income", MonthlyIncomeView.class);
        RouterLink compareLink = new RouterLink("Income vs Expenses", IncomeExpenseChartView.class);



        VerticalLayout menuLayout = new VerticalLayout(mainLink, chartLink, incomeLink, compareLink, balanceLink); // <-- Include link
        menuLayout.setPadding(true);

        addToDrawer(menuLayout);
    }


}
