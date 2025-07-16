package org.expense.tracker;

import java.sql.*;

public class DBConnect {

    private Connection connection;

    public void connectToDatabase(String dbName, String user, String password) {
        try {
            String url = "jdbc:postgresql://localhost:5432/" + dbName;
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
