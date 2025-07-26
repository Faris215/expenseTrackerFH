package org.expense.tracker;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.format.DateTimeFormatter;

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

        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        dao = new ExpenseDAO(db.getConnection());

        // Header
        H2 title = new H2("🏦 Bank Balance Management");
        title.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("color", "var(--lumo-primary-text-color)")
                .set("font-weight", "600");

        // Current balance display card
        VerticalLayout currentBalanceCard = createCurrentBalanceCard();

        // Update balance section
        VerticalLayout updateBalanceCard = createUpdateBalanceCard();

        add(title, currentBalanceCard, updateBalanceCard);

        // Initialize displays
        refreshDisplays();
    }

    private VerticalLayout createCurrentBalanceCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("margin-bottom", "1rem");

        H2 cardTitle = new H2("Current Bank Balance");
        cardTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.2rem")
                .set("color", "var(--lumo-secondary-text-color)");

        currentBalanceDisplay = new Span("€0.00");
        currentBalanceDisplay.getStyle()
                .set("font-size", "2.5rem")
                .set("font-weight", "700")
                .set("color", "var(--lumo-success-color)")
                .set("margin", "0.5rem 0");

        lastUpdatedDisplay = new Span("Never updated");
        lastUpdatedDisplay.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.9rem");

        card.add(cardTitle, currentBalanceDisplay, lastUpdatedDisplay);
        return card;
    }

    private VerticalLayout createUpdateBalanceCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "12px")
                .set("padding", "1.5rem")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        H2 cardTitle = new H2("Update Balance");
        cardTitle.getStyle()
                .set("margin", "0 0 1rem 0")
                .set("font-size", "1.2rem")
                .set("color", "var(--lumo-secondary-text-color)");

        newBalanceField = new NumberField("New Balance Amount");
        newBalanceField.setPrefixComponent(new Span("€"));
        newBalanceField.setPlaceholder("Enter your current bank balance");
        newBalanceField.setWidthFull();
        newBalanceField.getStyle()
                .set("margin-bottom", "1rem");

        Button updateButton = new Button("Update Balance");
        updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateButton.getStyle()
                .set("border-radius", "8px")
                .set("font-weight", "600");

        updateButton.addClickListener(e -> updateBalance());

        HorizontalLayout buttonLayout = new HorizontalLayout(updateButton);
        buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);

        card.add(cardTitle, newBalanceField, buttonLayout);
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
}