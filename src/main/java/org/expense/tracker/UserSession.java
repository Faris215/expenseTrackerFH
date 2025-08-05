package org.expense.tracker;

import com.vaadin.flow.server.VaadinSession;

public class UserSession {
    private static final String USER_KEY = "current_user";

    public static void setCurrentUser(User user) {
        VaadinSession.getCurrent().setAttribute(USER_KEY, user);
    }

    public static User getCurrentUser() {
        return (User) VaadinSession.getCurrent().getAttribute(USER_KEY);
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static void logout() {
        VaadinSession.getCurrent().setAttribute(USER_KEY, null);
    }

    public static int getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : -1;
    }
}