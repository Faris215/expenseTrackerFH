package org.expense.tracker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Route(value = "income", layout = MainLayout.class)
@PageTitle("Monthly Income")
public class MonthlyIncomeView extends VerticalLayout {

    private final ExpenseDAO dao;
    private NumberField amountField;
    private DatePicker monthPicker;
    private Grid<Income> grid;
    private ListDataProvider<Income> dataProvider;
    private Income selectedIncome = null;

    public MonthlyIncomeView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        // Header
        H2 title = new H2("💰 Monthly Income Management");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "700")
                .set("font-size", "2rem")
                .set("text-align", "center");

        // Summary card
        VerticalLayout summaryCard = createSummaryCard();

        // Form and grid layout
        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setWidthFull();
        mainContent.setSpacing(true);

        // Form card (left side)
        VerticalLayout formCard = createFormCard();
        formCard.setWidth("400px");

        // Grid card (right side)
        VerticalLayout gridCard = createGridCard();
        gridCard.setFlexGrow(1);

        mainContent.add(formCard, gridCard);

        add(title, summaryCard, mainContent);
    }

    private VerticalLayout createSummaryCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.15)")
                .set("margin-bottom", "1.5rem")
                .set("color", "white")
                .set("text-align", "center");

        List<Income> allIncome = dao.getAllIncome();
        double totalIncome = allIncome.stream().mapToDouble(Income::getAmount).sum();
        int totalMonths = allIncome.size();
        double avgIncome = totalMonths > 0 ? totalIncome / totalMonths : 0;

        H3 summaryTitle = new H3("Income Overview");
        summaryTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "white")
                .set("font-weight", "600");

        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        statsLayout.setSpacing(true);

        VerticalLayout totalCard = createMiniStatCard("Total Income", "€" + String.format("%.2f", totalIncome));
        VerticalLayout monthsCard = createMiniStatCard("Months Tracked", String.valueOf(totalMonths));
        VerticalLayout avgCard = createMiniStatCard("Average/Month", "€" + String.format("%.2f", avgIncome));

        statsLayout.add(totalCard, monthsCard, avgCard);
        card.add(summaryTitle, statsLayout);

        return card;
    }

    private VerticalLayout createMiniStatCard(String label, String value) {
        VerticalLayout miniCard = new VerticalLayout();
        miniCard.getStyle()
                .set("background", "rgba(255,255,255,0.1)")
                .set("border-radius", "12px")
                .set("padding", "1rem")
                .set("text-align", "center")
                .set("backdrop-filter", "blur(10px)")
                .set("min-width", "120px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("color", "white")
                .set("display", "block");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "rgba(255,255,255,0.8)")
                .set("display", "block")
                .set("margin-top", "0.25rem");

        miniCard.add(valueSpan, labelSpan);
        return miniCard;
    }

    private VerticalLayout createFormCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)")
                .set("height", "fit-content");

        H3 cardTitle = new H3("💼 Add/Edit Income");
        cardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("font-size", "1.2rem")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-align", "center");

        // Form fields with enhanced styling
        monthPicker = new DatePicker("📅 Select Month");
        monthPicker.setInitialPosition(LocalDate.now().withDayOfMonth(1));
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        monthPicker.setWidthFull();
        monthPicker.getStyle()
                .set("margin-bottom", "1rem");

        amountField = new NumberField("💰 Income Amount");
        amountField.setPrefixComponent(new Span("€"));
        amountField.setPlaceholder("Enter amount...");
        amountField.setWidthFull();
        amountField.getStyle()
                .set("margin-bottom", "1.5rem");

        // Enhanced buttons
        Button saveButton = new Button("Save Income");
        saveButton.setIcon(VaadinIcon.CHECK_CIRCLE.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        saveButton.setWidthFull();
        saveButton.getStyle()
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("margin-bottom", "0.5rem");

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setIcon(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);
        deleteButton.setWidthFull();
        deleteButton.getStyle()
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("margin-bottom", "0.5rem");

        Button clearButton = new Button("Clear Form");
        clearButton.setIcon(VaadinIcon.REFRESH.create());
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        clearButton.setWidthFull();
        clearButton.getStyle()
                .set("border-radius", "12px")
                .set("font-weight", "600");

        // Event handlers
        saveButton.addClickListener(event -> {
            if (amountField.isEmpty() || monthPicker.isEmpty()) {
                Notification notification = Notification.show("Please fill in both month and amount");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            LocalDate selectedMonth = monthPicker.getValue().withDayOfMonth(1);
            Income income = new Income(amountField.getValue(), selectedMonth);

            dao.saveOrUpdateIncome(income);
            reloadGrid();
            reloadSummary();
            clearForm();

            Notification notification = Notification.show("Income saved for " +
                    selectedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + selectedMonth.getYear());
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        deleteButton.addClickListener(event -> {
            if (selectedIncome != null) {
                dao.deleteIncome(selectedIncome.getId());
                reloadGrid();
                reloadSummary();
                clearForm();

                Notification notification = Notification.show("Income deleted successfully!");
                notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                Notification notification = Notification.show("Please select an income entry to delete");
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        clearButton.addClickListener(event -> clearForm());

        card.add(cardTitle, monthPicker, amountField, saveButton, deleteButton, clearButton);
        return card;
    }

    private VerticalLayout createGridCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)");

        H3 cardTitle = new H3("📊 Income History");
        cardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("font-size", "1.2rem")
                .set("color", "var(--lumo-primary-text-color)")
                .set("text-align", "center");

        // Enhanced grid
        grid = new Grid<>(Income.class, false);

        // Month column with better formatting
        grid.addColumn(income -> {
                    LocalDate month = income.getMonth();
                    return month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();
                })
                .setHeader("📅 Month")
                .setFlexGrow(1)
                .setSortable(true);

        // Amount column with enhanced rendering
        grid.addColumn(new ComponentRenderer<>(income -> {
                    Span amountSpan = new Span("€" + String.format("%.2f", income.getAmount()));
                    amountSpan.getStyle()
                            .set("font-weight", "600")
                            .set("color", "var(--lumo-success-color)")
                            .set("font-size", "1.1rem");
                    return amountSpan;
                }))
                .setHeader("💰 Amount")
                .setWidth("150px")
                .setFlexGrow(0)
                .setSortable(true);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_COLUMN_BORDERS,
                GridVariant.LUMO_WRAP_CELL_CONTENT
        );
        grid.setHeight("500px");

        // Enhanced selection styling
        grid.getStyle()
                .set("border-radius", "12px")
                .set("overflow", "hidden");

        // Selection handler
        grid.asSingleSelect().addValueChangeListener(event -> {
            selectedIncome = event.getValue();
            populateFormFields(selectedIncome);

            if (selectedIncome != null) {
                Notification.show("Selected: " +
                        selectedIncome.getMonth().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                        " " + selectedIncome.getMonth().getYear(), 2000, Notification.Position.BOTTOM_CENTER);
            }
        });

        dataProvider = new ListDataProvider<>(dao.getAllIncome());
        grid.setDataProvider(dataProvider);

        card.add(cardTitle, grid);
        return card;
    }

    private void populateFormFields(Income income) {
        if (income != null) {
            amountField.setValue(income.getAmount());
            monthPicker.setValue(income.getMonth());
        } else {
            clearForm();
        }
    }

    private void clearForm() {
        amountField.clear();
        monthPicker.setValue(LocalDate.now().withDayOfMonth(1));
        selectedIncome = null;
        grid.deselectAll();
    }

    private void reloadGrid() {
        List<Income> updated = dao.getAllIncome();
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updated);
        dataProvider.refreshAll();
    }

    private void reloadSummary() {
        // Force page refresh to update summary - in a real app you'd update the summary component directly
        getUI().ifPresent(ui -> ui.getPage().reload());
    }
}