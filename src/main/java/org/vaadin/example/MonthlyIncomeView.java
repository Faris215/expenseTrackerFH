package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;

@Route(value = "income", layout = MainLayout.class)
@PageTitle("Monthly Income")
public class MonthlyIncomeView extends VerticalLayout {

    private final ExpenseDAO dao;

    public MonthlyIncomeView() {
        setSpacing(true);
        setPadding(true);

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        NumberField amountField = new NumberField("Income Amount");
        amountField.setPrefixComponent(new com.vaadin.flow.component.html.Span("€"));

        DatePicker monthPicker = new DatePicker("Month");
        monthPicker.setInitialPosition(LocalDate.now().withDayOfMonth(1));

        Button saveButton = new Button("Save", event -> {
            if (amountField.isEmpty() || monthPicker.isEmpty()) {
                Notification.show("Fill in both month and amount");
                return;
            }

            LocalDate selectedMonth = monthPicker.getValue().withDayOfMonth(1);
            dao.saveOrUpdateIncome(new Income(amountField.getValue(), selectedMonth));
            Notification.show("Income saved for " + selectedMonth.getMonth());
        });

        Button loadButton = new Button("Load", event -> {
            if (monthPicker.isEmpty()) return;
            LocalDate selectedMonth = monthPicker.getValue().withDayOfMonth(1);
            Income income = dao.getIncomeForMonth(selectedMonth);
            if (income != null) {
                amountField.setValue(income.getAmount());
            } else {
                amountField.clear();
                Notification.show("No income recorded for " + selectedMonth.getMonth());
            }
        });

        add(new HorizontalLayout(monthPicker, amountField), new HorizontalLayout(loadButton, saveButton));
    }
}
