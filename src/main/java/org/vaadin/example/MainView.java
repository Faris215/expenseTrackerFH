package org.vaadin.example;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
public class MainView extends VerticalLayout {

    public static final List<Expense> expenses = new ArrayList<>();
    private final Grid<Expense> expenseGrid = new Grid<>(Expense.class);

    public MainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // --- Expense Input Form ---
        NumberField amountField = new NumberField("Amount");
        amountField.setPrefixComponent(new Paragraph("€"));
        amountField.setClearButtonVisible(true);

        TextField categoryField = new TextField("Category");
        TextField descriptionField = new TextField("Description");
        DatePicker datePicker = new DatePicker("Date");
        datePicker.setValue(LocalDate.now());

        Button addButton = new Button("Add Expense", event -> {
            if (amountField.getValue() == null || categoryField.isEmpty()) {
                Notification.show("Please fill in at least amount and category");
                return;
            }

            Expense expense = new Expense(
                    amountField.getValue(),
                    categoryField.getValue(),
                    descriptionField.getValue(),
                    datePicker.getValue()
            );

            expenses.add(expense);
            expenseGrid.setItems(expenses);
            clearForm(amountField, categoryField, descriptionField, datePicker);
        });

        HorizontalLayout formLayout = new HorizontalLayout(amountField, categoryField, descriptionField, datePicker, addButton);
        formLayout.setWidthFull();
        formLayout.setFlexGrow(1, amountField, categoryField, descriptionField, datePicker);

        // --- Expense Grid ---
        expenseGrid.setColumns("amount", "category", "description", "date");
        expenseGrid.setSizeFull();

        add(formLayout, expenseGrid);
    }

    private void clearForm(NumberField amountField, TextField categoryField, TextField descriptionField, DatePicker datePicker) {
        amountField.clear();
        categoryField.clear();
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
    }

    public static class Expense {
        private Double amount;
        private String category;
        private String description;
        private LocalDate date;

        public Expense() {} // Needed for Grid

        public Expense(Double amount, String category, String description, LocalDate date) {
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }

        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
    }
}
