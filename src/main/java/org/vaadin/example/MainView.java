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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Expenses")
public class MainView extends VerticalLayout {

    private final Grid<Expense> expenseGrid = new Grid<>(Expense.class);
    private final ExpenseService expenseService = new ExpenseService();
    private Expense selectedExpense = null;

    public MainView() {
        NumberField amountField = new NumberField("Amount");
        amountField.setPrefixComponent(new Paragraph("€"));

        TextField categoryField = new TextField("Category");
        TextField descriptionField = new TextField("Description");
        DatePicker datePicker = new DatePicker("Date");

        Button addOrUpdateButton = new Button("Save");
        Button deleteButton = new Button("Delete");

        addOrUpdateButton.addClickListener(e -> {
            if (amountField.isEmpty() || categoryField.isEmpty()) {
                Notification.show("Please fill in amount and category");
                return;
            }

            Expense expense = new Expense(
                    amountField.getValue(),
                    categoryField.getValue(),
                    descriptionField.getValue(),
                    datePicker.getValue()
            );

            if (selectedExpense != null) {
                expense.setId(selectedExpense.getId());
                expenseService.updateExpense(expense);
            } else {
                expenseService.addExpense(expense);
            }

            updateGrid();
            clearForm(amountField, categoryField, descriptionField, datePicker);
        });

        deleteButton.addClickListener(e -> {
            if (selectedExpense != null) {
                expenseService.deleteExpense(selectedExpense.getId());
                updateGrid();
                clearForm(amountField, categoryField, descriptionField, datePicker);
            }
        });

        Button balanceButton = new Button("Show Monthly Balance", e -> showMonthlyBalance());

        HorizontalLayout formLayout = new HorizontalLayout(amountField, categoryField, descriptionField, datePicker, addOrUpdateButton, deleteButton, balanceButton);
        formLayout.setWidthFull();

        expenseGrid.setColumns("amount", "category", "description", "date");
        expenseGrid.setItems(expenseService.getAllExpenses());
        expenseGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        expenseGrid.asSingleSelect().addValueChangeListener(e -> {
            selectedExpense = e.getValue();
            if (selectedExpense != null) {
                amountField.setValue(selectedExpense.getAmount());
                categoryField.setValue(selectedExpense.getCategory());
                descriptionField.setValue(selectedExpense.getDescription() == null ? "" : selectedExpense.getDescription());
                datePicker.setValue(selectedExpense.getDate());
            }
        });




        add(formLayout, expenseGrid);
    }

    private void updateGrid() {
        List<Expense> expenses = expenseService.getAllExpenses();
        expenseGrid.setItems(expenses);
        selectedExpense = null;
    }

    private void clearForm(NumberField amount, TextField category, TextField desc, DatePicker date) {
        amount.clear();
        category.clear();
        desc.clear();
        date.setValue(LocalDate.now());
        selectedExpense = null;
    }

    private void showMonthlyBalance() {
        LocalDate now = LocalDate.now().withDayOfMonth(1);
        ExpenseDAO dao = new ExpenseDAO(new DBConnect().getConnection());
        Income income = dao.getIncomeForMonth(now);

        double incomeAmount = income != null ? income.getAmount() : 0;
        double totalExpenses = dao.getAllExpenses().stream()
                .filter(e -> e.getDate().getMonth().equals(now.getMonth()))
                .mapToDouble(MainView.Expense::getAmount)
                .sum();

        double balance = incomeAmount - totalExpenses;

        Notification.show("Balance for " + now.getMonth() + ": €" + balance);
    }


    public static class Expense {
        private Integer id;
        private Double amount;
        private String category;
        private String description;
        private LocalDate date;

        public Expense() {}

        public Expense(Double amount, String category, String description, LocalDate date) {
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

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
