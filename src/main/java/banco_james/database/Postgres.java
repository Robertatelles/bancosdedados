package banco_james.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Postgres {
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USUARIO = "postgres";
    private static final String SENHA = "123456";

    public static Connection getDatabase() {
        try {
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao PostgreSQL: " + e.getMessage());
            throw new RuntimeException("Não foi possível estabelecer conexão com o banco de dados");
        }
    }
}