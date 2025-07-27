package org.expense.tracker;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

public class MainLayout extends AppLayout {

    private Button themeToggle;
    private boolean isDarkMode = true; // Default to dark mode

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("💰 Expense Tracker");
        logo.getStyle()
                .set("margin", "0.5rem 1rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "600")
                .set("color", "var(--lumo-primary-text-color)");

        themeToggle = new Button();
        updateThemeToggleIcon();
        themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeToggle.getStyle()
                .set("margin-right", "1rem")
                .set("border-radius", "50%")
                .set("width", "40px")
                .set("height", "40px");

        themeToggle.addClickListener(e -> toggleTheme());

        HorizontalLayout header = new HorizontalLayout(logo);
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.setWidthFull();
        header.getStyle()
                .set("padding", "0.5rem 1rem")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("background", "var(--lumo-base-color)");

        // Add theme toggle to the right side
        HorizontalLayout rightSide = new HorizontalLayout(themeToggle);
        rightSide.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.add(rightSide);
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink mainLink = createStyledRouterLink("🏠 Dashboard", DashboardView.class);
        RouterLink expensesLink = createStyledRouterLink("💸 Expenses", MainView.class);
        RouterLink balanceLink = createStyledRouterLink("📊 Balance Chart", BalanceChartView.class);
        RouterLink incomeLink = createStyledRouterLink("💰 Monthly Income", MonthlyIncomeView.class);
        RouterLink compareLink = createStyledRouterLink("📈 Income vs Expenses", IncomeExpenseChartView.class);
        RouterLink bankBalanceLink = createStyledRouterLink("🏦 Bank Balance", BankBalanceView.class);

        VerticalLayout menuLayout = new VerticalLayout(
                mainLink, expensesLink, incomeLink, bankBalanceLink, balanceLink, compareLink
        );
        menuLayout.setPadding(false);
        menuLayout.setSpacing(false);
        menuLayout.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "1rem 0");

        addToDrawer(menuLayout);
    }

    private RouterLink createStyledRouterLink(String text, Class<?> navigationTarget) {
        RouterLink link = new RouterLink(text, (Class<? extends com.vaadin.flow.component.Component>) navigationTarget);
        link.getStyle()
                .set("padding", "0.75rem 1.5rem")
                .set("text-decoration", "none")
                .set("color", "var(--lumo-body-text-color)")
                .set("display", "block")
                .set("border-radius", "0")
                .set("transition", "all 0.2s ease")
                .set("font-weight", "500");

        link.getElement().addEventListener("mouseenter", e -> {
            link.getStyle()
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("transform", "translateX(4px)");
        });

        link.getElement().addEventListener("mouseleave", e -> {
            link.getStyle()
                    .set("background", "transparent")
                    .set("color", "var(--lumo-body-text-color)")
                    .set("transform", "translateX(0)");
        });

        return link;
    }

    private void toggleTheme() {
        isDarkMode = !isDarkMode;

        if (isDarkMode) {
            getElement().executeJs("document.documentElement.setAttribute('theme', 'dark')");
        } else {
            getElement().executeJs("document.documentElement.removeAttribute('theme')");
        }

        // Store theme preference in session
        VaadinSession.getCurrent().setAttribute("theme", isDarkMode ? "dark" : "light");
        updateThemeToggleIcon();
    }

    private void updateThemeToggleIcon() {
        if (isDarkMode) {
            themeToggle.setIcon(VaadinIcon.SUN_O.create());
            themeToggle.setTooltipText("Switch to Light Mode");
        } else {
            themeToggle.setIcon(VaadinIcon.MOON_O.create());
            themeToggle.setTooltipText("Switch to Dark Mode");
        }
    }
}