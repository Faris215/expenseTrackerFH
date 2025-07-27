package org.expense.tracker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "bank-balance", layout = MainLayout.class)
@PageTitle("Bank Balance")
public class BankBalanceView extends VerticalLayout {

    private final ExpenseDAO dao;
    private Span currentBalanceDisplay;
    private Span lastUpdatedDisplay;
    private NumberField newBalanceField;

    public BankBalanceView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        // Header
        H2 title = new H2("🏦 Bank Balance Management");
        title.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "700")
                .set("font-size", "2rem");

        // Enhanced summary card with blue gradient
        VerticalLayout summaryCard = createBalanceSummaryCard();

        // Update balance section
        VerticalLayout updateBalanceCard = createUpdateBalanceCard();

        add(title, summaryCard, updateBalanceCard);

        // Initialize displays
        refreshDisplays();
    }

    private VerticalLayout createBalanceSummaryCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "linear-gradient(135deg, var(--lumo-primary-color) 0%, var(--lumo-primary-color-50pct) 100%)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.15)")
                .set("margin-bottom", "1.5rem")
                .set("color", "white");

        H3 summaryTitle = new H3("💰 Balance Overview");
        summaryTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("color", "white")
                .set("font-weight", "600")
                .set("text-align", "center")
                .set("font-size", "1.5rem");

        // Get balance analytics
        BankBalance currentBalance = dao.getCurrentBankBalance();
        double calculatedBalance = calculateCurrentBalance();

        // Create stats layout
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
        statsLayout.setSpacing(true);

        // Current Bank Balance
        double bankAmount = currentBalance != null ? currentBalance.getAmount() : 0.0;
        VerticalLayout bankCard = createBlueMiniStatCard("Bank Balance", "€" + String.format("%.2f", bankAmount), bankAmount >= 0);

        // Calculated Balance (from income/expenses)
        VerticalLayout calcCard = createBlueMiniStatCard("Calculated Balance", "€" + String.format("%.2f", calculatedBalance), calculatedBalance >= 0);

        // Difference
        double difference = bankAmount - calculatedBalance;
        VerticalLayout diffCard = createBlueMiniStatCard("Difference", "€" + String.format("%.2f", difference), difference >= 0);

        // Last Updated
        String lastUpdated = currentBalance != null ?
                formatTimeSince(currentBalance.getLastUpdated()) : "Never";
        VerticalLayout timeCard = createBlueMiniStatCard("Last Updated", lastUpdated, true);

        // Status
        String status = getBalanceStatus(bankAmount, calculatedBalance);
        VerticalLayout statusCard = createBlueMiniStatCard("Status", status, difference >= -10 && difference <= 10); // Green if within ±10

        statsLayout.add(bankCard, calcCard, diffCard, timeCard, statusCard);
        card.add(summaryTitle, statsLayout);

        return card;
    }

    private VerticalLayout createBlueMiniStatCard(String label, String value, boolean isPositive) {
        VerticalLayout miniCard = new VerticalLayout();
        miniCard.getStyle()
                .set("background", "rgba(255,255,255,0.15)")
                .set("border-radius", "12px")
                .set("padding", "1rem")
                .set("text-align", "center")
                .set("backdrop-filter", "blur(10px)")
                .set("min-width", "140px")
                .set("transition", "transform 0.2s ease");

        // Add hover effect
        miniCard.getElement().addEventListener("mouseenter", e -> {
            miniCard.getStyle().set("transform", "translateY(-2px)");
        });
        miniCard.getElement().addEventListener("mouseleave", e -> {
            miniCard.getStyle().set("transform", "translateY(0)");
        });

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.4rem")
                .set("font-weight", "700")
                .set("display", "block");

        // Set color based on context
        if (value.startsWith("€")) {
            valueSpan.getStyle().set("color", isPositive ? "#4ade80" : "#f87171"); // Green for positive, red for negative
        } else {
            valueSpan.getStyle().set("color", "white");
        }

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "rgba(255,255,255,0.8)")
                .set("display", "block")
                .set("margin-top", "0.25rem");

        miniCard.add(valueSpan, labelSpan);
        return miniCard;
    }

    private VerticalLayout createUpdateBalanceCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 4px 16px rgba(0,0,0,0.1)");

        H3 cardTitle = new H3("✏️ Update Balance");
        cardTitle.getStyle()
                .set("margin", "0 0 1.5rem 0")
                .set("font-size", "1.3rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-align", "center");

        // Current balance display
        VerticalLayout currentSection = new VerticalLayout();
        currentSection.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("margin-bottom", "1.5rem")
                .set("text-align", "center");

        Span currentLabel = new Span("Current Bank Balance");
        currentLabel.getStyle()
                .set("font-size", "1rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-weight", "500")
                .set("display", "block")
                .set("margin-bottom", "0.5rem");

        currentBalanceDisplay = new Span("€0.00");
        currentBalanceDisplay.getStyle()
                .set("font-size", "2.5rem")
                .set("font-weight", "700")
                .set("color", "var(--lumo-success-color)")
                .set("margin", "0.5rem 0")
                .set("display", "block");

        lastUpdatedDisplay = new Span("Never updated");
        lastUpdatedDisplay.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9rem")
                .set("display", "block");

        currentSection.add(currentLabel, currentBalanceDisplay, lastUpdatedDisplay);

        // Update form
        newBalanceField = new NumberField("New Balance Amount");
        newBalanceField.setPrefixComponent(new Span("€"));
        newBalanceField.setPlaceholder("Enter your current bank balance");
        newBalanceField.setWidthFull();
        newBalanceField.getStyle()
                .set("margin-bottom", "1.5rem");

        Button updateButton = new Button("Update Balance");
        updateButton.setIcon(VaadinIcon.EDIT.create());
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        updateButton.setWidthFull();
        updateButton.getStyle()
                .set("border-radius", "12px")
                .set("font-weight", "600")
                .set("margin-bottom", "0.5rem");

        Button refreshButton = new Button("Refresh Data");
        refreshButton.setIcon(VaadinIcon.REFRESH.create());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_LARGE);
        refreshButton.setWidthFull();
        refreshButton.getStyle()
                .set("border-radius", "12px")
                .set("font-weight", "600");

        updateButton.addClickListener(e -> updateBalance());
        refreshButton.addClickListener(e -> {
            refreshDisplays();
            getUI().ifPresent(ui -> ui.getPage().reload());
        });

        card.add(cardTitle, currentSection, newBalanceField, updateButton, refreshButton);
        return card;
    }

    private void updateBalance() {
        if (newBalanceField.isEmpty()) {
            Notification notification = Notification.show("Please enter a balance amount");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        double newAmount = newBalanceField.getValue();
        BankBalance bankBalance = new BankBalance(newAmount);

        dao.saveOrUpdateBankBalance(bankBalance);
        refreshDisplays();
        newBalanceField.clear();

        Notification notification = Notification.show("Bank balance updated successfully!");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Reload the page to refresh the summary card
        getUI().ifPresent(ui -> ui.getPage().reload());
    }

    private void refreshDisplays() {
        BankBalance currentBalance = dao.getCurrentBankBalance();

        if (currentBalance != null) {
            currentBalanceDisplay.setText("€" + String.format("%.2f", currentBalance.getAmount()));

            // Update color based on balance
            if (currentBalance.getAmount() >= 0) {
                currentBalanceDisplay.getStyle().set("color", "var(--lumo-success-color)");
            } else {
                currentBalanceDisplay.getStyle().set("color", "var(--lumo-error-color)");
            }

            lastUpdatedDisplay.setText("Last updated: " +
                    currentBalance.getLastUpdated().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));
        } else {
            currentBalanceDisplay.setText("€0.00");
            currentBalanceDisplay.getStyle().set("color", "var(--lumo-contrast-60pct)");
            lastUpdatedDisplay.setText("No balance recorded yet");
        }
    }

    private double calculateCurrentBalance() {
        // Get all income and expenses to calculate theoretical balance
        List<Income> allIncome = dao.getAllIncome();
        List<MainView.Expense> allExpenses = dao.getAllExpenses();

        double totalIncome = allIncome.stream().mapToDouble(Income::getAmount).sum();
        double totalExpenses = allExpenses.stream().mapToDouble(MainView.Expense::getAmount).sum();

        return totalIncome - totalExpenses;
    }

    private String getBalanceStatus(double bankBalance, double calculatedBalance) {
        double difference = Math.abs(bankBalance - calculatedBalance);

        if (difference <= 10) {
            return "✅ Accurate";
        } else if (difference <= 50) {
            return "⚠️ Minor Gap";
        } else if (difference <= 200) {
            return "🔶 Moderate Gap";
        } else {
            return "❌ Large Gap";
        }
    }

    private String formatTimeSince(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());

        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();

        if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else {
            return "Just now";
        }
    }
}