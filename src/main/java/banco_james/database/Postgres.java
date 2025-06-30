package banco_james.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import banco_james.model.Pessoa;

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

    public Pessoa buscarPorCpf(String cpf) {
        String sql = "SELECT id, nome, email, cpf, data_nascimento, trabalho FROM pessoas WHERE cpf = ?";

        try (Connection conn = getDatabase();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String email = rs.getString("email");
                String cpfEncontrado = rs.getString("cpf");
                String nascimento = rs.getString("nascimento");
                String trabalho = rs.getString("trabalho");

                return new Pessoa(id, nome, email, cpfEncontrado, nascimento, trabalho);
            } else {
                return null; 
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar pessoa por CPF: " + e.getMessage());
            return null;
        }
    }

}