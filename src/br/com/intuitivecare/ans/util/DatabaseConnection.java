package br.com.intuitivecare.ans.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Configurações do banco local
    private static final String URL = "jdbc:postgresql://localhost:5433/ans_desafio";
    private static final String USER = "postgres";
    private static final String PASS = "1234";

    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver que baixei
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL não encontrado!", e);
        }
    }
}