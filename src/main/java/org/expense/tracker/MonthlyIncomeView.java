package org.expense.tracker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "income", layout = MainLayout.class)
@PageTitle("Monthly Income")
public class MonthlyIncomeView extends VerticalLayout {

    private final ExpenseDAO dao;
    private final NumberField amountField;
    private final DatePicker monthPicker;
    private final Grid<Income> grid;
    private final ListDataProvider<Income> dataProvider;

    public MonthlyIncomeView() {
        setSpacing(true);
        setPadding(true);

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        amountField = new NumberField("Income Amount");
        amountField.setPrefixComponent(new Span("€"));

        monthPicker = new DatePicker("Month");
        monthPicker.setInitialPosition(LocalDate.now().withDayOfMonth(1));

        // Save button (standalone for new or updated values)
        Button saveButton = new Button("Save", event -> {
            if (amountField.isEmpty() || monthPicker.isEmpty()) {
                Notification.show("Fill in both month and amount");
                return;
            }

            LocalDate selectedMonth = monthPicker.getValue().withDayOfMonth(1);
            dao.saveOrUpdateIncome(new Income(amountField.getValue(), selectedMonth));
            reloadGrid();
            Notification.show("Income saved for " + selectedMonth.getMonth());
        });

        add(new HorizontalLayout(monthPicker, amountField), saveButton);

        // Grid setup
        grid = new Grid<>(Income.class, false);
        grid.addColumn(income -> income.getMonth().getMonth() + " " + income.getMonth().getYear()).setHeader("Month");
        grid.addColumn(Income::getAmount).setHeader("Amount (€)");

        grid.addItemClickListener(event -> {
            Income selectedIncome = event.getItem();
            amountField.setValue(selectedIncome.getAmount());
            monthPicker.setValue(selectedIncome.getMonth());
        });

        grid.setWidthFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        dataProvider = new ListDataProvider<>(dao.getAllIncome());
        grid.setDataProvider(dataProvider);

        add(grid);
    }

    private void reloadGrid() {
        List<Income> updated = dao.getAllIncome();
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updated);
        dataProvider.refreshAll();
    }
}
