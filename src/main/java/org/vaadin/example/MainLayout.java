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
        RouterLink chartLink = new RouterLink("Chart", ChartView.class);

        VerticalLayout menuLayout = new VerticalLayout(mainLink, chartLink);
        menuLayout.setPadding(true);

        addToDrawer(menuLayout);
    }
}
