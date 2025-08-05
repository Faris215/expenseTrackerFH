package org.expense.tracker;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login - Finance Tracker")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final UserDAO userDAO;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField emailField;
    private boolean isRegistrationMode = false;

    public LoginView() {
        DBConnect db = new DBConnect();
        db.connectToDatabase("expenseTracker", "postgres", "faris123");
        this.userDAO = new UserDAO(db.getConnection());

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, var(--lumo-primary-color-10pct) 0%, var(--lumo-primary-color-50pct) 100%)");

        createLoginForm();
    }

    private void createLoginForm() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "16px")
                .set("padding", "2rem")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.15)")
                .set("width", "400px")
                .set("max-width", "90vw");

        // Header
        H1 title = new H1("💰 Finance Tracker");
        title.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("text-align", "center")
                .set("color", "var(--lumo-primary-color)")
                .set("font-size", "2rem");

        Paragraph subtitle = new Paragraph("Sign in to manage your expenses");
        subtitle.getStyle()
                .set("margin", "0 0 2rem 0")
                .set("text-align", "center")
                .set("color", "var(--lumo-secondary-text-color)");

        // Form fields
        usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.getStyle().set("margin-bottom", "1rem");

        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.getStyle().set("margin-bottom", "1rem");

        emailField = new TextField("Email");
        emailField.setWidthFull();
        emailField.getStyle().set("margin-bottom", "1rem");
        emailField.setVisible(false);

        // Buttons
        Button primaryButton = new Button("Sign In");
        primaryButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        primaryButton.setWidthFull();
        primaryButton.getStyle()
                .set("margin-bottom", "1rem")
                .set("border-radius", "8px");

        Button toggleButton = new Button("Don't have an account? Register");
        toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        toggleButton.setWidthFull();

        // Event handlers
        primaryButton.addClickListener(e -> {
            if (isRegistrationMode) {
                handleRegistration();
            } else {
                handleLogin();
            }
        });

        toggleButton.addClickListener(e -> toggleMode(primaryButton, toggleButton, subtitle));

        // Enter key support
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> {
            if (isRegistrationMode) {
                handleRegistration();
            } else {
                handleLogin();
            }
        });

        card.add(title, subtitle, usernameField, passwordField, emailField, primaryButton, toggleButton);
        add(card);
    }

    private void toggleMode(Button primaryButton, Button toggleButton, Paragraph subtitle) {
        isRegistrationMode = !isRegistrationMode;

        if (isRegistrationMode) {
            primaryButton.setText("Register");
            toggleButton.setText("Already have an account? Sign In");
            subtitle.setText("Create your account to get started");
            emailField.setVisible(true);
        } else {
            primaryButton.setText("Sign In");
            toggleButton.setText("Don't have an account? Register");
            subtitle.setText("Sign in to manage your expenses");
            emailField.setVisible(false);
        }

        clearFields();
    }

    private void handleLogin() {
        String username = usernameField.getValue().trim();
        String password = passwordField.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showNotification("Please fill in all fields", NotificationVariant.LUMO_ERROR);
            return;
        }

        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            UserSession.setCurrentUser(user);
            showNotification("Welcome back, " + user.getUsername() + "!", NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(DashboardView.class);
        } else {
            showNotification("Invalid username or password", NotificationVariant.LUMO_ERROR);
        }
    }

    private void handleRegistration() {
        String username = usernameField.getValue().trim();
        String password = passwordField.getValue();
        String email = emailField.getValue().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showNotification("Please fill in all fields", NotificationVariant.LUMO_ERROR);
            return;
        }

        if (password.length() < 6) {
            showNotification("Password must be at least 6 characters long", NotificationVariant.LUMO_ERROR);
            return;
        }

        User newUser = new User(username, password, email);
        int userId = userDAO.createUser(newUser);

        if (userId > 0) {
            newUser.setId(userId);
            UserSession.setCurrentUser(newUser);
            showNotification("Account created successfully! Welcome, " + username + "!", NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(DashboardView.class);
        } else {
            showNotification("Registration failed. Username or email might already exist.", NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
    }
}