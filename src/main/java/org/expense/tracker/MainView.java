package org.expense.tracker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "Expenses", layout = MainLayout.class)
@PageTitle("Expenses")
public class MainView extends VerticalLayout {

    private final Grid<Expense> expenseGrid = new Grid<>(Expense.class, false);
    private final ExpenseService expenseService = new ExpenseService();
    private Expense selectedExpense = null;

    // Form fields as instance variables
    private NumberField amountField;
    private TextField categoryField;
    private TextField descriptionField;
    private DatePicker datePicker;
    private ComboBox<String> recurringCombo;

    public MainView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        // Header
        H2 title = new H2("💸 Expense Management");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "700")
                .set("font-size", "2rem");

        // Form card
        VerticalLayout formCard = createFormCard();

        // Grid card
        VerticalLayout gridCard = createGridCard();

        add(title, formCard, gridCard);
    }

    private VerticalLayout createFormCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-bottom", "1.5rem");

        H2 cardTitle = new H2("Add/Edit Expense");
        cardTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)");

        // Form fields
        amountField = new NumberField("Amount");
        amountField.setPrefixComponent(new Span("€"));
        amountField.setPlaceholder("0.00");
        amountField.addClassName("bordered");

        categoryField = new TextField("Category");
        categoryField.setPlaceholder("e.g., Food, Transport, Entertainment");
        categoryField.addClassName("bordered");

        descriptionField = new TextField("Description");
        descriptionField.setPlaceholder("Optional description");
        descriptionField.addClassName("bordered");

        datePicker = new DatePicker("Date");
        datePicker.setValue(LocalDate.now());
        datePicker.addClassName("bordered");

        recurringCombo = new ComboBox<>("Recurring");
        recurringCombo.setItems("None", "Monthly", "Annually");
        recurringCombo.setValue("None");
        recurringCombo.addClassName("bordered");

        // Buttons
        Button addOrUpdateButton = new Button("Save Expense");
        addOrUpdateButton.setIcon(VaadinIcon.CHECK.create());
        addOrUpdateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addOrUpdateButton.getStyle()
                .set("border-radius", "8px")
                .set("font-weight", "600");

        Button deleteButton = new Button("Delete");
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.getStyle()
                .set("border-radius", "8px")
                .set("font-weight", "600");

        Button clearButton = new Button("Clear Form");
        clearButton.setIcon(VaadinIcon.REFRESH.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        clearButton.getStyle()
                .set("border-radius", "8px")
                .set("font-weight", "600");

        // Event handlers
        addOrUpdateButton.addClickListener(e -> {
            if (amountField.isEmpty() || categoryField.isEmpty()) {
                Notification notification = Notification.show("Please fill in amount and category");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            String recurrenceValue = recurringCombo.getValue();
            boolean isRecurring = !"None".equals(recurrenceValue);
            String recurrenceType = isRecurring ? recurrenceValue : null;

            Expense expense = new Expense(
                    amountField.getValue(),
                    categoryField.getValue(),
                    descriptionField.getValue(),
                    datePicker.getValue(),
                    isRecurring,
                    recurrenceType
            );

            if (selectedExpense != null) {
                expense.setId(selectedExpense.getId());
                expenseService.updateExpense(expense);
                Notification notification = Notification.show("Expense updated successfully!");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                expenseService.addExpense(expense);
                Notification notification = Notification.show("Expense added successfully!");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            updateGrid();
            clearForm();
        });

        deleteButton.addClickListener(e -> {
            if (selectedExpense != null) {
                expenseService.deleteExpense(selectedExpense.getId());
                updateGrid();
                clearForm();
                Notification notification = Notification.show("Expense deleted successfully!");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show("Please select an expense to delete");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        clearButton.addClickListener(e -> {
            clearForm();
        });

        // Layout
        HorizontalLayout row1 = new HorizontalLayout(amountField, categoryField, descriptionField);
        row1.setWidthFull();
        row1.setFlexGrow(1, amountField, categoryField, descriptionField);

        HorizontalLayout row2 = new HorizontalLayout(datePicker, recurringCombo);
        row2.setWidthFull();
        row2.setFlexGrow(1, datePicker, recurringCombo);

        HorizontalLayout buttonLayout = new HorizontalLayout(addOrUpdateButton, deleteButton, clearButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        buttonLayout.setSpacing(true);

        card.add(cardTitle, row1, row2, buttonLayout);
        return card;
    }

    private VerticalLayout createGridCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("flex-grow", "1");

        H2 cardTitle = new H2("📋 Expense History");
        cardTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)");

        // Configure grid
        expenseGrid.addColumn(Expense::getAmount)
                .setHeader("Amount (€)")
                .setWidth("120px")
                .setFlexGrow(0);

        expenseGrid.addColumn(Expense::getCategory)
                .setHeader("Category")
                .setWidth("150px")
                .setFlexGrow(0);

        expenseGrid.addColumn(Expense::getDescription)
                .setHeader("Description")
                .setFlexGrow(1);

        expenseGrid.addColumn(Expense::getDate)
                .setHeader("Date")
                .setWidth("120px")
                .setFlexGrow(0);

        expenseGrid.addColumn(expense -> expense.getRecurring() ? "Yes" : "No")
                .setHeader("Recurring")
                .setWidth("100px")
                .setFlexGrow(0);

        expenseGrid.addColumn(expense -> expense.getRecurrenceType() != null ? expense.getRecurrenceType() : "-")
                .setHeader("Type")
                .setWidth("100px")
                .setFlexGrow(0);

        expenseGrid.setItems(expenseService.getAllExpenses());
        expenseGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        expenseGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        expenseGrid.setHeight("400px"); // Set a fixed height instead

        // Selection handler - we need to pass the form fields to populate them
        expenseGrid.asSingleSelect().addValueChangeListener(e -> {
            selectedExpense = e.getValue();
            populateFormFields(selectedExpense);
        });

        card.add(cardTitle, expenseGrid);
        return card;
    }

    private void updateGrid() {
        List<Expense> expenses = expenseService.getAllExpenses();
        expenseGrid.setItems(expenses);
        selectedExpense = null;
    }

    private void clearForm() {
        amountField.clear();
        categoryField.clear();
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
        recurringCombo.setValue("None");
        selectedExpense = null;
        expenseGrid.deselectAll();
    }

    private void populateFormFields(Expense expense) {
        if (expense != null) {
            amountField.setValue(expense.getAmount());
            categoryField.setValue(expense.getCategory());
            descriptionField.setValue(expense.getDescription() != null ? expense.getDescription() : "");
            datePicker.setValue(expense.getDate());
            String type = expense.getRecurrenceType();
            recurringCombo.setValue(type != null ? type : "None");
        } else {
            clearForm();
        }
    }

    public static class Expense {
        private Integer id;
        private Double amount;
        private String category;
        private String description;
        private LocalDate date;
        private boolean recurring;
        private String recurrenceType;

        public Expense() {}

        public Expense(Double amount, String category, String description, LocalDate date, boolean recurring, String recurrenceType) {
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
            this.recurring = recurring;
            this.recurrenceType = recurrenceType;
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

        public boolean getRecurring() { return recurring; }
        public void setRecurring(boolean recurring) { this.recurring = recurring; }

        public String getRecurrenceType() { return recurrenceType; }
        public void setRecurrenceType(String recurrenceType) { this.recurrenceType = recurrenceType; }
    }
}